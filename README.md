<div align="center">

# ✈️ Airline Service Assistant

### AI-Powered Flight Search & Customer Service Platform

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16-000000?style=for-the-badge&logo=next.js&logoColor=white)](https://nextjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

[Features](#-features) • [Tech Stack](#-tech-stack) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [Contributing](#-contributing)

</div>

---

## 🎯 Overview

**Airline Service Assistant** is a zero-cost, AI-powered airline customer service assistant that provides intelligent flight search and policy guidance. It acts as a virtual customer service agent focused purely on **advisory and consultation** — **without handling actual bookings or transactions**.

### 🌟 Key Highlights

- 🤖 **Tool-First AI**: Uses Spring AI function calling to ensure accurate, citation-backed responses
- 💰 **Zero-Cost Deployment**: Runs entirely on free tiers (PostgreSQL, Redis, Gemini API)
- 🔍 **Smart Flight Advisory**: Multi-factor flight analysis (price, duration, CO₂, schedules) with explanations
- 📊 **Policy RAG**: Vector search over airline policies with pgvector and source citations
- 🌍 **Bilingual Support**: Vietnamese and English with zero hallucination
- 💬 **Chat-First Interface**: Single conversational interface for all interactions
- 📈 **Real-time Data**: Auto-crawled flight snapshots and policy ingestion

---

## ✨ Features

### Core Capabilities
- 🔎 **Flight Search & Advisory**: Find and analyze flights with intelligent recommendations
- 📖 **Policy Q&A**: Ask about airline policies, fees, baggage rules, etc. with citations
- 💰 **Price Comparison**: Compare fares across airlines with detailed breakdowns
- 🌿 **CO₂ Impact**: Environmental impact analysis vs. route averages
- 📊 **Route Analytics**: Price trends and statistics for informed decisions

### For Customer Service Agents
- 📋 **Quick Policy Lookup**: Instant access to fare rules and conditions with sources
- 💵 **Fee Information**: Policy-based fee explanations with rule citations
- 🔄 **Disruption Guidance**: Alternative flight suggestions during delays/cancellations
- 📊 **Price Intelligence**: Historical pricing data for customer consultation

### Technical Features
- 🏗️ **Modular Architecture**: Clean separation (AI/Flight/Policy services)
- 🧪 **Testcontainers**: Full integration tests with PostgreSQL + Redis
- 📚 **OpenAPI Docs**: Auto-generated Swagger UI
- 🔄 **Auto Data Ingestion**: Scheduled flight data crawling and policy processing
- 📦 **Docker Compose**: One-command local environment

---

## 🛠️ Tech Stack

### Backend
| Technology | Purpose | Why? |
|------------|---------|------|
| **Java 21 + Spring Boot 3.5** | Core framework | Enterprise-grade, mature ecosystem |
| **Spring AI 1.0** | LLM orchestration | Native tool calling support |
| **PostgreSQL 16 + pgvector** | Database + Vector search | Single DB for OLTP + RAG |
| **Redis 7** | Caching | Fast search result + status caching |
| **Flyway** | Schema migrations | Version-controlled DB changes |
| **Testcontainers** | Integration testing | Real DB tests in CI/CD |

### Frontend
| Technology | Purpose | Why? |
|------------|---------|------|
| **Next.js 16 (App Router)** | React framework | SSR/ISR for SEO, streaming support |
| **Tailwind CSS 4 + shadcn/ui** | UI components | Rapid dev, consistent design |
| **TanStack Query** | Data fetching | Smart caching, auto-retry |
| **Supabase Client** | Auth + Storage | OAuth, file uploads |
| **Recharts** | Data visualization | Price trends, CO₂ charts |

### AI & Data
| Technology | Purpose | Cost |
|------------|---------|------|
| **Gemini 2.5 Flash API** | Production LLM | $0 (free tier: 15 RPM, 1M TPM, 1500 RPD) |
| **Ollama** (Llama 3.1 8B) | Local LLM (dev) | $0 (self-hosted) |
| **Ollama nomic-embed-text** | Embeddings (384-dim) | $0 (self-hosted) |
| **pgvector** | Vector similarity search | $0 (PostgreSQL extension) |

**Why 100% Free?**
- Gemini 2.5 Flash free tier: 15 req/min, 1M tokens/min, 1500 req/day - perfect for MVP
- Ollama local: No API costs, unlimited requests for development
- Switch between Gemini (cloud) and Ollama (local) via config

### DevOps
| Technology | Purpose | Cost |
|------------|---------|------|
| **Docker + Compose** | Local environment | $0 |
| **GitHub Actions** | CI/CD | $0 (2000 min/month) |
| **Supabase Free Tier** | DB + Storage | $0 (500MB + 1GB) |
| **Railway/Render Free** | Backend hosting | $0 (500h/month) |
| **Vercel Free** | Frontend hosting | $0 (100GB bandwidth) |

---

## 🚀 Quick Start

### Prerequisites
- **Docker & Docker Compose** (for local development)
- **Java 21+** (if running backend natively)
- **Node.js 22+** (if running frontend natively)
- **pnpm** (frontend package manager)

### 1. Clone & Setup

```bash
# Clone repository
git clone https://github.com/iamnguyenvu/airline-service-assistant.git
cd airline-service-assistant

# Copy environment files
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env.local
```

### 2. Start Infrastructure (Docker Compose)

```bash
# Start PostgreSQL + Redis + Ollama
docker compose up -d postgres redis ollama

# Pull Ollama model (one-time, ~4.7GB)
docker exec -it airline-ollama ollama pull llama3.1:8b

# Verify services
docker compose ps
```

### 3. Run Backend

```bash
cd backend

# Build and run (Maven)
./mvnw spring-boot:run

# Or use your IDE to run BackendApplication.java
```

Backend will start at `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator: `http://localhost:8080/actuator/health`

### 4. Run Frontend

```bash
cd frontend

# Install dependencies
pnpm install

# Start dev server
pnpm dev
```

Frontend will start at `http://localhost:3000`

### 5. Test the Setup

```bash
# Health check
curl http://localhost:8080/actuator/health

# Test AI chat assistant
curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" \
  -d '{"message":"Tìm vé SGN-HAN ngày 15/12, budget dưới 2 triệu"}'

# Test policy Q&A
curl -X POST http://localhost:8080/api/policy/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"VNA cho phép mang bao nhiêu kg hành lý xách tay?"}'
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [Technical Specification](docs/TECH_SPEC.md) | Detailed architecture & design decisions |
| [Deployment Guide](docs/DEPLOYMENT.md) | Step-by-step deployment instructions |
| [Development Workflow](docs/DEVELOPMENT.md) | Local setup, testing, contribution guide |
| [API Documentation](http://localhost:8080/swagger-ui.html) | Interactive API explorer (when running) |

---

## 🏗️ Architecture

```
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────┐
│   Next.js UI    │◄────►│  Spring Boot API │◄────►│  PostgreSQL+PG  │
│   (Port 3000)   │      │   (Port 8080)    │      │  (Port 5432)    │
└────────┬────────┘      └────────┬─────────┘      └─────────────────┘
         │                        │                          │
         │                        │                          │
         ▼                        ▼                          ▼
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────┐
│  Supabase Auth  │      │   Redis Cache    │      │   Ollama LLM    │
│  + Storage      │      │   (Port 6379)    │      │  (Port 11434)   │
└─────────────────┘      └──────────────────┘      └─────────────────┘
```

### Data Flow
1. **User Query** → Frontend → Backend API
2. **AI Orchestrator** → Parse intent → Select tools
3. **Tools Execute** → Search DB / RAG / Live API
4. **Response Assembly** → Citations + Explanations
5. **UI Render** → Breakdown scores + badges

---

## 🧪 Testing

```bash
# Backend unit + integration tests
cd backend
./mvnw test

# Backend with coverage
./mvnw verify

# Frontend tests (when implemented)
cd frontend
pnpm test

# E2E tests (when implemented)
pnpm test:e2e
```

---

## 🚢 Deployment

### Option 1: All Free (Recommended for MVP)
```bash
# Frontend → Vercel
vercel --prod

# Backend → Railway
railway up

# Database → Supabase (already setup)
```

### Option 2: Self-Hosted (Full Control)
```bash
# Build all services
docker compose --profile full up --build -d

# Access at:
# - Frontend: http://localhost:3000
# - Backend: http://localhost:8080
```

See [DEPLOYMENT.md](docs/DEPLOYMENT.md) for detailed instructions.

---

## 🗂️ Project Structure

```
airline-service-assistant/
├── backend/                    # Spring Boot backend
│   ├── src/main/java/io/github/nguyenvu/backend/
│   │   ├── ai/                # AI orchestration
│   │   ├── flight/            # Flight service
│   │   ├── ingestion/         # Data ingestion
│   │   └── config/            # Spring configuration
│   ├── src/main/resources/
│   │   ├── db/migration/      # Flyway migrations
│   │   └── application.properties
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   # Next.js frontend
│   ├── src/
│   │   ├── app/               # App Router pages
│   │   ├── components/        # React components
│   │   ├── lib/               # Utilities & clients
│   │   └── hooks/             # Custom React hooks
│   ├── public/
│   ├── Dockerfile
│   └── package.json
├── docs/                       # Documentation
│   ├── TECH_SPEC.md           # Technical specification
│   ├── DEPLOYMENT.md          # Deployment guide
│   └── DEVELOPMENT.md         # Dev workflow
├── .github/workflows/          # CI/CD pipelines
│   ├── backend-ci.yml
│   ├── frontend-ci.yml
│   └── docker-publish.yml
├── docker-compose.yml          # Local development
└── README.md                   # This file
```

---

## 🤝 Contributing

We welcome contributions! Please see [DEVELOPMENT.md](docs/DEVELOPMENT.md) for:
- Code style guidelines
- Commit message conventions
- PR process
- Local development setup

### Quick Contribution Guide
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📊 Roadmap

### ✅ Phase 1: MVP (Completed)
- [x] Backend scaffolding with Spring Boot + Spring AI
- [x] Database schema with Flyway migrations
- [x] Docker Compose for local dev
- [x] CI/CD pipelines
- [x] Frontend scaffolding with Next.js

### 🔨 Phase 2: Core Features (In Progress)
- [ ] Flight search & advisory implementation  
- [ ] AI chat with Spring AI tool calling
- [ ] Policy RAG with pgvector and citations
- [ ] Auto flight data ingestion scheduler
- [ ] Policy upload and ingestion system

### 🚀 Phase 3: Advanced Features (Planned)
- [ ] Live flight status integration
- [ ] IRROPS advisory and alternative suggestions
- [ ] Multi-airline support (VietJet, Bamboo Airways, etc.)
- [ ] Vietnamese language optimization
- [ ] Mobile responsive chat interface

### 🌟 Phase 4: Production Ready (Future)
- [ ] Load testing & optimization
- [ ] Advanced policy parsing (complex fare rules)
- [ ] Real-time flight delay notifications
- [ ] Analytics dashboard for usage patterns
- [ ] API rate limiting and authentication

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [Spring AI](https://docs.spring.io/spring-ai/reference/) - AI orchestration framework
- [Ollama](https://ollama.ai/) - Local LLM runtime
- [Supabase](https://supabase.com/) - Backend-as-a-Service
- [shadcn/ui](https://ui.shadcn.com/) - Beautiful UI components
- [pgvector](https://github.com/pgvector/pgvector) - Vector similarity search

---

## 📬 Contact

**Nguyen Vu** - [@iamnguyenvu](https://github.com/iamnguyenvu)

Project Link: [https://github.com/iamnguyenvu/airline-service-assistant](https://github.com/iamnguyenvu/airline-service-assistant)

---

<div align="center">

Made with ❤️ by [Nguyen Vu](https://github.com/iamnguyenvu)

⭐ Star this repo if you find it helpful!

</div>
