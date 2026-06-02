import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { environment } from '../../../environments/environment';

/**
 * Thin loader that embeds the standalone admin micro-frontend in an iframe.
 *
 * The admin UI itself lives in /mfe-admin - a separate Angular workspace,
 * independently built and deployed. The shell only knows the MFE's URL and
 * hands off the current JWT via URL fragment so the MFE can call the API
 * gateway as the same authenticated user.
 */
@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin.html',
  styleUrls: ['./admin.css'],
})
export class Admin implements OnInit, OnDestroy {
  iframeUrl = signal<SafeResourceUrl | null>(null);
  mfeOrigin = '';

  constructor(private sanitizer: DomSanitizer) {}

  ngOnInit(): void {
    const token = localStorage.getItem('access_token') ?? '';
    // URL fragments never reach the server - safer than a query param for
    // passing the JWT to the embedded MFE.
    const url = `${environment.adminMfeUrl}/#token=${encodeURIComponent(token)}`;
    this.mfeOrigin = environment.adminMfeUrl;
    this.iframeUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(url));
  }

  ngOnDestroy(): void {
    this.iframeUrl.set(null);
  }
}
