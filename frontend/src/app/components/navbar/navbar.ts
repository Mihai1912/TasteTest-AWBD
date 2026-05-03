import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar implements OnInit {
  isLoggedIn = false;
  username: string | null = null;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.updateLoginStatus();
  }

  updateLoginStatus() {
    this.isLoggedIn = this.authService.isLoggedIn();
    if (this.isLoggedIn) {
      this.authService.validateToken().subscribe(
        (response) => {
          this.username = response;
        },
        (error) => {
          this.logout();
        }
      );
    }
  }

  logout() {
    this.authService.logout();
    this.isLoggedIn = false;
    this.username = null;
    this.router.navigate(['/login']);
  }
}
