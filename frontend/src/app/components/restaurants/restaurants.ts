import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { RestaurantService } from '../../services/restaurant.service';
import { AuthService } from '../../services/auth.service';
import { RestaurantDto } from '../../models/restaurant.model';

type SortField = 'name' | 'address' | 'phone';
type SortDir = 'asc' | 'desc';

@Component({
  selector: 'app-restaurants',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './restaurants.html',
  styleUrls: ['./restaurants.css'],
})
export class Restaurants implements OnInit {
  restaurants: RestaurantDto[] = [];
  topRated: RestaurantDto[] = [];
  activeTab: 'all' | 'topRated' = 'all';

  page = 0;
  size = 10;
  sortField: SortField = 'name';
  sortDir: SortDir = 'asc';
  totalPages = 0;
  totalElements = 0;
  first = true;
  last = true;

  readonly pageSizeOptions = [5, 10, 20, 50];
  readonly sortFields: SortField[] = ['name', 'address', 'phone'];

  constructor(
    private restaurantService: RestaurantService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadRestaurants();
    this.loadTopRated();
  }

  /** Only admins and restaurant owners may add restaurants (mirrors the backend). */
  get canManageRestaurants(): boolean {
    try {
      return this.authService.isAdmin() || this.authService.hasRole('RESTAURANT_OWNER');
    } catch {
      return false;
    }
  }

  loadRestaurants() {
    this.restaurantService
      .getRestaurantsPaged({
        page: this.page,
        size: this.size,
        sort: `${this.sortField},${this.sortDir}`,
      })
      .subscribe({
        next: (data) => {
          this.restaurants = data.content;
          this.totalPages = data.totalPages;
          this.totalElements = data.totalElements;
          this.first = data.first;
          this.last = data.last;
          try { this.cdr.detectChanges(); } catch (e) { /* noop */ }
        },
        error: (error) => {
          console.error('[Restaurants] Error loading restaurants', error);
        },
      });
  }

  loadTopRated() {
    this.restaurantService.getTopRatedRestaurants().subscribe({
      next: (data) => {
        this.topRated = data;
        try { this.cdr.detectChanges(); } catch (e) { /* noop */ }
      },
      error: (error) =>
        console.error('[Restaurants] Error loading top rated restaurants', error),
    });
  }

  onSortChange() {
    this.page = 0;
    this.loadRestaurants();
  }

  onSizeChange() {
    this.page = 0;
    this.loadRestaurants();
  }

  toggleSortDir() {
    this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    this.page = 0;
    this.loadRestaurants();
  }

  goToPage(target: number) {
    if (target < 0 || target >= this.totalPages || target === this.page) return;
    this.page = target;
    this.loadRestaurants();
  }

  prevPage() {
    if (!this.first) this.goToPage(this.page - 1);
  }

  nextPage() {
    if (!this.last) this.goToPage(this.page + 1);
  }

  private readonly cardGradients = [
    'linear-gradient(135deg, #ffd166, #ef476f)',
    'linear-gradient(135deg, #06d6a0, #118ab2)',
    'linear-gradient(135deg, #8338ec, #3a86ff)',
    'linear-gradient(135deg, #f72585, #b5179e)',
    'linear-gradient(135deg, #ff9f1c, #ffbf69)',
    'linear-gradient(135deg, #2ec4b6, #20a4f3)',
    'linear-gradient(135deg, #e63946, #f1faee)',
    'linear-gradient(135deg, #43aa8b, #f9c74f)',
  ];

  // Curated food/restaurant photos (Unsplash). Used when a restaurant has no
  // imageUrl of its own. The gradient stays behind as a loading/error fallback.
  private readonly cardImages = [
    'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&q=80&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800&q=80&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&q=80&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800&q=80&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&q=80&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800&q=80&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1559339352-11d035aa65de?w=800&q=80&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?w=800&q=80&auto=format&fit=crop',
    'https://images.unsplash.com/photo-1481833761820-0509d3217039?w=800&q=80&auto=format&fit=crop',
  ];

  getCardGradient(i: number): string {
    return this.cardGradients[i % this.cardGradients.length];
  }

  /** Stable photo for a restaurant: its own imageUrl, else a curated stock image. */
  getCardImage(restaurant: RestaurantDto, i: number): string {
    if (restaurant.imageUrl) {
      return restaurant.imageUrl;
    }
    const seed = restaurant.id || restaurant.name || String(i);
    let hash = 0;
    for (let k = 0; k < seed.length; k++) {
      hash = (hash * 31 + seed.charCodeAt(k)) >>> 0;
    }
    return this.cardImages[hash % this.cardImages.length];
  }

  /** Hide a broken image so the gradient fallback shows through. */
  onImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.style.display = 'none';
  }
}
