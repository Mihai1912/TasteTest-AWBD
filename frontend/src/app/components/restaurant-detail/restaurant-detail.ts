import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RestaurantService } from '../../services/restaurant.service';
import { ReviewService } from '../../services/review.service';
import { MenuService } from '../../services/menu.service';
import { RestaurantDto } from '../../models/restaurant.model';
import { ReviewIdDto } from '../../models/review.model';
import { MenuDto } from '../../models/menu.model';

@Component({
  selector: 'app-restaurant-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './restaurant-detail.html',
  styleUrl: './restaurant-detail.css',
})
export class RestaurantDetail implements OnInit {
  restaurant: RestaurantDto | null = null;
  reviews: ReviewIdDto[] = [];
  menus: MenuDto[] = [];
  rating: number = 0;
  restaurantId: string = '';

  constructor(
    private route: ActivatedRoute,
    private restaurantService: RestaurantService,
    private reviewService: ReviewService,
    private menuService: MenuService
  ) {}

  ngOnInit() {
    this.restaurantId = this.route.snapshot.paramMap.get('id') || '';
    this.loadRestaurant();
  }

  loadRestaurant() {
    this.restaurantService.getRestaurant(this.restaurantId).subscribe(
      (data) => {
        this.restaurant = data;
        this.loadRating();
      }
    );
    this.loadReviews();
    this.loadMenus();
  }

  loadRating() {
    this.restaurantService.getRatings(this.restaurantId).subscribe((rating) => {
      this.rating = rating;
    });
  }

  loadReviews() {
    this.reviewService.getRestaurantReviews(this.restaurantId).subscribe((data) => {
      this.reviews = data;
    });
  }

  loadMenus() {
    this.menuService.getRestaurantMenus(this.restaurantId).subscribe((data) => {
      this.menus = data;
    });
  }
}
