import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class RoleService {
  constructor(private api: ApiService) {}

  getRoles(): Observable<string[]> {
    return this.api.get<string[]>('/role/all');
  }

  addRoles(roles: string[]): Observable<string[]> {
    return this.api.post<string[]>('/role/add', roles);
  }
}
