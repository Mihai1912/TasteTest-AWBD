import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RestaurantDto } from '../models/restaurant.model';
import { Page, PageRequest } from '../models/page.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class RestaurantService {
  constructor(private apiService: ApiService) {}

  addRestaurant(restaurant: RestaurantDto): Observable<RestaurantDto> {
    return this.apiService.post<RestaurantDto>('/restaurant/add', restaurant);
  }

  deleteRestaurant(id: string): Observable<string> {
    return this.apiService.delete<string>(`/restaurant/delete/${id}`);
  }

  updateRestaurant(id: string, restaurant: RestaurantDto): Observable<RestaurantDto> {
    return this.apiService.put<RestaurantDto>(`/restaurant/update/${id}`, restaurant);
  }

  getRestaurant(id: string): Observable<RestaurantDto> {
    return this.apiService.get<RestaurantDto>(`/restaurant/get/${id}`);
  }

  getAllRestaurants(): Observable<RestaurantDto[]> {
    return this.apiService.get<RestaurantDto[]>('/restaurant/getAll');
  }

  getRestaurantsPaged(request: PageRequest = {}): Observable<Page<RestaurantDto>> {
    const params: Record<string, string | number> = {};
    if (request.page !== undefined) params['page'] = request.page;
    if (request.size !== undefined) params['size'] = request.size;
    if (request.sort) params['sort'] = request.sort;
    return this.apiService.get<Page<RestaurantDto>>('/restaurant/paged', params);
  }

  getRatings(id: string): Observable<number> {
    return this.apiService.get<number>(`/restaurant/getRatings/${id}`);
  }

  getTopRatedRestaurants(): Observable<RestaurantDto[]> {
    return this.apiService.get<RestaurantDto[]>('/restaurant/top-rated');
  }

  getRestaurantIdByName(name: string): Observable<string> {
    return this.apiService.get<string>(`/restaurant/getRestaurantId/${name}`);
  }
}
