import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MenuItemDto } from '../models/menu-item.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class MenuItemService {
  constructor(private apiService: ApiService) {}

  addMenuItem(menuId: string, item: MenuItemDto): Observable<MenuItemDto> {
    return this.apiService.post<MenuItemDto>(`/menu-items/add/${menuId}`, item);
  }

  deleteMenuItem(menuItemId: string): Observable<string> {
    return this.apiService.delete<string>(`/menu-items/delete/${menuItemId}`);
  }

  updateMenuItem(menuItemId: string, item: MenuItemDto): Observable<MenuItemDto> {
    return this.apiService.put<MenuItemDto>(`/menu-items/update/${menuItemId}`, item);
  }

  getMenuItem(menuItemId: string): Observable<MenuItemDto> {
    return this.apiService.get<MenuItemDto>(`/menu-items/get/${menuItemId}`);
  }

  getMenuItemsByMenu(menuId: string): Observable<MenuItemDto[]> {
    return this.apiService.get<MenuItemDto[]>(`/menu-items/get-by-menu/${menuId}`);
  }
}
