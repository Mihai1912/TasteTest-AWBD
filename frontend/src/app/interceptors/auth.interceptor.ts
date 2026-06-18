import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (req.url.includes('/auth/login') || req.url.includes('/auth/register')) {
      return next.handle(req);
    }

    const token = this.authService.getToken();
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(req).pipe(
      catchError(error => {
        // Only force a logout when the server tells us the token itself is
        // bad — i.e. it sent back an "expired/invalid token" 401. Generic
        // 401s from individual endpoints (admin-only data, permission gaps)
        // must NOT nuke the whole session.
        if (this.isTokenRejection(error)) {
          console.warn('Server rejected the stored token. Logging out...');
          this.authService.logout();
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }

  private isTokenRejection(error: unknown): boolean {
    if (!(error instanceof HttpErrorResponse)) return false;
    if (error.status !== 401) return false;
    const body = error.error;
    const message: string = (typeof body === 'string' ? body : body?.message || body?.error || '')
      .toString()
      .toLowerCase();
    return (
      message.includes('expired') ||
      message.includes('invalid token') ||
      message.includes('missing authorization') ||
      message.includes('jwt')
    );
  }
}
