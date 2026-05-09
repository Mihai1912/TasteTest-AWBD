import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MenuItemService } from '../../services/menu-item.service';
import { MenuItemDto } from '../../models/menu-item.model';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ChangeDetectorRef } from '@angular/core';
import { catchError, finalize, timeout } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-menu-item-form',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './menu-item-form.html',
  styleUrls: ['./menu-item-form.css'],
})
export class MenuItemForm implements OnInit {
  private readonly menuItemService: MenuItemService;
  private readonly route: ActivatedRoute;
  private readonly router: Router;
  private readonly authService: AuthService;
  private readonly cdr: ChangeDetectorRef;

  item: MenuItemDto = { name: '', price: 0, description: '' };
  isEditMode = false;
  menuItemId: string = '';
  menuId: string = '';
  loading = false;
  error: string | null = null;

  constructor(
    menuItemService: MenuItemService,
    route: ActivatedRoute,
    router: Router,
    authService: AuthService,
    cdr: ChangeDetectorRef
  ) {
    this.menuItemService = menuItemService;
    this.route = route;
    this.router = router;
    this.authService = authService;
    this.cdr = cdr;
  }

  ngOnInit() {
    // Ensure owner or admin role
    if (!this.authService.hasRole('RESTAURANT_OWNER') && !this.authService.isAdmin()) {
      alert('Only restaurant owners or admins can manage menu items');
      this.router.navigate(['/home']);
      return;
    }

    const menuItemId = this.route.snapshot.paramMap.get('id');
    const menuId = this.route.snapshot.paramMap.get('menuId');
    if (menuItemId) {
      this.isEditMode = true;
      this.menuItemId = menuItemId;
      this.loadItem();
    } else if (menuId) {
      this.menuId = menuId;
    }
  }

  loadItem() {
    this.loading = true;
    this.error = null;
    this.menuItemService.getMenuItem(this.menuItemId).pipe(
      timeout(8000),
      catchError((err) => {
        console.error('[MenuItemForm] Failed to load item', err);
        this.error = err?.status === 401
          ? 'Your session has expired. Please login again.'
          : err?.status === 404
            ? 'Menu item not found.'
            : 'Could not load menu item.';
        return of(null);
      }),
      finalize(() => {
        this.loading = false;
        try { this.cdr.detectChanges(); } catch (e) { /* noop */ }
      })
    ).subscribe((data) => {
      if (!data) return;
      this.item = data;
      if (data.menuId) {
        this.menuId = data.menuId;
      }
      try { this.cdr.detectChanges(); } catch (e) { /* noop */ }
    });
  }

  save() {
    this.loading = true;
    this.error = null;
    if (this.isEditMode) {
      this.menuItemService.updateMenuItem(this.menuItemId, this.item).pipe(
        timeout(8000),
        catchError((err) => {
          console.error('[MenuItemForm] Failed to update', err);
          this.error = err?.status === 401
            ? 'Your session has expired. Please login again.'
            : 'Could not update menu item.';
          return of(null);
        }),
        finalize(() => {
          this.loading = false;
          try { this.cdr.detectChanges(); } catch (e) { /* noop */ }
        })
      ).subscribe((updated) => {
        if (!updated) return;
        const targetMenu = updated.menuId || this.item.menuId || this.menuId;
        this.router.navigate(['/menus', targetMenu]);
      });
    } else {
      this.menuItemService.addMenuItem(this.menuId, this.item).pipe(
        timeout(8000),
        catchError((err) => {
          console.error('[MenuItemForm] Failed to add', err);
          this.error = err?.status === 401
            ? 'Your session has expired. Please login again.'
            : 'Could not add menu item.';
          return of(null);
        }),
        finalize(() => {
          this.loading = false;
          try { this.cdr.detectChanges(); } catch (e) { /* noop */ }
        })
      ).subscribe((created) => {
        if (!created) return;
        const targetMenu = created.menuId || this.menuId;
        this.router.navigate(['/menus', targetMenu]);
      });
    }
  }
}

