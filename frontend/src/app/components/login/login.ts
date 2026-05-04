import { Component } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginDto } from '../../models/auth.model';
import { CommonModule } from '@angular/common';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class Login {
  loginDto: LoginDto = { email: '', password: '' };
  error: string = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  login() {
    this.error = '';
    this.loading = true;
    this.log('Submitting login request', { email: this.loginDto.email });
    this.authService.login(this.loginDto)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe(
        (response) => {
          this.log('Login successful, navigating to restaurants', response);
          this.router.navigate(['/restaurants']);
        },
        (error) => {
          this.errorLog('Login error', error);
          this.error = error?.error?.message || error?.message || 'Login failed';
        }
      );
  }

  private log(message: string, data?: unknown): void {
    console.debug(`[Login] ${message}`, data ?? '');
  }

  private errorLog(message: string, data?: unknown): void {
    console.error(`[Login] ${message}`, data ?? '');
  }
}
