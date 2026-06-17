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

  getCardGradient(i: number): string {
    return this.cardGradients[i % this.cardGradients.length];
  }
}
