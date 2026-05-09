import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { UserAdminDto } from '../models/admin.model';
import { ReviewDto } from '../models/review.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiService: ApiService;

  constructor(apiService: ApiService) {
    this.apiService = apiService;
  }

  getAllUsers(): Observable<UserAdminDto[]> {
    return this.apiService.get<UserAdminDto[]>('/user/all');
  }

  getUserReviews(userId: string): Observable<ReviewDto[]> {
    return this.apiService.get<ReviewDto[]>(`/user/${userId}/reviews`);
  }

  updateUserRoles(userId: string, roles: string[]): Observable<UserAdminDto> {
    return this.apiService.put<UserAdminDto>(`/user/${userId}/roles`, roles);
  }
}
