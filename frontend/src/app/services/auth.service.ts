import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoginDto, RegisterDto, LoginResponseDto } from '../models/auth.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly accessTokenKey = 'access_token';
  private readonly tokenTypeKey = 'token_type';
  private readonly expiresInKey = 'expires_in';
  private readonly userEmailKey = 'user_email';

  private readonly loggedInSubject = new BehaviorSubject<boolean>(!!localStorage.getItem(this.accessTokenKey));
  private readonly userEmailSubject = new BehaviorSubject<string | null>(localStorage.getItem(this.userEmailKey));

  readonly isLoggedIn$ = this.loggedInSubject.asObservable();
  readonly userEmail$ = this.userEmailSubject.asObservable();

  constructor(private apiService: ApiService) {}

  register(registerDto: RegisterDto): Observable<any> {
    return this.apiService.post('/auth/register', registerDto);
  }

  login(loginDto: LoginDto): Observable<LoginResponseDto> {
    return this.apiService.post<LoginResponseDto>('/auth/login', loginDto).pipe(
      tap(response => {
        // normalize possible response shapes
        const token = (response as any).access_token || (response as any).token || (response as any).accessToken;
        const type = (response as any).token_type || (response as any).tokenType || 'Bearer';
        const expires = (response as any).expires_in || (response as any).expiresIn || 0;

        if (token) {
          localStorage.setItem(this.accessTokenKey, token);
        }
        if (type) {
          localStorage.setItem(this.tokenTypeKey, type);
        }
        if (expires !== undefined && expires !== null) {
          localStorage.setItem(this.expiresInKey, expires.toString());
        }

        localStorage.setItem(this.userEmailKey, loginDto.email);
        this.loggedInSubject.next(!!token);
        this.userEmailSubject.next(loginDto.email);
      })
    );
  }

  validateToken(): Observable<string> {
    return this.apiService.get<string>('/auth/token');
  }

  logout(): void {
    localStorage.removeItem(this.accessTokenKey);
    localStorage.removeItem(this.tokenTypeKey);
    localStorage.removeItem(this.expiresInKey);
    localStorage.removeItem(this.userEmailKey);
    this.loggedInSubject.next(false);
    this.userEmailSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.accessTokenKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
