import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MenuService } from '../../services/menu.service';
import { MenuDto } from '../../models/menu.model';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-menu-form',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './menu-form.html',
  styleUrls: ['./menu-form.css'],
})
export class MenuForm implements OnInit {
  menu: MenuDto = { id: '', name: '' };
  isEditMode = false;
  menuId: string = '';
  loading = false;
  restaurantId: string = '';
  restaurantName: string = '';

  constructor(
    private menuService: MenuService,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Only admins may edit menus
    if (!this.authService.isAdmin()) {
      // navigate back to menu detail or home
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.router.navigate(['/menus', id]);
      } else {
        this.router.navigate(['/home']);
      }
      return;
    }

    const id = this.route.snapshot.paramMap.get('id');
    const rid = this.route.snapshot.paramMap.get('restaurantId');
    const rname = this.route.snapshot.queryParamMap.get('restaurantName');

    if (id) {
      // Edit mode
      this.isEditMode = true;
      this.menuId = id;
      this.loadMenu();
    } else if (rid) {
      // Add mode
      this.isEditMode = false;
      this.restaurantId = rid;
      this.restaurantName = rname || '';
    } else {
      // Missing params, redirect
      this.router.navigate(['/home']);
    }
  }

  loadMenu() {
    this.menuService.getMenu(this.menuId).subscribe((data) => {
      this.menu = data;
    });
  }

  saveMenu() {
    const menuName = (this.menu.name || '').trim();
    if (!menuName) {
      alert('Menu name is required.');
      return;
    }

    this.loading = true;
    if (this.isEditMode) {
      this.menuService.updateMenu(this.menuId, menuName).subscribe(() => {
        this.router.navigate(['/menus', this.menuId]);
      }, (err) => {
        console.error('[MenuForm] Failed to update menu', err);
        this.loading = false;
      });
    } else {
      // Add mode
      this.menuService.addMenu(menuName, this.restaurantName).subscribe((created) => {
        // Go directly to the created menu so admin can continue with menu items.
        if (created?.id) {
          this.router.navigate(['/menus', created.id]);
        } else {
          this.router.navigate(['/restaurants', this.restaurantId]);
        }
      }, (err) => {
        console.error('[MenuForm] Failed to add menu', err);
        this.loading = false;
      });
    }
  }
}

