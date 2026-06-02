import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';

import { AdminRoot } from './app/admin/admin';
import { AuthInterceptor } from './app/services/auth.interceptor';

// Pick the JWT out of the URL fragment (#token=...) handed in by the shell,
// stash it in localStorage so the auth interceptor can attach it to API calls,
// then strip the fragment from the URL bar so it doesn't get logged anywhere.
const hash = window.location.hash;
if (hash.startsWith('#token=')) {
  const token = decodeURIComponent(hash.substring('#token='.length));
  if (token) {
    localStorage.setItem('access_token', token);
  }
  history.replaceState(null, '', window.location.pathname + window.location.search);
}

const config: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ]
};

bootstrapApplication(AdminRoot, config).catch((err) => console.error(err));
