import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReviewDto } from '../models/review.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private apiService: ApiService) {}

  getUserReviews(userId: string): Observable<ReviewDto[]> {
    return this.apiService.get<ReviewDto[]>(`/user/${userId}/reviews`);
  }
}
