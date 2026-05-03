import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoginDto, RegisterDto, LoginResponseDto } from '../models/auth.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private apiService: ApiService) {}

  register(registerDto: RegisterDto): Observable<any> {
    return this.apiService.post('/auth/register', registerDto);
  }

  login(loginDto: LoginDto): Observable<LoginResponseDto> {
    return this.apiService.post<LoginResponseDto>('/auth/login', loginDto).pipe(
      tap(response => {
        localStorage.setItem('access_token', response.access_token);
        localStorage.setItem('token_type', response.token_type);
        localStorage.setItem('expires_in', response.expires_in.toString());
      })
    );
  }

  validateToken(): Observable<string> {
    return this.apiService.get<string>('/auth/token');
  }

  logout(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('token_type');
    localStorage.removeItem('expires_in');
  }

  getToken(): string | null {
    return localStorage.getItem('access_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
