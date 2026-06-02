import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UserAdminDto } from '../models/admin.model';
import { ReviewDto } from '../models/review.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private api: ApiService) {}

  getAllUsers(): Observable<UserAdminDto[]> {
    return this.api.get<UserAdminDto[]>('/user/all');
  }

  getUserReviews(userId: string): Observable<ReviewDto[]> {
    return this.api.get<ReviewDto[]>(`/user/${userId}/reviews`);
  }

  updateUserRoles(userId: string, roles: string[]): Observable<UserAdminDto> {
    return this.api.put<UserAdminDto>(`/user/${userId}/roles`, roles);
  }
}
