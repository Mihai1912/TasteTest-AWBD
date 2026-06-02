import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface ChatRequest {
  message: string;
}

export interface ChatResponse {
  reply: string;
}

@Injectable({ providedIn: 'root' })
export class AgentService {
  constructor(private apiService: ApiService) {}

  chat(message: string): Observable<ChatResponse> {
    return this.apiService.post<ChatResponse>('/agent/chat', { message });
  }
}
