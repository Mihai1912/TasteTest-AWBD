import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { FeedbackDto } from '../models/feedback.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class FeedbackService {
  constructor(private apiService: ApiService) {}

  addFeedback(feedback: FeedbackDto): Observable<FeedbackDto> {
    return this.apiService.post<FeedbackDto>('/feedback/add', feedback);
  }
}
