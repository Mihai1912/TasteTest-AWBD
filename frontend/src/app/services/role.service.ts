import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  constructor(private apiService: ApiService) {}

  addRoles(roles: string[]): Observable<string[]> {
    return this.apiService.post<string[]>('/role/add', roles);
  }
}
