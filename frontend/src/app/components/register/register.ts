import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RegisterDto } from '../../models/auth.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './register.html',
  styleUrls: ['./register.css'],
})
export class Register {
  registerDto: RegisterDto = { email: '', username: '', password: '' };
  error: string = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  register() {
    this.error = '';
    this.loading = true;
    this.authService.register(this.registerDto).subscribe(
      () => {
        this.router.navigate(['/login']);
      },
      (error) => {
        this.error = error?.error?.message || 'Registration failed';
        this.loading = false;
      }
    );
  }
}
