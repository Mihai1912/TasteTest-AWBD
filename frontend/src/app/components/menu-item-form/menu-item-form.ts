import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MenuItemService } from '../../services/menu-item.service';
import { MenuItemDto } from '../../models/menu-item.model';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-menu-item-form',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './menu-item-form.html',
  styleUrls: ['./menu-item-form.css'],
})
export class MenuItemForm implements OnInit {
  item: MenuItemDto = { name: '', price: 0, description: '' };
  isEditMode = false;
  menuItemId: string = '';
  menuId: string = '';
  loading = false;

  constructor(
    private menuItemService: MenuItemService,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

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
    this.menuItemService.getMenuItem(this.menuItemId).subscribe((data) => {
      this.item = data;
      this.loading = false;
    }, (err) => {
      console.error('[MenuItemForm] Failed to load item', err);
      this.loading = false;
    });
  }

  save() {
    this.loading = true;
    if (this.isEditMode) {
      this.menuItemService.updateMenuItem(this.menuItemId, this.item).subscribe(() => {
        // after update navigate back to the containing menu
        const targetMenu = (this.item as any).menuId || this.menuId;
        this.router.navigate(['/menus', targetMenu]);
      }, (err) => {
        console.error('[MenuItemForm] Failed to update', err);
        this.loading = false;
      });
    } else {
      this.menuItemService.addMenuItem(this.menuId, this.item).subscribe(() => {
        this.router.navigate(['/menus', this.menuId]);
      }, (err) => {
        console.error('[MenuItemForm] Failed to add', err);
        this.loading = false;
      });
    }
  }
}

