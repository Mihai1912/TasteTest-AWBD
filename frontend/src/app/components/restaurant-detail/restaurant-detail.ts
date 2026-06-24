import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { RestaurantService } from '../../services/restaurant.service';
import { ReviewService } from '../../services/review.service';
import { ReplyService } from '../../services/reply.service';
import { MenuService } from '../../services/menu.service';
import { AuthService } from '../../services/auth.service';
import { RestaurantDto } from '../../models/restaurant.model';
import { ReviewIdDto } from '../../models/review.model';
import { ReplyDto } from '../../models/reply.model';
import { MenuDto } from '../../models/menu.model';
import { of } from 'rxjs';
import { catchError, finalize, timeout } from 'rxjs/operators';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-restaurant-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './restaurant-detail.html',
  styleUrls: ['./restaurant-detail.css'],
})
export class RestaurantDetail implements OnInit {
  private readonly route: ActivatedRoute;
  private readonly restaurantService: RestaurantService;
  private readonly reviewService: ReviewService;
  private readonly replyService: ReplyService;
  private readonly menuService: MenuService;
  private readonly authService: AuthService;
  private readonly cdr: ChangeDetectorRef;
  private readonly router: Router;

  restaurant: RestaurantDto | null = null;
  reviews: ReviewIdDto[] = [];
  repliesMap: Record<string, ReplyDto[]> = {};
  replyDrafts: Record<string, string> = {};
  replyEditDrafts: Record<string, string> = {};
  replyEditing: Record<string, boolean> = {};
  replyLoading: Record<string, boolean> = {};
  replySubmitting: Record<string, boolean> = {};
  replyUpdateSubmitting: Record<string, boolean> = {};
  replyDeleteSubmitting: Record<string, boolean> = {};
  replyErrors: Record<string, string> = {};
  menus: MenuDto[] = [];
  // no longer embed menu items here; menus link to a dedicated menu page
  rating: number = 0;
  restaurantId: string = '';
  loading = false;
  error: string | null = null;

  constructor(
    route: ActivatedRoute,
    restaurantService: RestaurantService,
    reviewService: ReviewService,
    replyService: ReplyService,
    menuService: MenuService,
    authService: AuthService,
    cdr: ChangeDetectorRef,
    router: Router
  ) {
    this.route = route;
    this.restaurantService = restaurantService;
    this.reviewService = reviewService;
    this.replyService = replyService;
    this.menuService = menuService;
    this.authService = authService;
    this.cdr = cdr;
    this.router = router;
  }

  ngOnInit() {
    this.log('Component initialized');
    this.restaurantId = this.route.snapshot.paramMap.get('id') || '';
    this.log('Route parameter read', { restaurantId: this.restaurantId });
    if (!this.restaurantId) {
      this.error = 'Missing restaurant id';
      this.warn('Missing restaurant id in route');
      return;
    }

    this.loadRestaurant();
  }

  get isAdmin(): boolean {
    try {
      return this.authService.isAdmin();
    } catch (e) {
      return false;
    }
  }

  get isOwner(): boolean {
    try {
      return this.authService.hasRole('RESTAURANT_OWNER');
    } catch (e) {
      return false;
    }
  }

  get canManageReplies(): boolean {
    return this.isAdmin || this.isOwner;
  }

  loadRestaurant() {
    this.loading = true;
    this.error = null;
    this.log('Loading restaurant details', { restaurantId: this.restaurantId });

    this.restaurantService.getRestaurant(this.restaurantId).pipe(
      timeout(10000), // 10 second timeout
      catchError((err) => {
        this.errorLog('Failed to load restaurant', err);
        if (err.name === 'TimeoutError') {
          this.error = 'Request timed out. Please check your connection and try again.';
        } else if (err.status === 401) {
          this.error = 'Your session has expired. Please login again.';
        } else if (err.status === 403) {
          this.error = 'You do not have permission to view this restaurant.';
        } else if (err.status === 404) {
          this.error = 'Restaurant not found.';
        } else if (err.status === 0) {
          this.error = 'Network error. Please check your connection.';
        } else {
          this.error = `Error loading restaurant: ${err.status ? err.status + ' ' : ''}${err.statusText || err.message || 'Unknown error'}`;
        }
        return of(null);
      }),
      finalize(() => {
        this.loading = false;
        // Ensure view updates after loading flag changes
        try { this.cdr.detectChanges(); } catch(e) { /* noop */ }
      })
    ).subscribe((restaurant) => {
      this.restaurant = restaurant;
      try { this.cdr.detectChanges(); } catch(e) { /* noop */ }
      this.log('Restaurant load finished', { found: !!restaurant });
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
    this.log('Loading restaurant rating', { restaurantId: this.restaurantId });
    this.restaurantService.getRatings(this.restaurantId).pipe(
      timeout(8000), // 8 second timeout
      catchError((err) => {
        this.errorLog('Failed to load ratings', err);
        return of(0);
      })
    ).subscribe((rating) => {
      const parsedRating = Number(rating);
      this.rating = Number.isFinite(parsedRating) ? parsedRating : 0;
      try { this.cdr.detectChanges(); } catch(e) { /* noop */ }
      this.log('Loaded restaurant rating', { rating: this.rating });
    });
  }

  loadReviews() {
    this.log('Loading restaurant reviews', { restaurantId: this.restaurantId });
    this.reviewService.getRestaurantReviews(this.restaurantId).pipe(
      timeout(8000), // 8 second timeout
      catchError((err) => {
        this.errorLog('Failed to load reviews', err);
        return of([] as ReviewIdDto[]);
      })
    ).subscribe((reviews) => {
      this.reviews = reviews;
      try { this.cdr.detectChanges(); } catch(e) { /* noop */ }
      this.log('Loaded restaurant reviews', { count: reviews.length });
      reviews.forEach((review) => {
        if (review.id) {
          this.loadReplies(review.id);
        }
      });
    });
  }

  loadReplies(reviewId: string) {
    this.replyLoading[reviewId] = true;
    this.replyErrors[reviewId] = '';
    this.log('Loading replies for review', { reviewId });

    this.replyService.getAllRepliesOfReview(reviewId).pipe(
      timeout(8000),
      catchError((err) => {
        this.errorLog('Failed to load replies', { reviewId, err });
        this.replyErrors[reviewId] = 'Could not load replies.';
        return of([] as ReplyDto[]);
      }),
      finalize(() => {
        this.replyLoading[reviewId] = false;
        try { this.cdr.detectChanges(); } catch(e) { /* noop */ }
      })
    ).subscribe((replies) => {
      this.repliesMap[reviewId] = replies;
      try { this.cdr.detectChanges(); } catch(e) { /* noop */ }
      this.log('Loaded replies for review', { reviewId, count: replies.length });
    });
  }

  submitReply(reviewId: string) {
    const text = (this.replyDrafts[reviewId] || '').trim();
    if (!text) {
      this.replyErrors[reviewId] = 'Reply text is required.';
      return;
    }

    this.replySubmitting[reviewId] = true;
    this.replyErrors[reviewId] = '';
    this.replyService.addReply(reviewId, { text }).pipe(
      timeout(8000),
      catchError((err) => {
        this.errorLog('Failed to add reply', { reviewId, err });
        this.replyErrors[reviewId] = err?.status === 401
          ? 'Your session has expired. Please login again.'
          : 'Could not submit reply.';
        return of(null);
      }),
      finalize(() => {
        this.replySubmitting[reviewId] = false;
        try { this.cdr.detectChanges(); } catch(e) { /* noop */ }
      })
    ).subscribe((reply) => {
      if (!reply) return;
      this.replyDrafts[reviewId] = '';
      this.loadReplies(reviewId);
    });
  }

  startReplyEdit(reply: ReplyDto) {
    if (!reply.id) return;
    this.replyEditDrafts[reply.id] = reply.text;
    this.replyEditing[reply.id] = true;
    this.replyErrors[this.getReplyReviewKey(reply)] = '';
  }

  cancelReplyEdit(reply: ReplyDto) {
    if (!reply.id) return;
    this.replyEditing[reply.id] = false;
    delete this.replyEditDrafts[reply.id];
  }

  saveReply(reviewId: string, reply: ReplyDto) {
    if (!reply.id) return;
    const text = (this.replyEditDrafts[reply.id] || '').trim();
    if (!text) {
      this.replyErrors[reviewId] = 'Reply text is required.';
      return;
    }

    this.replyUpdateSubmitting[reply.id] = true;
    this.replyErrors[reviewId] = '';
    this.replyService.updateReply(reply.id, { text }).pipe(
      timeout(8000),
      catchError((err) => {
        this.errorLog('Failed to update reply', { reviewId, replyId: reply.id, err });
        this.replyErrors[reviewId] = err?.status === 401
          ? 'Your session has expired. Please login again.'
          : 'Could not update reply.';
        return of(null);
      }),
      finalize(() => {
        this.replyUpdateSubmitting[reply.id!] = false;
        try { this.cdr.detectChanges(); } catch(e) { /* noop */ }
      })
    ).subscribe((updated) => {
      if (!updated) return;
      this.replyEditing[reply.id!] = false;
      this.loadReplies(reviewId);
    });
  }

  deleteReply(reviewId: string, reply: ReplyDto) {
    if (!reply.id) return;
    if (!confirm('Delete this reply?')) return;

    this.replyDeleteSubmitting[reply.id] = true;
    this.replyErrors[reviewId] = '';
    this.replyService.deleteReply(reply.id).pipe(
      timeout(8000),
      catchError((err) => {
        this.errorLog('Failed to delete reply', { reviewId, replyId: reply.id, err });
        this.replyErrors[reviewId] = err?.status === 401
          ? 'Your session has expired. Please login again.'
          : 'Could not delete reply.';
        return of(null);
      }),
      finalize(() => {
        this.replyDeleteSubmitting[reply.id!] = false;
        try { this.cdr.detectChanges(); } catch(e) { /* noop */ }
      })
    ).subscribe((deleted) => {
      if (!deleted) return;
      this.loadReplies(reviewId);
    });
  }

  private getReplyReviewKey(reply: ReplyDto): string {
    return Object.keys(this.repliesMap).find((reviewId) =>
      (this.repliesMap[reviewId] || []).some((item) => item.id === reply.id)
    ) || '';
  }

  loadMenus() {
    this.log('Loading restaurant menus', { restaurantId: this.restaurantId });
    this.menuService.getRestaurantMenus(this.restaurantId).pipe(
      timeout(8000), // 8 second timeout
      catchError((err) => {
        this.errorLog('Failed to load menus', err);
        return of([] as MenuDto[]);
      })
    ).subscribe((menus) => {
      this.menus = menus;
      try { this.cdr.detectChanges(); } catch(e) { /* noop */ }
      this.log('Loaded restaurant menus', { count: menus.length });
      // preload nothing by default; menu items are loaded on demand
    });
  }

  addMenu() {
    if (!this.restaurant) return;
    this.router.navigate(
      ['/restaurants', this.restaurantId, 'menus', 'add'],
      { queryParams: { restaurantName: this.restaurant.name } }
    );
  }

  // ----------------------- View helpers -----------------------

  /** Percentage (0-100) for a 0-5 rating, used to clip the filled bubbles overlay. */
  bubbleFillPercent(value: number): number {
    const v = Number.isFinite(value) ? value : 0;
    return Math.max(0, Math.min(100, (v / 5) * 100));
  }

  /** Up to two uppercase initials for an avatar badge. */
  getInitials(name?: string | null): string {
    const trimmed = (name || '').trim();
    if (!trimmed) return '?';
    const parts = trimmed.split(/\s+/).filter(Boolean);
    const initials = parts.length === 1
      ? parts[0].slice(0, 2)
      : parts[0][0] + parts[parts.length - 1][0];
    return initials.toUpperCase();
  }

  private readonly avatarGradients = [
    'linear-gradient(135deg, #ffd166, #ef476f)',
    'linear-gradient(135deg, #06d6a0, #118ab2)',
    'linear-gradient(135deg, #8338ec, #3a86ff)',
    'linear-gradient(135deg, #f72585, #b5179e)',
    'linear-gradient(135deg, #ff9f1c, #ffbf69)',
    'linear-gradient(135deg, #2ec4b6, #20a4f3)',
    'linear-gradient(135deg, #e63946, #f77f00)',
    'linear-gradient(135deg, #43aa8b, #f9c74f)',
  ];

  /** Deterministic gradient for an avatar based on a seed string. */
  getAvatarGradient(seed?: string | null): string {
    const s = (seed || '').trim();
    let hash = 0;
    for (let i = 0; i < s.length; i++) {
      hash = (hash * 31 + s.charCodeAt(i)) >>> 0;
    }
    return this.avatarGradients[hash % this.avatarGradients.length];
  }

  /** Strip protocol/trailing slash for a tidier website label. */
  displayWebsite(url?: string | null): string {
    return (url || '').replace(/^https?:\/\//i, '').replace(/\/$/, '');
  }

  private log(message: string, data?: unknown): void {
    console.debug(`[RestaurantDetail] ${message}`, data ?? '');
  }

  private warn(message: string, data?: unknown): void {
    console.warn(`[RestaurantDetail] ${message}`, data ?? '');
  }

  private errorLog(message: string, data?: unknown): void {
    console.error(`[RestaurantDetail] ${message}`, data ?? '');
  }
}
