import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { timeout, catchError } from 'rxjs/operators';
import { throwError, TimeoutError } from 'rxjs';
import { ApiService } from './api.service';

export interface ChatRequest {
  message: string;
}

export interface ChatResponse {
  reply: string;
}

// LLM inference can be slow — give it up to 2 minutes before giving up.
const AGENT_TIMEOUT_MS = 120_000;

@Injectable({ providedIn: 'root' })
export class AgentService {
  constructor(private apiService: ApiService) {}

  chat(message: string): Observable<ChatResponse> {
    return this.apiService.post<ChatResponse>('/agent/chat', { message }).pipe(
      timeout(AGENT_TIMEOUT_MS),
      catchError(err => {
        if (err instanceof TimeoutError) {
          return throwError(() => ({
            message: 'The model is taking too long to respond. Please try again.',
          }));
        }
        return throwError(() => err);
      }),
    );
  }
}
