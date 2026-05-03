import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MenuDto } from '../models/menu.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  constructor(private apiService: ApiService) {}

  addMenu(name: string, restaurantName: string): Observable<MenuDto> {
    return this.apiService.post<MenuDto>(
      `/menus/add?name=${encodeURIComponent(name)}&restaurantName=${encodeURIComponent(restaurantName)}`,
      {}
    );
  }

  deleteMenu(id: string): Observable<string> {
    return this.apiService.delete<string>(`/menus/delete/${id}`);
  }

  updateMenu(id: string, name: string): Observable<MenuDto> {
    return this.apiService.put<MenuDto>(
      `/menus/update/${id}?name=${encodeURIComponent(name)}`,
      {}
    );
  }

  getMenu(id: string): Observable<MenuDto> {
    return this.apiService.get<MenuDto>(`/menus/get/${id}`);
  }

  getRestaurantMenus(restaurantId: string): Observable<MenuDto[]> {
    return this.apiService.get<MenuDto[]>(`/menus/getRestaurantMenus/${restaurantId}`);
  }
}
