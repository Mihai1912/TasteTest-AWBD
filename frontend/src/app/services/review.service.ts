import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReviewDto, ReviewIdDto } from '../models/review.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  constructor(private apiService: ApiService) {}

  addReview(restaurantId: string, review: ReviewDto, rating: number): Observable<ReviewDto> {
    return this.apiService.post<ReviewDto>(`/review/add/${restaurantId}?rating=${rating}`, review);
  }

  deleteReview(reviewId: string): Observable<string> {
    return this.apiService.delete<string>(`/review/delete/${reviewId}`);
  }

  updateReview(reviewId: string, review: ReviewDto): Observable<ReviewDto> {
    return this.apiService.put<ReviewDto>(`/review/update/${reviewId}`, review);
  }

  getReview(reviewId: string): Observable<ReviewDto> {
    return this.apiService.get<ReviewDto>(`/review/get/${reviewId}`);
  }

  getRestaurantReviews(restaurantId: string): Observable<ReviewIdDto[]> {
    return this.apiService.get<ReviewIdDto[]>(`/review/getRestaurantReviews/${restaurantId}`);
  }
}
