# Configurarea Frontend-ului Angular

## Cerințe preliminare
- Node.js (v18+)
- npm (vine împreună cu Node.js)

## Instalare

```bash
cd frontend
npm install
```

## Server de dezvoltare

Pornește serverul de dezvoltare:

```bash
npm start
```

Aplicația va fi disponibilă la `http://localhost:4200`

## Build pentru producție

```bash
npm run build
```

Rezultatul va fi în directorul `dist/`.

## Configurarea API-ului

Frontend-ul este configurat să se conecteze la backend-ul Spring Boot la `http://localhost:8090/api`.

Asigură-te că backend-ul tău rulează înainte de a porni frontend-ul:
```bash
cd .. # Revino la rădăcina proiectului
./mvnw spring-boot:run
```

## Structura proiectului

```
frontend/
├── src/
│   ├── app/
│   │   ├── services/
│   │   │   └── api.service.ts      # Serviciu HTTP pentru apeluri API
│   │   ├── app.ts                  # Componenta rădăcină
│   │   ├── app.config.ts           # Configurarea aplicației
│   │   └── app.routes.ts           # Configurarea rutelor
│   ├── environments/               # Configurarea mediului
│   ├── index.html                  # Fișierul HTML principal
│   ├── main.ts                     # Punctul de intrare al aplicației
│   └── styles.css                  # Stiluri globale
├── angular.json                    # Configurarea Angular CLI
├── tsconfig.json                   # Configurarea TypeScript
└── package.json                    # Dependențele proiectului
```

## Crearea de componente și servicii

Generează o componentă nouă:
```bash
ng generate component components/numele-componentei-tale
```

Generează un serviciu nou:
```bash
ng generate service services/numele-serviciului-tau
```

## Folosirea serviciului API

Exemplu de utilizare într-o componentă:

```typescript
import { Component, OnInit } from '@angular/core';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-example',
  templateUrl: './example.component.html',
  styleUrls: ['./example.component.css']
})
export class ExampleComponent implements OnInit {
  data: any;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.get('/restaurants').subscribe(
      (response) => {
        this.data = response;
      },
      (error) => {
        console.error('Error fetching data:', error);
      }
    );
  }
}
```
