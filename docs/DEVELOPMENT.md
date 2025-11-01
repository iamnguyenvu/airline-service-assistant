# Development Workflow Guide

Hướng dẫn quy trình phát triển cho Airline Service Assistant.

## Quick Start (TL;DR)

```bash
# 1. Clone & setup
git clone https://github.com/iamnguyenvu/airline-service-assistant.git
cd airline-service-assistant

# 2. Start services
docker compose up -d postgres redis ollama

# 3. Pull AI model
docker compose exec ollama ollama pull llama3.1:8b

# 4. Start backend
cd backend
./mvnw spring-boot:run

# 5. Start frontend (new terminal)
cd frontend
npm install && npm run dev
```

Visit: http://localhost:3000

---

## Development Environment Setup

### IDE Recommendations

**Backend (Java):**
- IntelliJ IDEA (recommended)
- VS Code with Java Extension Pack
- Eclipse

**Frontend (TypeScript/React):**
- VS Code (recommended)
- WebStorm

### Required IDE Plugins/Extensions

**IntelliJ IDEA:**
- Lombok
- Spring Boot Assistant
- Docker

**VS Code:**
- Java Extension Pack
- Spring Boot Extension Pack
- ESLint
- Prettier
- Tailwind CSS IntelliSense

---

## Project Structure

```
airline-service-assistant/
├── backend/                    # Spring Boot application
│   ├── src/main/java/
│   │   └── io/github/nguyenvu/backend/
│   │       ├── controller/     # REST controllers
│   │       ├── service/        # Business logic
│   │       ├── repository/     # Data access
│   │       ├── model/          # Entities & DTOs
│   │       ├── config/         # Spring configurations
│   │       └── ai/             # AI orchestration
│   ├── src/main/resources/
│   │   ├── db/migration/       # Flyway SQL migrations
│   │   └── application.properties
│   └── pom.xml
│
├── frontend/                   # Next.js application
│   ├── src/
│   │   ├── app/                # App Router pages
│   │   ├── components/         # React components
│   │   ├── lib/                # Utilities & configs
│   │   └── hooks/              # Custom React hooks
│   ├── public/                 # Static assets
│   └── package.json
│
├── docs/                       # Documentation
│   ├── TECH_SPEC.md           # Technical specification
│   ├── DEPLOYMENT.md          # Deployment guide
│   └── DEVELOPMENT.md         # This file
│
├── docker-compose.yml         # Local dev services
└── .github/workflows/         # CI/CD pipelines
```

---

## Development Workflow

### 1. Feature Development

```bash
# 1. Create feature branch
git checkout -b feature/flight-search

# 2. Make changes
# ... code ...

# 3. Test locally
cd backend && ./mvnw test
cd frontend && npm test

# 4. Commit with conventional commits
git commit -m "feat(flight): add flight search API endpoint

- Implement FlightSearchController
- Add FlightService with caching
- Create FlightSearchRequest/Response DTOs
- Add integration tests"

# 5. Push and create PR
git push origin feature/flight-search
```

### 2. Database Changes

**Creating New Migration:**

```bash
# 1. Create new migration file
cd backend/src/main/resources/db/migration
touch V2__add_user_notifications.sql

# 2. Write migration
cat > V2__add_user_notifications.sql << 'EOF'
CREATE TABLE user_notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50),
    read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user 
        FOREIGN KEY (user_id) REFERENCES users_public(id)
);

CREATE INDEX idx_notifications_user ON user_notifications(user_id, read);
EOF

# 3. Test migration
./mvnw flyway:migrate

# 4. If error, rollback and fix
./mvnw flyway:clean  # WARNING: drops all data
./mvnw flyway:migrate
```

**Migration Best Practices:**
- Never modify existing migrations in production
- Always test migrations on local DB first
- Use transactions when possible
- Add indexes for foreign keys
- Document breaking changes

### 3. API Development

**Backend (Spring Boot):**

```java
// 1. Create DTO
@Data
@Builder
public class FlightSearchRequest {
    @NotBlank
    private String from;
    
    @NotBlank
    private String to;
    
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    private Integer maxStops;
    private Long maxPriceCents;
}

// 2. Create Controller
@RestController
@RequestMapping("/api/flights")
@Tag(name = "Flights", description = "Flight search and booking APIs")
public class FlightController {
    
    @Autowired
    private FlightService flightService;
    
    @PostMapping("/search")
    @Operation(summary = "Search flights")
    public ResponseEntity<List<FlightResponse>> search(
            @Valid @RequestBody FlightSearchRequest request) {
        return ResponseEntity.ok(flightService.search(request));
    }
}

// 3. Create Service
@Service
@Slf4j
public class FlightService {
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Cacheable(value = "flights", key = "#request")
    public List<FlightResponse> search(FlightSearchRequest request) {
        log.info("Searching flights: {}", request);
        return flightRepository.findByRoute(
            request.getFrom(), 
            request.getTo(), 
            request.getDate()
        );
    }
}

// 4. Write tests
@SpringBootTest
@AutoConfigureMockMvc
class FlightControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldSearchFlights() throws Exception {
        mockMvc.perform(post("/api/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "from": "SGN",
                        "to": "HAN",
                        "date": "2025-12-01"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].flightNo").exists());
    }
}
```

**Frontend (Next.js):**

```typescript
// 1. Create API client (src/lib/api.ts)
export async function searchFlights(params: FlightSearchParams) {
  const response = await fetch(`${API_URL}/api/flights/search`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params),
  });
  
  if (!response.ok) throw new Error('Failed to search flights');
  return response.json();
}

// 2. Create React Query hook (src/hooks/useFlights.ts)
export function useFlightSearch() {
  return useMutation({
    mutationFn: searchFlights,
    onSuccess: (data) => {
      console.log('Found flights:', data.length);
    },
  });
}

// 3. Use in component (src/components/FlightSearch.tsx)
export function FlightSearch() {
  const { mutate, data, isLoading } = useFlightSearch();
  
  const handleSearch = (params: FlightSearchParams) => {
    mutate(params);
  };
  
  return (
    <div>
      {/* Search form */}
      {isLoading && <div>Searching...</div>}
      {data && <FlightResults flights={data} />}
    </div>
  );
}
```

### 4. AI Tool Development

**Adding New AI Tool:**

```java
// 1. Define tool interface
@Component
@Slf4j
public class FlightSearchTool implements Function<FlightSearchRequest, List<Flight>> {
    
    @Override
    public List<Flight> apply(FlightSearchRequest request) {
        log.info("AI Tool called: searchFlights with {}", request);
        // Implementation
        return flightService.search(request);
    }
}

// 2. Register tool in ChatClient
@Bean
public ChatClient aiChatClient(
        ChatClient.Builder builder,
        FlightSearchTool flightSearchTool) {
    
    return builder
        .defaultSystem("""
            You are an airline customer service assistant.
            Use tools to provide accurate information.
            Always cite sources.
            """)
        .defaultFunctions(
            "searchFlights", flightSearchTool,
            "getFareRules", fareRulesTool,
            "estimateCO2", co2Tool
        )
        .build();
}

// 3. Use in orchestrator
@Service
public class AiOrchestrator {
    
    @Autowired
    private ChatClient chatClient;
    
    public String chat(String userMessage) {
        return chatClient.prompt()
            .user(userMessage)
            .call()
            .content();
    }
}
```

### 5. Testing Strategy

**Backend Tests:**

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=FlightControllerTest

# Run with coverage
./mvnw test jacoco:report
# View: target/site/jacoco/index.html

# Integration tests with Testcontainers
./mvnw verify
```

**Test Structure:**
```java
@SpringBootTest
@Testcontainers
class FlightServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:16");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }
    
    @Test
    void shouldPersistFlight() {
        // Given
        Flight flight = createTestFlight();
        
        // When
        Flight saved = flightRepository.save(flight);
        
        // Then
        assertThat(saved.getId()).isNotNull();
    }
}
```

**Frontend Tests:**

```bash
# Run tests
npm test

# Run with coverage
npm test -- --coverage

# E2E tests (if using Playwright)
npm run test:e2e
```

---

## Code Style & Standards

### Backend (Java)

**Formatting:**
```xml
<!-- Add to pom.xml -->
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <configuration>
        <java>
            <googleJavaFormat/>
        </java>
    </configuration>
</plugin>
```

```bash
# Format code
./mvnw spotless:apply

# Check formatting
./mvnw spotless:check
```

**Naming Conventions:**
- Classes: `PascalCase` (FlightService)
- Methods: `camelCase` (searchFlights)
- Constants: `UPPER_SNAKE_CASE` (MAX_RETRIES)
- Packages: `lowercase` (io.github.nguyenvu.backend.service)

### Frontend (TypeScript)

**Formatting:**
```bash
# Format code
npm run format

# Check formatting
npm run lint
```

**Naming Conventions:**
- Components: `PascalCase` (FlightCard)
- Functions: `camelCase` (formatCurrency)
- Types/Interfaces: `PascalCase` (FlightSearchParams)
- Files: `kebab-case` (flight-search.tsx) or `PascalCase` (FlightSearch.tsx)

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code restructuring
- `test`: Adding tests
- `chore`: Maintenance

**Examples:**
```bash
feat(flight): add real-time flight status tracking

fix(booking): resolve PDF generation encoding issue for Vietnamese characters

docs(api): update Swagger documentation for booking endpoints

refactor(ai): extract tool definitions into separate config class

test(flight): add integration tests for search with filters
```

---

## Debugging

### Backend Debugging

**IntelliJ IDEA:**
1. Run → Edit Configurations
2. Add New → Spring Boot
3. Main class: `BackendApplication`
4. Set breakpoints
5. Debug mode (Shift+F9)

**VS Code:**
```json
// .vscode/launch.json
{
  "configurations": [
    {
      "type": "java",
      "name": "Debug Backend",
      "request": "launch",
      "mainClass": "io.github.nguyenvu.backend.BackendApplication",
      "projectName": "backend"
    }
  ]
}
```

**Remote Debugging:**
```bash
# Start with debug agent
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

# Connect IDE to port 5005
```

### Frontend Debugging

**Browser DevTools:**
- Chrome DevTools (F12)
- React DevTools extension
- Network tab for API calls

**VS Code:**
```json
// .vscode/launch.json
{
  "configurations": [
    {
      "type": "chrome",
      "request": "launch",
      "name": "Debug Frontend",
      "url": "http://localhost:3000",
      "webRoot": "${workspaceFolder}/frontend"
    }
  ]
}
```

---

## Performance Optimization

### Backend

**1. Database Query Optimization:**
```java
// Use projections instead of full entities
interface FlightProjection {
    String getFlightNo();
    String getCarrier();
    Long getPriceCents();
}

List<FlightProjection> findByRoute(String from, String to);
```

**2. Caching:**
```java
@Cacheable(value = "flights", key = "#from + '-' + #to + '-' + #date")
public List<Flight> findByRoute(String from, String to, LocalDate date) {
    // Expensive query
}
```

**3. Async Processing:**
```java
@Async
public CompletableFuture<PolicyDocument> parsePolicy(String url) {
    // Long-running task
}
```

### Frontend

**1. Code Splitting:**
```typescript
// Dynamic imports
const FlightMap = dynamic(() => import('@/components/FlightMap'), {
  loading: () => <Skeleton />,
  ssr: false,
});
```

**2. Image Optimization:**
```typescript
import Image from 'next/image';

<Image
  src="/airline-logo.png"
  width={100}
  height={50}
  alt="Airline"
  priority
/>
```

**3. React Query Optimization:**
```typescript
useQuery({
  queryKey: ['flights', params],
  queryFn: () => searchFlights(params),
  staleTime: 5 * 60 * 1000, // 5 minutes
  cacheTime: 10 * 60 * 1000, // 10 minutes
});
```

---

## Common Tasks

### Adding a New Entity

```bash
# 1. Create migration
# V3__add_airline_partners.sql

# 2. Create entity
# backend/src/main/java/io/github/nguyenvu/backend/model/AirlinePartner.java

# 3. Create repository
# backend/src/main/java/io/github/nguyenvu/backend/repository/AirlinePartnerRepository.java

# 4. Create service
# backend/src/main/java/io/github/nguyenvu/backend/service/AirlinePartnerService.java

# 5. Create controller
# backend/src/main/java/io/github/nguyenvu/backend/controller/AirlinePartnerController.java

# 6. Write tests
```

### Adding a New Page

```bash
# 1. Create page component
# frontend/src/app/flights/search/page.tsx

# 2. Create components
# frontend/src/components/FlightSearchForm.tsx
# frontend/src/components/FlightResults.tsx

# 3. Create hooks
# frontend/src/hooks/useFlightSearch.ts

# 4. Create types
# frontend/src/types/flight.ts

# 5. Update navigation
```

---

## Troubleshooting Common Issues

### Issue: Maven build fails with "Cannot resolve dependencies"

**Solution:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Re-download dependencies
./mvnw clean install -U
```

### Issue: Frontend build fails with TypeScript errors

**Solution:**
```bash
# Clean and rebuild
rm -rf node_modules .next
npm install
npm run build
```

### Issue: Database migration fails

**Solution:**
```bash
# Check Flyway history
./mvnw flyway:info

# Repair if needed
./mvnw flyway:repair

# Re-run migrations
./mvnw flyway:migrate
```

### Issue: Ollama model not responding

**Solution:**
```bash
# Restart Ollama
docker compose restart ollama

# Check model is loaded
docker compose exec ollama ollama list

# Test model directly
docker compose exec ollama ollama run llama3.1:8b "Hello"
```

---

## Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Spring AI Docs**: https://docs.spring.io/spring-ai/reference/
- **Next.js Docs**: https://nextjs.org/docs
- **TanStack Query**: https://tanstack.com/query/latest
- **Supabase Docs**: https://supabase.com/docs
- **Ollama Docs**: https://ollama.ai/docs

---

## Getting Help

1. Check documentation in `docs/`
2. Search GitHub Issues
3. Create new issue with:
   - Description of problem
   - Steps to reproduce
   - Expected vs actual behavior
   - Environment details (OS, versions)
   - Relevant logs
