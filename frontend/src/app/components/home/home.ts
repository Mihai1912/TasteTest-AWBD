import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RestaurantService } from '../../services/restaurant.service';
import { AuthService } from '../../services/auth.service';
import { RestaurantDto } from '../../models/restaurant.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './home.html',
  styleUrls: ['./home.css'],
})
export class Home {
  query = '';
  suggestions: RestaurantDto[] = [];
  showSuggestions = false;
  loading = false;
  activeIndex = -1;

  private allRestaurants: RestaurantDto[] = [];
  private loaded = false;
  private loadingAll = false;

  constructor(
    private restaurantService: RestaurantService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  /** Lazily load the restaurant list once, the first time the user interacts. */
  private ensureLoaded(): void {
    // Suggestions require the (auth-gated) restaurant API. Skip for anonymous
    // visitors so we don't trigger a 401 that would bounce them to /login.
    if (this.loaded || this.loadingAll || !this.authService.isLoggedIn()) {
      return;
    }
    this.loadingAll = true;
    this.loading = true;
    this.restaurantService.getAllRestaurants().pipe(
      catchError(() => of([] as RestaurantDto[]))
    ).subscribe((list) => {
      this.allRestaurants = list ?? [];
      this.loaded = true;
      this.loadingAll = false;
      this.loading = false;
      this.updateSuggestions();
      // Zoneless app: async callbacks don't auto-trigger change detection.
      try { this.cdr.detectChanges(); } catch (e) { /* noop */ }
    });
  }

  onFocus(): void {
    this.ensureLoaded();
    if (this.query.trim()) {
      this.updateSuggestions();
    }
  }

  onInput(): void {
    this.ensureLoaded();
    this.activeIndex = -1;
    this.updateSuggestions();
  }

  private updateSuggestions(): void {
    const q = this.query.trim().toLowerCase();
    if (!q) {
      this.suggestions = [];
      this.showSuggestions = false;
      return;
    }
    this.suggestions = this.allRestaurants
      .filter((r) =>
        (r.name || '').toLowerCase().includes(q) ||
        (r.address || '').toLowerCase().includes(q)
      )
      .slice(0, 6);
    this.showSuggestions = true;
  }

  /** Navigate to a chosen restaurant's detail page. */
  select(restaurant: RestaurantDto): void {
    this.showSuggestions = false;
    this.query = restaurant.name;
    if (restaurant.id) {
      this.router.navigate(['/restaurants', restaurant.id]);
    } else {
      this.router.navigate(['/restaurants']);
    }
  }

  /** Triggered by the Search button / Enter key. */
  search(): void {
    if (this.suggestions.length > 0) {
      const choice = this.activeIndex >= 0 ? this.suggestions[this.activeIndex] : this.suggestions[0];
      this.select(choice);
      return;
    }
    // No match (or not loaded yet) -> fall back to the full listing.
    this.showSuggestions = false;
    const q = this.query.trim();
    this.router.navigate(['/restaurants'], q ? { queryParams: { q } } : {});
  }

  onSubmit(event: Event): void {
    event.preventDefault();
    this.search();
  }

  onKeydown(event: KeyboardEvent): void {
    if (!this.showSuggestions || this.suggestions.length === 0) {
      return;
    }
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.activeIndex = (this.activeIndex + 1) % this.suggestions.length;
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.activeIndex = (this.activeIndex - 1 + this.suggestions.length) % this.suggestions.length;
    } else if (event.key === 'Escape') {
      this.showSuggestions = false;
      this.activeIndex = -1;
    }
  }

  /** Hide the dropdown on blur, but late enough for a click/mousedown to register. */
  hideSuggestionsSoon(): void {
    setTimeout(() => (this.showSuggestions = false), 150);
  }
}
