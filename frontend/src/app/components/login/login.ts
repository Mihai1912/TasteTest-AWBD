import { Component } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginDto } from '../../models/auth.model';
import { CommonModule } from '@angular/common';

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
    this.authService.login(this.loginDto)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe(
        (response) => {
          // navigate after successful login
          this.router.navigate(['/restaurants']);
        },
        (error) => {
          // Log full error to console for debugging and show a friendly message
          console.error('Login error:', error);
          // prefer server message but fall back to http message
          this.error = error?.error?.message || error?.message || 'Login failed';
        }
      );
  }
}
