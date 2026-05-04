# Team Conventions

## Naming

- Feature branches: `feature/<feature-id>-<short-description>`
- Backend packages: `com.example.<domain>.<layer>` (e.g. `com.example.orders.service`)
- Frontend components: PascalCase (e.g. `OrderButton.tsx`, `OrderHistoryTable.tsx`)
- CSS classes: BEM notation (e.g. `order-button`, `order-button--loading`)
- Environment variables: SCREAMING_SNAKE_CASE

## Backend Layering

```
controller  →  service  →  repository
                ↓
            domain model (pure Java, no Spring annotations)
```

- Controllers: only HTTP concern, delegate everything to service
- Services: business logic, transactions (`@Transactional`)
- Repositories: Spring Data JPA interfaces only, no custom SQL unless necessary
- DTOs: separate Request/Response records (Java records preferred)
- Mappers: MapStruct

## Frontend Component Structure

```
src/
  components/       ← reusable, dumb components
  features/         ← feature-specific components + hooks
  pages/            ← route-level components
  api/              ← Axios API client functions
  hooks/            ← shared custom hooks
  types/            ← TypeScript interfaces
```

## Testing

- Backend: JUnit 5 + Mockito for unit, SpringBootTest for integration
- Frontend: Vitest + React Testing Library
- Integration tests suffix: `IT` (e.g. `OrderServiceIT.java`)
- Unit tests suffix: `Test` (e.g. `OrderServiceTest.java`)
- Minimum coverage: 80% line coverage on service layer

## PR Rules

- PR title format: `feat(feature-id): short description`
- Minimum 1 approval required
- CI must be green before merge
- No force push to main
- Squash merge preferred for feature branches
