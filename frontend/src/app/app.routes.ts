import { Routes } from '@angular/router';
import { Home } from './components/home/home';
import { Login } from './components/login/login';
import { Register } from './components/register/register';
import { Restaurants } from './components/restaurants/restaurants';
import { RestaurantDetail } from './components/restaurant-detail/restaurant-detail';
import { RestaurantForm } from './components/restaurant-form/restaurant-form';
import { ReviewForm } from './components/review-form/review-form';
import { MenuDetail } from './components/menu-detail/menu-detail';
import { FeedbackForm } from './components/feedback-form/feedback-form';
import { Admin } from './components/admin/admin';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: Home },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'restaurants/add', component: RestaurantForm, canActivate: [AuthGuard] },
  { path: 'restaurants/:id/edit', component: RestaurantForm, canActivate: [AuthGuard] },
  { path: 'restaurants/:id/review', component: ReviewForm, canActivate: [AuthGuard] },
  { path: 'restaurants/:id', component: RestaurantDetail, canActivate: [AuthGuard] },
  { path: 'restaurants', component: Restaurants, canActivate: [AuthGuard] },
  { path: 'menus/:id', component: MenuDetail, canActivate: [AuthGuard] },
  { path: 'feedback', component: FeedbackForm, canActivate: [AuthGuard] },
  { path: 'admin', component: Admin, canActivate: [AuthGuard] },
];
