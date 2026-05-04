import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoginDto, RegisterDto, LoginResponseDto } from '../models/auth.model';
import { ApiService } from './api.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly accessTokenKey = 'access_token';
  private readonly tokenTypeKey = 'token_type';
  private readonly expiresInKey = 'expires_in';
  private readonly tokenExpirationTimeKey = 'token_expiration_time';
  private readonly userEmailKey = 'user_email';

  private readonly loggedInSubject = new BehaviorSubject<boolean>(false);
  private readonly userEmailSubject = new BehaviorSubject<string | null>(null);

  readonly isLoggedIn$ = this.loggedInSubject.asObservable();
  readonly userEmail$ = this.userEmailSubject.asObservable();

  private tokenExpirationTimer: any;

  constructor(private apiService: ApiService) {
    this.debug('Initializing auth service');
    this.restoreAuthState();
  }

  /**
   * Decode JWT payload and return parsed object, or null if token missing/invalid
   */
  private getTokenPayload(): any | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      if (!payload) return null;
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
      const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
      return JSON.parse(atob(padded));
    } catch (e) {
      console.warn('Failed to decode token payload', e);
      return null;
    }
  }

  /**
   * Try to extract roles/authorities from token payload. Returns array of role strings (may be empty).
   */
  getUserRoles(): string[] {
    const payload = this.getTokenPayload();
    if (!payload) return [];

    // Common claim names used by various backends
    const possibleClaims = ['roles', 'role', 'authorities', 'authorities', 'roles_claim'];
    for (const claim of possibleClaims) {
      const val = payload[claim];
      if (!val) continue;
      if (Array.isArray(val)) return val.map((v) => String(v));
      if (typeof val === 'string') return val.split(',').map((s: string) => s.trim());
    }

    // Some tokens place roles under 'scope' or 'scopes'
    if (payload.scope) return String(payload.scope).split(' ').map((s: string) => s.trim());
    if (payload.scopes) return Array.isArray(payload.scopes) ? payload.scopes.map((s: any) => String(s)) : String(payload.scopes).split(' ').map((s: string) => s.trim());

    return [];
  }

  hasRole(role: string): boolean {
    if (!role) return false;
    const roles = this.getUserRoles().map((r) => r.toLowerCase());
    const wanted = role.toLowerCase();
    return roles.includes(wanted) || roles.includes(`role_${wanted}`) || roles.includes(`role-${wanted}`) || roles.includes(`roles_${wanted}`);
  }

  isAdmin(): boolean {
    return this.hasRole('admin') || this.hasRole('ROLE_ADMIN');
  }

  register(registerDto: RegisterDto): Observable<any> {
    return this.apiService.post('/auth/register', registerDto);
  }

  login(loginDto: LoginDto): Observable<LoginResponseDto> {
    this.debug('Login request started', { email: loginDto.email });
    return this.apiService.post<LoginResponseDto>('/auth/login', loginDto).pipe(
      tap(response => {
        const token = (response as any).access_token || (response as any).token || (response as any).accessToken;
        const type = (response as any).token_type || (response as any).tokenType || 'Bearer';
        const expires = Number((response as any).expires_in ?? (response as any).expiresIn ?? 0);

        if (token) {
          localStorage.setItem(this.accessTokenKey, token);
        }
        if (type) {
          localStorage.setItem(this.tokenTypeKey, type);
        }
        if (Number.isFinite(expires) && expires > 0) {
          localStorage.setItem(this.expiresInKey, expires.toString());
          const expirationTime = this.resolveExpirationTime(token, expires);
          this.setExpirationTime(expirationTime);
        } else {
          localStorage.removeItem(this.expiresInKey);
          this.setExpirationTime(this.resolveExpirationTime(token));
        }

        localStorage.setItem(this.userEmailKey, loginDto.email);
        this.loggedInSubject.next(!!token);
        this.userEmailSubject.next(loginDto.email);
        this.debug('Login success', {
          hasToken: !!token,
          tokenType: type,
          expiresIn: Number.isFinite(expires) ? expires : null,
          expirationTime: localStorage.getItem(this.tokenExpirationTimeKey)
        });

        this.setupTokenExpirationTimer();
      })
    );
  }

  private restoreAuthState(): void {
    const token = this.getToken();
    if (!token) {
      this.loggedInSubject.next(false);
      this.userEmailSubject.next(null);
      this.debug('No stored token found on startup');
      return;
    }

    const storedEmail = localStorage.getItem(this.userEmailKey);
    this.userEmailSubject.next(storedEmail);

    const expirationTime = this.resolveExpirationTime(token);
    if (expirationTime) {
      this.setExpirationTime(expirationTime);
    }

    if (!this.isTokenValid()) {
      this.debug('Stored token is invalid or expired on startup; logging out');
      this.logout();
      return;
    }

    this.loggedInSubject.next(true);
    this.debug('Auth restored from storage', {
      email: storedEmail,
      expirationTime: localStorage.getItem(this.tokenExpirationTimeKey)
    });
    this.setupTokenExpirationTimer();
  }

  private resolveExpirationTime(token?: string | null, expiresIn?: number | null): number | null {
    const jwtExpiration = token ? this.getJwtExpirationTime(token) : null;
    if (jwtExpiration) {
      return jwtExpiration;
    }

    const storedExpirationTime = this.getStoredExpirationTime();
    if (storedExpirationTime) {
      return storedExpirationTime;
    }

    if (expiresIn && Number.isFinite(expiresIn) && expiresIn > 0) {
      const now = Date.now();
      // Backend currently sends token.ttl; treat it as milliseconds unless it clearly looks like seconds.
      const durationMs = expiresIn > 100000 ? expiresIn : expiresIn * 1000;
      return now + durationMs;
    }

    return null;
  }

  private getStoredExpirationTime(): number | null {
    const expirationTimeStr = localStorage.getItem(this.tokenExpirationTimeKey);
    if (!expirationTimeStr) {
      return null;
    }

    const expirationTime = Number(expirationTimeStr);
    return Number.isFinite(expirationTime) && expirationTime > 0 ? expirationTime : null;
  }

  private setExpirationTime(expirationTime: number | null): void {
    if (expirationTime && Number.isFinite(expirationTime) && expirationTime > 0) {
      localStorage.setItem(this.tokenExpirationTimeKey, expirationTime.toString());
    } else {
      localStorage.removeItem(this.tokenExpirationTimeKey);
    }
  }

  private getJwtExpirationTime(token: string): number | null {
    try {
      const payload = token.split('.')[1];
      if (!payload) {
        return null;
      }

      const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/');
      const paddedPayload = normalizedPayload.padEnd(Math.ceil(normalizedPayload.length / 4) * 4, '=');
      const decoded = JSON.parse(atob(paddedPayload));
      if (typeof decoded.exp === 'number') {
        return decoded.exp * 1000;
      }
    } catch (error) {
      console.warn('Unable to decode JWT expiration:', error);
    }

    return null;
  }

  /**
   * Set up a timer to automatically logout when token expires
   */
  private setupTokenExpirationTimer() {
    // Clear any existing timer
    if (this.tokenExpirationTimer) {
      clearTimeout(this.tokenExpirationTimer);
    }

    const expirationTimeStr = localStorage.getItem(this.tokenExpirationTimeKey);
    if (!expirationTimeStr) {
      return;
    }

    const expirationTime = parseInt(expirationTimeStr, 10);
    const now = new Date().getTime();
    const timeUntilExpiration = expirationTime - now;

    if (timeUntilExpiration <= 0) {
      // Token already expired
      console.warn('Token already expired. Logging out...');
      this.logout();
      return;
    }

    const timerDuration = timeUntilExpiration;

    this.debug(`Token expires in ${Math.round(timeUntilExpiration / 1000)} seconds`);

    this.tokenExpirationTimer = setTimeout(() => {
      this.warn('Token expiration timer triggered. Logging out...');
      this.logout();
    }, timerDuration);
  }

  validateToken(): Observable<string> {
    return this.apiService.get<string>('/auth/token');
  }

  /**
   * Check if token is still valid (not expired)
   */
  isTokenValid(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    const expirationTime = this.resolveExpirationTime(token);
    if (!expirationTime) {
      return true;
    }

    return Date.now() < expirationTime;
  }

  logout(): void {
    if (this.tokenExpirationTimer) {
      clearTimeout(this.tokenExpirationTimer);
    }
    localStorage.removeItem(this.accessTokenKey);
    localStorage.removeItem(this.tokenTypeKey);
    localStorage.removeItem(this.expiresInKey);
    localStorage.removeItem(this.tokenExpirationTimeKey);
    localStorage.removeItem(this.userEmailKey);
    this.loggedInSubject.next(false);
    this.userEmailSubject.next(null);
    this.debug('Logged out and cleared auth state');
  }

  getToken(): string | null {
    return localStorage.getItem(this.accessTokenKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken() && this.isTokenValid();
  }

  private debug(message: string, data?: unknown): void {
    console.debug(`[AuthService] ${message}`, data ?? '');
  }

  private warn(message: string, data?: unknown): void {
    console.warn(`[AuthService] ${message}`, data ?? '');
  }
}
