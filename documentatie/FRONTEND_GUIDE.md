# Implementarea Frontend-ului Angular - Ghid complet

## ✅ Ce a fost creat

### **Configurare de bază**
- ✅ Configurarea proxy-ului de dezvoltare (`proxy.conf.json`) - mapează `/api` la `http://localhost:8090`
- ✅ Interceptor de autentificare JWT - adaugă automat `Authorization: Bearer <token>` la toate cererile
- ✅ Auth Guard - protejează rutele care necesită autentificare
- ✅ Configurări de mediu (dev & prod)

### **Modele de date** (Interfețe TypeScript)
- ✅ Modele de autentificare (LoginDto, RegisterDto, LoginResponseDto)
- ✅ Model de restaurant (nume, adresă, telefon, website, program)
- ✅ Modele de recenzie (ReviewDto, ReviewIdDto cu rating-uri și comentarii)
- ✅ Modele Menu & MenuItem
- ✅ Modele Reply & Feedback

### **Servicii** (Câte unul per entitate)
- ✅ **AuthService** - login, register, logout, validare token
- ✅ **RestaurantService** - adăugare/editare/ștergere/listare restaurante, rating-uri, top-rated
- ✅ **ReviewService** - adăugare/editare/ștergere recenzii cu rating-uri
- ✅ **MenuService** - gestionare meniuri
- ✅ **MenuItemService** - gestionare articole din meniu
- ✅ **ReplyService** - adăugare/editare răspunsuri la recenzii
- ✅ **FeedbackService** - trimiterea de feedback de la utilizatori
- ✅ **UserService** - admin: vizualizarea recenziilor utilizatorilor
- ✅ **RoleService** - admin: gestionarea rolurilor

### **Componente**
- ✅ **Navbar** - navigare cu status de autentificare, butoane login/logout
- ✅ **Login** - formular de login email/parolă
- ✅ **Register** - înregistrarea unui utilizator nou
- ✅ **Restaurants** - listarea tuturor restaurantelor & a celor top-rated cu filtre
- ✅ **RestaurantDetail** - vizualizarea unui singur restaurant (info, rating, meniuri, recenzii)
- ✅ **RestaurantForm** - adăugare/editare restaurant (doar owner/admin)
- ✅ **ReviewForm** - adăugare recenzii cu rating de la 1 la 5 stele
- ✅ **MenuDetail** - vizualizarea articolelor pentru un meniu
- ✅ **FeedbackForm** - trimiterea de feedback despre platformă
- ✅ **Admin** - panou de admin (căutarea recenziilor utilizatorilor, gestionarea rolurilor)
- ✅ **Home** - pagina de întâmpinare

### **Rute**
```
/home                    → Pagina principală
/login                   → Formular de login
/register                → Formular de înregistrare
/restaurants             → Listă toate/top-rated restaurante (protejat)
/restaurants/:id         → Detalii pentru un singur restaurant (protejat)
/restaurants/add         → Adăugare restaurant nou (protejat)
/restaurants/:id/edit    → Editare restaurant (protejat)
/restaurants/:id/review  → Adăugare recenzie (protejat)
/menus/:id              → Articole din meniu (protejat)
/feedback               → Trimitere feedback (protejat)
/admin                  → Panou de admin (protejat)
```

---

## 🚀 Rularea aplicației

### **1. Pornește backend-ul Spring Boot**
```bash
cd /Users/mihai/Documents/facultate/awbd/TasteTest-AWDB
./mvnw spring-boot:run
```
Backend-ul va rula pe `http://localhost:8090`

### **2. Pornește frontend-ul Angular**
```bash
cd frontend
npm start
```
Frontend-ul va rula pe `http://localhost:4200` cu proxy de dezvoltare către backend

---

## 📋 Listă de verificare pentru testare

- [ ] Înregistrează un utilizator nou la `/register`
- [ ] Autentifică-te cu credențialele tale la `/login`
- [ ] Vizualizează toate restaurantele la `/restaurants`
- [ ] Apasă pe un restaurant pentru a vedea detaliile, meniurile și recenziile
- [ ] Adaugă o recenzie cu un rating
- [ ] Trimite feedback despre platformă la `/feedback`
- [ ] Deconectează-te (butonul din navbar)

---

## 🔐 Funcționalități cheie

**Autentificare:**
- Token-uri JWT stocate în localStorage
- Incluse automat în toate cererile API prin interceptor
- Validarea token-ului la login

**Autorizare:**
- Rute protejate - redirecționare la login dacă nu ești autentificat
- Proprietarul de restaurant poate adăuga/edita restaurante
- Utilizatorii pot adăuga recenzii și feedback
- Panou de admin pentru gestionarea rolurilor și căutarea recenziilor utilizatorilor

**Gestionarea erorilor:**
- Prinderea și afișarea erorilor pe formulare
- Redirecționare la eșecuri de autentificare
- Mesaje de eroare prietenoase

---

## 📁 Structura Frontend-ului
```
frontend/
├── src/app/
│   ├── models/              # Interfețe TypeScript
│   ├── services/            # Servicii de comunicare cu API-ul
│   ├── interceptors/        # Interceptor HTTP pentru JWT
│   ├── guards/              # Protecția rutelor
│   ├── components/          # Componente UI
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
│   ├── app.routes.ts        # Configurarea rutelor
│   ├── app.config.ts        # Configurarea aplicației (interceptor)
│   └── app.ts               # Componenta rădăcină
├── proxy.conf.json          # Configurarea proxy-ului de dev
├── angular.json             # Configurarea Angular CLI
└── package.json             # Dependențe

```

---

## ⚙️ Note de configurare

- **URL de bază API**: `/api/v1` (proxy-at prin dev-server)
- **Antet JWT**: `Authorization: Bearer <token>`
- **Stocarea token-ului**: `localStorage.access_token`
- **Interceptor**: Adaugă automat token-ul la toate cererile HTTP
- **Proxy CORS**: Mapează `/api` → `http://localhost:8090/api`

---

## ✨ Observații

- Toate cele 9 controllere de backend sunt acoperite cu servicii corespunzătoare
- Toate endpoint-urile din backend sunt implementate în frontend
- Componentele sunt complet funcționale, cu validare de formular și gestionarea erorilor
- Design responsiv, cu stilizare curată și modernă
- Pregătit pentru build de producție: `npm run build`
- Toate DTO-urile corespund exact cu backend-ul (inclusiv greșeala de tastare din `urserName`)
