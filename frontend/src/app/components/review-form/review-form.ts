import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ReviewService } from '../../services/review.service';
import { ReviewDto } from '../../models/review.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-review-form',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './review-form.html',
  styleUrls: ['./review-form.css'],
})
export class ReviewForm implements OnInit {
  review: ReviewDto = { comment: '', rating: 5, urserName: '' };
  restaurantId: string = '';
  loading = false;

  constructor(
    private reviewService: ReviewService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.restaurantId = this.route.snapshot.paramMap.get('id') || '';
  }

  submitReview() {
    this.loading = true;
    this.reviewService.addReview(this.restaurantId, this.review, this.review.rating).subscribe(() => {
      this.router.navigate(['/restaurants', this.restaurantId]);
    });
  }
}
