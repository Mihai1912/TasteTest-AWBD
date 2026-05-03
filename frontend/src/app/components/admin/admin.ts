import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { RoleService } from '../../services/role.service';
import { ReviewDto } from '../../models/review.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './admin.html',
  styleUrl: './admin.css',
})
export class Admin {
  userId: string = '';
  userReviews: ReviewDto[] = [];
  roles: string[] = [''];
  loading = false;

  constructor(
    private userService: UserService,
    private roleService: RoleService
  ) {}

  searchUserReviews() {
    this.loading = true;
    this.userService.getUserReviews(this.userId).subscribe(
      (data) => {
        this.userReviews = data;
        this.loading = false;
      },
      () => {
        this.loading = false;
      }
    );
  }

  addRole(index: number) {
    this.roles.splice(index + 1, 0, '');
  }

  removeRole(index: number) {
    this.roles.splice(index, 1);
  }

  submitRoles() {
    this.roleService.addRoles(this.roles.filter((r) => r)).subscribe(() => {
      alert('Roles added successfully');
    });
  }
}
