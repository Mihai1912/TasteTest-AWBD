import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MenuItemService } from '../../services/menu-item.service';
import { MenuItemDto } from '../../models/menu-item.model';
import { of } from 'rxjs';
import { catchError, finalize, timeout } from 'rxjs/operators';
import { Router } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-menu-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './menu-detail.html',
  styleUrls: ['./menu-detail.css'],
})
export class MenuDetail implements OnInit {
  menuId: string = '';
  items: MenuItemDto[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private menuItemService: MenuItemService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  get isAdmin(): boolean {
    try {
      return this.authService.isAdmin();
    } catch (e) {
      return false;
    }
  }

  ngOnInit() {
    this.menuId = this.route.snapshot.paramMap.get('id') || '';
    this.log('ngOnInit', { menuId: this.menuId, isAdmin: this.isAdmin, isOwner: this.isOwner });
    this.loadItems();
  }

  loadItems() {
    this.loading = true;
    this.error = null;
    this.log('Loading menu items', { menuId: this.menuId });
    this.menuItemService.getMenuItemsByMenu(this.menuId).pipe(
      timeout(8000),
      catchError((err) => {
        this.error = this.formatError(err);
        this.errorLog('Failed to load menu items', err);
        return of([] as MenuItemDto[]);
      }),
      finalize(() => {
        this.loading = false;
      })
    ).subscribe((data) => {
      this.items = data || [];
      this.log('Menu items loaded', { count: this.items.length });
      try { this.cdr.detectChanges(); } catch(e) { /* noop */ }
    });
  }

  private formatError(err: any): string {
    try {
      if (!err) return 'Unknown error';
      if (err.status === 0) return 'Network error';
      if (err.status === 401) return 'Unauthorized - please login';
      if (err.status === 403) return 'Forbidden - insufficient permissions';
      if (err.status === 404) return 'Not found';
      return err.message || (err.statusText ? `${err.status} ${err.statusText}` : 'Unknown error');
    } catch (e) {
      return 'Unknown error';
    }
  }

  private log(message: string, data?: unknown): void {
    // use log so messages appear even if debug level is filtered out in the console
    console.log(`[MenuDetail] ${message}`, data ?? '');
  }

  private errorLog(message: string, data?: unknown): void {
    console.error(`[MenuDetail] ${message}`, data ?? '');
  }

  get isOwner(): boolean {
    try { return this.authService.hasRole('RESTAURANT_OWNER'); } catch(e) { return false; }
  }

  get canManageItems(): boolean {
    return this.isOwner || this.isAdmin;
  }

  addItem() {
    // navigate to add item form for this menu
    this.router.navigate(['/menus', this.menuId, 'items', 'add']);
  }

  deleteItem(itemId: string) {
    if (!confirm('Delete this menu item?')) return;
    this.menuItemService.deleteMenuItem(itemId).subscribe(() => {
      this.loadItems();
    }, (err) => {
      console.error('[MenuDetail] Failed to delete item', err);
      alert('Failed to delete item');
    });
  }
}
