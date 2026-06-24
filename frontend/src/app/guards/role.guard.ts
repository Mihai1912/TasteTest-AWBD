import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Route guard that restricts access to users holding at least one of the roles
 * declared on the route via `data: { roles: [...] }`.
 *
 * Example:
 *   { path: 'restaurants/add', component: RestaurantForm,
 *     canActivate: [AuthGuard, RoleGuard],
 *     data: { roles: ['ADMIN', 'RESTAURANT_OWNER'] } }
 *
 * Mirrors the backend `@PreAuthorize("hasAuthority('RESTAURANT_OWNER') or hasAuthority('ADMIN')")`.
 */
@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  private readonly router: Router;
  private readonly authService: AuthService;

  constructor(router: Router, authService: AuthService) {
    this.router = router;
    this.authService = authService;
  }

  canActivate(route: ActivatedRouteSnapshot): boolean {
    // Not authenticated -> behave like AuthGuard and send to login.
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return false;
    }

    const requiredRoles: string[] = (route.data?.['roles'] as string[]) ?? [];

    // No role requirement declared -> authentication alone is enough.
    if (requiredRoles.length === 0) {
      return true;
    }

    const allowed = requiredRoles.some((role) => {
      const normalized = role.toUpperCase();
      if (normalized === 'ADMIN' || normalized === 'ROLE_ADMIN') {
        return this.authService.isAdmin();
      }
      return this.authService.hasRole(role);
    });

    if (allowed) {
      return true;
    }

    // Authenticated but lacking permission -> bounce back to the listing.
    this.router.navigate(['/restaurants']);
    return false;
  }
}
