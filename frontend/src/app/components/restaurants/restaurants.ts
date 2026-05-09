import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { RestaurantService } from '../../services/restaurant.service';
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
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadRestaurants();
    this.loadTopRated();
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
}
