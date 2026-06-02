import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { FeedbackAdminDto } from '../models/admin.model';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  constructor(private api: ApiService) {}

  getAllFeedback(): Observable<FeedbackAdminDto[]> {
    return this.api.get<FeedbackAdminDto[]>('/feedback/all');
  }
}
