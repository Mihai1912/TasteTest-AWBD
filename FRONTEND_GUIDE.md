# Angular Frontend Implementation - Complete Guide

## ✅ What Was Created

### **Core Setup**
- ✅ Dev proxy configuration (`proxy.conf.json`) - maps `/api` to `http://localhost:8090`
- ✅ JWT Auth Interceptor - automatically adds `Authorization: Bearer <token>` to all requests
- ✅ Auth Guard - protects routes that require authentication
- ✅ Environment configurations (dev & prod)

### **Data Models** (TypeScript Interfaces)
- ✅ Auth models (LoginDto, RegisterDto, LoginResponseDto)
- ✅ Restaurant model (name, address, phone, website, schedule)
- ✅ Review models (ReviewDto, ReviewIdDto with ratings and comments)
- ✅ Menu & MenuItem models
- ✅ Reply & Feedback models

### **Services** (One per entity)
- ✅ **AuthService** - login, register, logout, token validation
- ✅ **RestaurantService** - add/edit/delete/list restaurants, ratings, top-rated
- ✅ **ReviewService** - add/edit/delete reviews with ratings
- ✅ **MenuService** - manage menus
- ✅ **MenuItemService** - manage menu items
- ✅ **ReplyService** - add/edit replies to reviews
- ✅ **FeedbackService** - submit user feedback
- ✅ **UserService** - admin: view user reviews
- ✅ **RoleService** - admin: manage roles

### **Components**
- ✅ **Navbar** - navigation with auth status, login/logout buttons
- ✅ **Login** - email/password login form
- ✅ **Register** - new user registration
- ✅ **Restaurants** - list all & top-rated restaurants with filters
- ✅ **RestaurantDetail** - view single restaurant (info, rating, menus, reviews)
- ✅ **RestaurantForm** - add/edit restaurant (owner/admin only)
- ✅ **ReviewForm** - add reviews with 1-5 star rating
- ✅ **MenuDetail** - view menu items for a menu
- ✅ **FeedbackForm** - submit platform feedback
- ✅ **Admin** - admin panel (user reviews lookup, role management)
- ✅ **Home** - welcome page

### **Routes**
```
/home                    → Home page
/login                   → Login form
/register                → Register form
/restaurants             → List all/top-rated restaurants (guarded)
/restaurants/:id         → Single restaurant details (guarded)
/restaurants/add         → Add new restaurant (guarded)
/restaurants/:id/edit    → Edit restaurant (guarded)
/restaurants/:id/review  → Add review (guarded)
/menus/:id              → Menu items (guarded)
/feedback               → Send feedback (guarded)
/admin                  → Admin panel (guarded)
```

---

## 🚀 Running the App

### **1. Start the Spring Boot Backend**
```bash
cd /Users/mihai/Documents/facultate/awbd/TasteTest-AWDB
./mvnw spring-boot:run
```
Backend will run on `http://localhost:8090`

### **2. Start the Angular Frontend**
```bash
cd frontend
npm start
```
Frontend will run on `http://localhost:4200` with dev proxy to backend

---

## 📋 Testing Checklist

- [ ] Register a new user at `/register`
- [ ] Login with your credentials at `/login`
- [ ] View all restaurants at `/restaurants`
- [ ] Click a restaurant to see details, menus, and reviews
- [ ] Add a review with a rating
- [ ] Submit platform feedback at `/feedback`
- [ ] Logout (button in navbar)

---

## 🔐 Key Features

**Authentication:**
- JWT tokens stored in localStorage
- Auto-included in all API requests via interceptor
- Token validation on login

**Authorization:**
- Routes guarded - redirect to login if not authenticated
- Restaurant owner can add/edit restaurants
- Users can add reviews and feedback
- Admin panel for role management and user review lookup

**Error Handling:**
- Catch and display errors on forms
- Redirect on auth failures
- Graceful error messages

---

## 📁 Frontend Structure
```
frontend/
├── src/app/
│   ├── models/              # TypeScript interfaces
│   ├── services/            # API communication services
│   ├── interceptors/        # HTTP interceptor for JWT
│   ├── guards/              # Route protection
│   ├── components/          # UI components
│   │   ├── navbar/
│   │   ├── login/
│   │   ├── register/
│   │   ├── restaurants/
│   │   ├── restaurant-detail/
│   │   ├── restaurant-form/
│   │   ├── review-form/
│   │   ├── menu-detail/
│   │   ├── feedback-form/
│   │   ├── admin/
│   │   └── home/
│   ├── app.routes.ts        # Route configuration
│   ├── app.config.ts        # App setup (interceptor)
│   └── app.ts               # Root component
├── proxy.conf.json          # Dev proxy config
├── angular.json             # Angular CLI config
└── package.json             # Dependencies

```

---

## ⚙️ Configuration Notes

- **API Base URL**: `/api/v1` (proxied via dev-server)
- **JWT Header**: `Authorization: Bearer <token>`
- **Token Storage**: `localStorage.access_token`
- **Interceptor**: Auto-adds token to all HTTP requests
- **CORS Proxy**: Maps `/api` → `http://localhost:8090/api`

---

## ✨ Notes

- All 9 backend controllers are covered with corresponding services
- All endpoints from backend are implemented in the frontend
- Components are fully functional with form validation and error handling
- Responsive design with clean, modern styling
- Ready for production build: `npm run build`
- All DTOs match backend exactly (including typo in `urserName`)
