import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FeedbackService } from '../../services/feedback.service';
import { AuthService } from '../../services/auth.service';
import { FeedbackDto } from '../../models/feedback.model';
import { FeedbackAdminDto } from '../../models/admin.model';

@Component({
  selector: 'app-feedback-form',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './feedback-form.html',
  styleUrls: ['./feedback-form.css'],
})
export class FeedbackForm implements OnInit {
  // Submission form (regular users)
  feedback: FeedbackDto = { feedbackType: '', experience: '', comment: '' };
  loading = false;
  message = '';

  // Admin inbox
  feedbackEntries: FeedbackAdminDto[] = [];
  loadingFeedback = false;
  feedbackError = '';

  constructor(
    private feedbackService: FeedbackService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (this.isAdmin) {
      this.loadAllFeedback();
    }
  }

  get isAdmin(): boolean {
    try {
      return this.authService.isAdmin();
    } catch {
      return false;
    }
  }

  loadAllFeedback(): void {
    this.loadingFeedback = true;
    this.feedbackError = '';
    this.feedbackService.getAllFeedback().subscribe({
      next: (entries) => {
        this.feedbackEntries = entries ?? [];
        this.loadingFeedback = false;
        // Zoneless app: async callbacks don't auto-trigger change detection.
        try { this.cdr.detectChanges(); } catch (e) { /* noop */ }
      },
      error: (error) => {
        console.error('[FeedbackForm] Could not load feedback', error);
        this.feedbackError = 'Could not load feedback. Please try again.';
        this.loadingFeedback = false;
        try { this.cdr.detectChanges(); } catch (e) { /* noop */ }
      },
    });
  }

  submitFeedback() {
    this.loading = true;
    this.feedbackService.addFeedback(this.feedback).subscribe(
      () => {
        this.message = 'Feedback submitted successfully!';
        this.loading = false;
        try { this.cdr.detectChanges(); } catch (e) { /* noop */ }
        setTimeout(() => this.router.navigate(['/restaurants']), 2000);
      },
      (error) => {
        this.message = 'Error submitting feedback';
        this.loading = false;
        try { this.cdr.detectChanges(); } catch (e) { /* noop */ }
      }
    );
  }

  /** CSS modifier class for an experience value (colour-codes the badge). */
  experienceClass(experience: string): string {
    switch ((experience || '').toUpperCase()) {
      case 'GOOD': return 'exp-good';
      case 'BAD': return 'exp-bad';
      case 'AVERAGE': return 'exp-average';
      default: return '';
    }
  }

  /** Title-case an enum-style value, e.g. "FEATURE" -> "Feature". */
  pretty(value: string): string {
    if (!value) return '';
    return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
  }
}
