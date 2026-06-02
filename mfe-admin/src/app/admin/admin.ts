import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserService } from '../services/user.service';
import { RoleService } from '../services/role.service';
import { FeedbackService } from '../services/feedback.service';
import { ReviewDto } from '../models/review.model';
import { FeedbackAdminDto, UserAdminDto } from '../models/admin.model';

@Component({
  selector: 'mfe-admin-root',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './admin.html',
  styleUrls: ['./admin.css'],
})
export class AdminRoot implements OnInit {
  users: UserAdminDto[] = [];
  selectedUserId: string = '';
  selectedUser: UserAdminDto | null = null;
  selectedRoles: string[] = [];
  userReviews: ReviewDto[] = [];
  roleDrafts: string[] = [''];
  availableRoles: string[] = [];
  feedbackEntries: FeedbackAdminDto[] = [];
  loadingUsers = false;
  loadingRoles = false;
  loadingReviews = false;
  loadingFeedback = false;
  savingRoles = false;
  creatingRoles = false;
  message = '';
  error = '';

  constructor(
    private userService: UserService,
    private roleService: RoleService,
    private feedbackService: FeedbackService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadRoles();
    this.loadFeedback();
  }

  loadUsers() {
    this.loadingUsers = true;
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        if (this.selectedUserId) {
          this.syncSelectedUser();
        }
      },
      error: (error) => this.handleError('Could not load users', error),
      complete: () => (this.loadingUsers = false),
    });
  }

  loadRoles() {
    this.loadingRoles = true;
    this.roleService.getRoles().subscribe({
      next: (roles) => {
        this.availableRoles = roles;
        this.syncSelectedUser();
      },
      error: (error) => this.handleError('Could not load roles', error),
      complete: () => (this.loadingRoles = false),
    });
  }

  loadFeedback() {
    this.loadingFeedback = true;
    this.feedbackService.getAllFeedback().subscribe({
      next: (feedback) => (this.feedbackEntries = feedback),
      error: (error) => this.handleError('Could not load feedback', error),
      complete: () => (this.loadingFeedback = false),
    });
  }

  onUserChange() {
    this.syncSelectedUser();
    this.userReviews = [];
    this.message = '';
    this.error = '';
  }

  private syncSelectedUser() {
    this.selectedUser = this.users.find((user) => user.id === this.selectedUserId) || null;
    this.selectedRoles = this.selectedUser?.roles ? [...this.selectedUser.roles] : [];
  }

  loadSelectedUserReviews() {
    if (!this.selectedUserId) return;
    this.loadingReviews = true;
    this.userService.getUserReviews(this.selectedUserId).subscribe({
      next: (data) => (this.userReviews = data),
      error: (error) => this.handleError('Could not load user reviews', error),
      complete: () => (this.loadingReviews = false),
    });
  }

  addRole(index: number) {
    this.roleDrafts.splice(index + 1, 0, '');
  }

  removeRole(index: number) {
    this.roleDrafts.splice(index, 1);
  }

  submitRoles() {
    this.creatingRoles = true;
    this.message = '';
    this.error = '';
    this.roleService.addRoles(this.roleDrafts.filter((r) => r.trim())).subscribe({
      next: () => {
        this.message = 'Roles added successfully';
        this.roleDrafts = [''];
        this.loadRoles();
      },
      error: (error) => this.handleError('Could not add roles', error),
      complete: () => (this.creatingRoles = false),
    });
  }

  toggleSelectedRole(role: string) {
    if (this.selectedRoles.includes(role)) {
      this.selectedRoles = this.selectedRoles.filter((current) => current !== role);
    } else {
      this.selectedRoles = [...this.selectedRoles, role];
    }
  }

  saveUserRoles() {
    if (!this.selectedUserId) return;
    this.savingRoles = true;
    this.message = '';
    this.error = '';
    this.userService.updateUserRoles(this.selectedUserId, this.selectedRoles).subscribe({
      next: (updatedUser) => {
        this.selectedUser = updatedUser;
        this.selectedRoles = [...updatedUser.roles];
        this.users = this.users.map((user) => (user.id === updatedUser.id ? updatedUser : user));
        this.message = 'User roles updated successfully';
      },
      error: (error) => this.handleError('Could not update user roles', error),
      complete: () => (this.savingRoles = false),
    });
  }

  private handleError(message: string, error: unknown) {
    console.error('[mfe-admin]', message, error);
    this.error = message;
    this.message = '';
    this.loadingUsers = false;
    this.loadingRoles = false;
    this.loadingReviews = false;
    this.loadingFeedback = false;
    this.creatingRoles = false;
    this.savingRoles = false;
  }
}
