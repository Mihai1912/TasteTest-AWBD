import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RestaurantService } from '../../services/restaurant.service';
import { RestaurantDto } from '../../models/restaurant.model';

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
    this.loadRestaurants();
  }

  loadRestaurants() {
    this.loading = true;
    this.restaurantService.getAllRestaurants().subscribe(
      (data) => {
        this.restaurants = data;
        this.loading = false;
      },
      (error) => {
        console.error('Error loading restaurants', error);
        this.loading = false;
      }
    );
    this.loadTopRated();
  }

  loadTopRated() {
    this.restaurantService.getTopRatedRestaurants().subscribe(
      (data) => {
        this.topRated = data;
      },
      (error) => {
        console.error('Error loading top rated', error);
      }
    );
  }
}
