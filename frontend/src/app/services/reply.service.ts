import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReplyDto } from '../models/reply.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class ReplyService {
  constructor(private apiService: ApiService) {}

  addReply(reviewId: string, reply: ReplyDto): Observable<ReplyDto> {
    return this.apiService.post<ReplyDto>(`/replies/add/${reviewId}`, reply);
  }

  deleteReply(id: string): Observable<string> {
    return this.apiService.delete<string>(`/replies/delete/${id}`);
  }

  updateReply(id: string, reply: ReplyDto): Observable<ReplyDto> {
    return this.apiService.put<ReplyDto>(`/replies/update/${id}`, reply);
  }

  getReply(id: string): Observable<ReplyDto> {
    return this.apiService.get<ReplyDto>(`/replies/get/${id}`);
  }

  getAllRepliesOfReview(reviewId: string): Observable<ReplyDto[]> {
    return this.apiService.get<ReplyDto[]>(`/replies/getAllofReview/${reviewId}`);
  }
}
