import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RestaurantService } from '../../services/restaurant.service';
import { RestaurantDto } from '../../models/restaurant.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-restaurant-form',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './restaurant-form.html',
  styleUrl: './restaurant-form.css',
})
export class RestaurantForm implements OnInit {
  restaurant: RestaurantDto = { name: '', address: '', phone: '', website: '', schedule: '' };
  isEditMode = false;
  restaurantId: string = '';
  loading = false;

  constructor(
    private restaurantService: RestaurantService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.restaurantId = id;
      this.loadRestaurant();
    }
  }

  loadRestaurant() {
    this.restaurantService.getRestaurant(this.restaurantId).subscribe((data) => {
      this.restaurant = data;
    });
  }

  saveRestaurant() {
    this.loading = true;
    if (this.isEditMode) {
      this.restaurantService.updateRestaurant(this.restaurantId, this.restaurant).subscribe(() => {
        this.router.navigate(['/restaurants', this.restaurantId]);
      });
    } else {
      this.restaurantService.addRestaurant(this.restaurant).subscribe(() => {
        this.router.navigate(['/restaurants']);
      });
    }
  }
}
