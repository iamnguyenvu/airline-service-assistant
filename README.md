# ✈️ Airline Service Assistant

> AI-powered airline customer service assistant with intelligent flight search, booking, and policy Q&A — Built with Spring AI & Next.js, deployable at **zero cost**.

[![Backend CI](https://github.com/iamnguyenvu/airline-service-assistant/workflows/Backend%20CI/badge.svg)](https://github.com/iamnguyenvu/airline-service-assistant/actions)
[![Frontend CI](https://github.com/iamnguyenvu/airline-service-assistant/workflows/Frontend%20CI/badge.svg)](https://github.com/iamnguyenvu/airline-service-assistant/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 🌟 Features

- 🤖 **AI-Powered Assistant**: Natural language flight search and booking using tool-calling LLMs (Ollama/Groq)
- 🔍 **Smart Flight Search**: Explainable ranking with breakdown (price, duration, CO₂, preferences)
- 📋 **Policy Q&A**: RAG-based answers with citations from airline documents
- 🎫 **Mock Booking**: Generate PNR and PDF e-tickets
- 🌿 **CO₂ Estimation**: Environmental impact awareness
- 📊 **Historical Data**: 3-month detailed + 1-year aggregated flight price trends
- 🔐 **Secure Auth**: Supabase OAuth integration (Google/GitHub)

## 🏗️ Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌──────────────────┐
│   Next.js 16    │────▶│  Spring Boot    │────▶│  PostgreSQL 16   │
│   Frontend      │     │  3.5 Backend    │     │  + pgvector      │
└─────────────────┘     └─────────────────┘     └──────────────────┘
        │                       │                         │
        │                       │                         │
        ▼                       ▼                         ▼
┌─────────────────┐     ┌─────────────────┐     ┌──────────────────┐
│   Supabase      │     │   Redis Cache   │     │   Ollama LLM     │
│  Auth/Storage   │     │                 │     │  (Local/Free)    │
└─────────────────┘     └─────────────────┘     └──────────────────┘
```

## 🛠️ Tech Stack

### Backend
- **Java 21** + **Spring Boot 3.5.x**
- **Spring AI 1.0** (Ollama integration + Tool Calling)
- **PostgreSQL 16** + **pgvector** (Vector search for RAG)
- **Redis 7** (Caching)
- **Flyway** (Database migrations)
- **OpenAPI/Swagger** (API documentation)

### Frontend
- **Next.js 16** (App Router)
- **React 19** + **TypeScript**
- **Tailwind CSS 4** + **shadcn/ui**
- **TanStack Query** (Data fetching)
- **Supabase JS** (Auth & Storage)
- **Recharts** (Data visualization)

### AI & ML
- **Ollama** (Local LLM - Llama 3.1 8B / Mistral 7B)
- **Alternative**: Groq API (free tier: 14,400 req/day)
- **Embeddings**: sentence-transformers (all-MiniLM-L6-v2)

### DevOps
- **Docker Compose** (Local development)
- **GitHub Actions** (CI/CD)
- **Testcontainers** (Integration testing)

## 🚀 Quick Start

### Prerequisites

- **Java 21+** (Backend)
- **Node.js 22+** (Frontend)
- **Docker & Docker Compose** (Infrastructure)
- **pnpm** (Frontend package manager)

### 1. Clone the repository

```bash
git clone https://github.com/iamnguyenvu/airline-service-assistant.git
cd airline-service-assistant
```

### 2. Start infrastructure services

```bash
# Start PostgreSQL, Redis, and Ollama
docker-compose up -d postgres redis ollama

# Wait for services to be healthy
docker-compose ps

# Pull Ollama model (first time only)
docker exec -it airline-ollama ollama pull llama3.1:8b
```

### 3. Setup Backend

```bash
cd backend

# Copy environment template
cp .env.example .env

# Edit .env with your configuration (if needed)

# Build and run
./mvnw clean install
./mvnw spring-boot:run
```

Backend will be available at `http://localhost:8080`
- API Docs: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

### 4. Setup Frontend

```bash
cd frontend

# Install pnpm if not already installed
npm install -g pnpm

# Install dependencies
pnpm install

# Copy environment template
cp .env.example .env.local

# Edit .env.local with your Supabase credentials

# Run development server
pnpm dev
```

Frontend will be available at `http://localhost:3000`

## 📦 Project Structure

```
airline-service-assistant/
├── backend/                    # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/io/github/nguyenvu/backend/
│   │   │   │   ├── ai/         # AI orchestration & tools
│   │   │   │   ├── flight/     # Flight service
│   │   │   │   ├── ingestion/  # Data ingestion
│   │   │   │   ├── config/     # Configuration
│   │   │   │   └── security/   # Security config
│   │   │   └── resources/
│   │   │       ├── db/migration/  # Flyway migrations
│   │   │       └── application.properties
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                   # Next.js application
│   ├── src/
│   │   ├── app/                # App router pages
│   │   ├── components/         # React components
│   │   ├── lib/                # Utilities & API clients
│   │   └── types/              # TypeScript types
│   ├── public/
│   ├── Dockerfile
│   └── package.json
│
├── docs/                       # Documentation
│   ├── TECH_SPEC.md           # Technical specification
│   └── DEPLOYMENT.md          # Deployment guide
│
├── .github/workflows/          # CI/CD pipelines
│   ├── backend.yml
│   ├── frontend.yml
│   └── docker.yml
│
├── docker-compose.yml          # Local development setup
└── README.md                   # This file
```

## 🧪 Testing

### Backend Tests

```bash
cd backend
./mvnw test                    # Unit tests
./mvnw verify                  # Integration tests with Testcontainers
```

### Frontend Tests

```bash
cd frontend
pnpm lint                      # ESLint check
pnpm format                    # Prettier formatting
```

## 📚 API Documentation

Once the backend is running, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Key Endpoints

```
POST   /api/chat/ask              # AI chat with tool-calling
GET    /api/flights/search        # Search flights
POST   /api/booking/confirm       # Create booking
GET    /api/prefs/{userId}        # Get user preferences
PUT    /api/prefs/{userId}        # Update preferences
```

## 🌍 Deployment Options

### Free Tier Options (Zero Cost)

#### Frontend
- **Vercel** (Recommended): Automatic deployments from GitHub
- **Netlify**: Alternative with similar features

#### Backend
- **Railway**: 500h/month free tier
- **Render**: Free tier with sleep on inactivity
- **Fly.io**: Free tier with resource limits

#### Database
- **Supabase**: 500MB database + 1GB storage (free)
- **Neon**: 3GB database (free tier)

#### AI Options
1. **Ollama** (Self-hosted, requires VPS/local)
2. **Groq API** (Free tier: 14,400 req/day)
3. **OpenRouter** (Free models available)

See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) for detailed deployment instructions.

## 🔐 Environment Variables

### Backend (.env)

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/airline_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
REDIS_HOST=localhost
REDIS_PORT=6379
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=llama3.1:8b
SUPABASE_JWT_ISSUER=https://your-project.supabase.co/auth/v1
SUPABASE_JWKS_URI=https://your-project.supabase.co/auth/v1/jwks
```

### Frontend (.env.local)

```env
NEXT_PUBLIC_SUPABASE_URL=https://your-project.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-key
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Spring AI](https://docs.spring.io/spring-ai/reference/) - AI integration framework
- [Ollama](https://ollama.ai/) - Local LLM runtime
- [Supabase](https://supabase.com/) - Backend as a Service
- [shadcn/ui](https://ui.shadcn.com/) - UI components
- [pgvector](https://github.com/pgvector/pgvector) - Vector similarity search

## 📞 Contact

Nguyen Vu - [@iamnguyenvu](https://github.com/iamnguyenvu)

Project Link: [https://github.com/iamnguyenvu/airline-service-assistant](https://github.com/iamnguyenvu/airline-service-assistant)

---

⭐ If you find this project useful, please consider giving it a star!
