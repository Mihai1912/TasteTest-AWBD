import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private readonly apiService: ApiService;

  constructor(apiService: ApiService) {
    this.apiService = apiService;
  }

  getRoles(): Observable<string[]> {
    return this.apiService.get<string[]>('/role/all');
  }

  addRoles(roles: string[]): Observable<string[]> {
    return this.apiService.post<string[]>('/role/add', roles);
  }
}
