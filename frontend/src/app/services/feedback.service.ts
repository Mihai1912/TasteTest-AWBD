import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { FeedbackAdminDto } from '../models/admin.model';
import { FeedbackDto } from '../models/feedback.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class FeedbackService {
  private readonly apiService: ApiService;

  constructor(apiService: ApiService) {
    this.apiService = apiService;
  }

  getAllFeedback(): Observable<FeedbackAdminDto[]> {
    return this.apiService.get<FeedbackAdminDto[]>('/feedback/all');
  }

  addFeedback(feedback: FeedbackDto): Observable<FeedbackDto> {
    return this.apiService.post<FeedbackDto>('/feedback/add', feedback);
  }
}
