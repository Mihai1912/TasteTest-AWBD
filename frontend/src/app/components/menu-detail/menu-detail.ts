import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MenuItemService } from '../../services/menu-item.service';
import { MenuItemDto } from '../../models/menu-item.model';

@Component({
  selector: 'app-menu-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './menu-detail.html',
  styleUrl: './menu-detail.css',
})
export class MenuDetail implements OnInit {
  menuId: string = '';
  items: MenuItemDto[] = [];

  constructor(
    private route: ActivatedRoute,
    private menuItemService: MenuItemService
  ) {}

  ngOnInit() {
    this.menuId = this.route.snapshot.paramMap.get('id') || '';
    this.loadItems();
  }

  loadItems() {
    this.menuItemService.getMenuItemsByMenu(this.menuId).subscribe((data) => {
      this.items = data;
    });
  }
}
