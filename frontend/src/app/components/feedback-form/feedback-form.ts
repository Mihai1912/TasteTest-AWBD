import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FeedbackService } from '../../services/feedback.service';
import { FeedbackDto } from '../../models/feedback.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-feedback-form',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './feedback-form.html',
  styleUrls: ['./feedback-form.css'],
})
export class FeedbackForm {
  feedback: FeedbackDto = { feedbackType: '', experience: '', comment: '' };
  loading = false;
  message = '';

  constructor(private feedbackService: FeedbackService, private router: Router) {}

  submitFeedback() {
    this.loading = true;
    this.feedbackService.addFeedback(this.feedback).subscribe(
      () => {
        this.message = 'Feedback submitted successfully!';
        this.loading = false;
        setTimeout(() => this.router.navigate(['/restaurants']), 2000);
      },
      (error) => {
        this.message = 'Error submitting feedback';
        this.loading = false;
      }
    );
  }
}
