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
import { of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

@Component({
  selector: 'app-restaurant-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './restaurant-detail.html',
  styleUrls: ['./restaurant-detail.css'],
})
export class RestaurantDetail implements OnInit {
  restaurant: RestaurantDto | null = null;
  reviews: ReviewIdDto[] = [];
  menus: MenuDto[] = [];
  rating: number = 0;
  restaurantId: string = '';
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private restaurantService: RestaurantService,
    private reviewService: ReviewService,
    private menuService: MenuService
  ) {}

  ngOnInit() {
    this.restaurantId = this.route.snapshot.paramMap.get('id') || '';
    if (!this.restaurantId) {
      this.error = 'Missing restaurant id';
      return;
    }

    this.loadRestaurant();
  }

  loadRestaurant() {
    this.loading = true;
    this.error = null;

    this.restaurantService.getRestaurant(this.restaurantId).pipe(
      catchError((err) => {
        console.error('Failed to load restaurant:', err);
        this.error = 'Unable to load restaurant details.';
        return of(null);
      }),
      finalize(() => {
        this.loading = false;
      })
    ).subscribe((restaurant) => {
      this.restaurant = restaurant;
      if (restaurant) {
        this.loadRating();
        this.loadReviews();
        this.loadMenus();
      } else if (!this.error) {
        this.error = 'Restaurant not found.';
      }
    });
  }

  loadRating() {
    this.restaurantService.getRatings(this.restaurantId).pipe(
      catchError((err) => {
        console.error('Failed to load ratings:', err);
        return of(0);
      })
    ).subscribe((rating) => {
      this.rating = rating;
    });
  }

  loadReviews() {
    this.reviewService.getRestaurantReviews(this.restaurantId).pipe(
      catchError((err) => {
        console.error('Failed to load reviews:', err);
        return of([] as ReviewIdDto[]);
      })
    ).subscribe((reviews) => {
      this.reviews = reviews;
    });
  }

  loadMenus() {
    this.menuService.getRestaurantMenus(this.restaurantId).pipe(
      catchError((err) => {
        console.error('Failed to load menus:', err);
        return of([] as MenuDto[]);
      })
    ).subscribe((menus) => {
      this.menus = menus;
    });
  }
}
