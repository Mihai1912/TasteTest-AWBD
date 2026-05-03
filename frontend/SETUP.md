# Angular Frontend Setup

## Prerequisites
- Node.js (v18+)
- npm (comes with Node.js)

## Installation

```bash
cd frontend
npm install
```

## Development Server

Start the development server:

```bash
npm start
```

The app will be available at `http://localhost:4200`

## Building for Production

```bash
npm run build
```

Output will be in the `dist/` directory.

## API Configuration

The frontend is configured to connect to the Spring Boot backend at `http://localhost:8090/api`.

Make sure your backend is running before starting the frontend:
```bash
cd .. # Go back to project root
./mvnw spring-boot:run
```

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── services/
│   │   │   └── api.service.ts      # HTTP service for API calls
│   │   ├── app.ts                  # Root component
│   │   ├── app.config.ts           # App configuration
│   │   └── app.routes.ts           # Route configuration
│   ├── environments/               # Environment configuration
│   ├── index.html                  # Main HTML file
│   ├── main.ts                     # Application entry point
│   └── styles.css                  # Global styles
├── angular.json                    # Angular CLI configuration
├── tsconfig.json                   # TypeScript configuration
└── package.json                    # Project dependencies
```

## Creating Components and Services

Generate a new component:
```bash
ng generate component components/your-component-name
```

Generate a new service:
```bash
ng generate service services/your-service-name
```

## Using the API Service

Example usage in a component:

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
