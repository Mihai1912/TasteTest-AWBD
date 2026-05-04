import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RestaurantService } from '../../services/restaurant.service';
import { RestaurantDto } from '../../models/restaurant.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-restaurants',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './restaurants.html',
  styleUrls: ['./restaurants.css'],
})
export class Restaurants implements OnInit {
  restaurants: RestaurantDto[] = [];
  topRated: RestaurantDto[] = [];
  loading = false;
  activeTab: 'all' | 'topRated' = 'all';

  constructor(private restaurantService: RestaurantService) {}

  ngOnInit() {
    this.log('Component initialized');
    this.loadRestaurants();
  }

  loadRestaurants() {
    this.loading = true;
    this.log('Loading restaurants and top-rated lists');
    this.restaurantService.getAllRestaurants().subscribe(
      (data) => {
        this.restaurants = data;
        this.loading = false;
        this.log('Loaded restaurants', { count: data.length });
      },
      (error) => {
        this.error('Error loading restaurants', error);
        this.loading = false;
      }
    );
    this.loadTopRated();
  }

  loadTopRated() {
    this.restaurantService.getTopRatedRestaurants().subscribe(
      (data) => {
        this.topRated = data;
        this.log('Loaded top-rated restaurants', { count: data.length });
      },
      (error) => {
        this.error('Error loading top rated restaurants', error);
      }
    );
  }

  private log(message: string, data?: unknown): void {
    console.debug(`[Restaurants] ${message}`, data ?? '');
  }

  private error(message: string, data?: unknown): void {
    console.error(`[Restaurants] ${message}`, data ?? '');
  }
}
