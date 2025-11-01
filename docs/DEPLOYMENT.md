# Deployment Guide - Airline Service Assistant

Hướng dẫn chi tiết để triển khai Airline Service Assistant với các tùy chọn zero-cost hoặc minimal-cost.

## Mục lục

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Database Setup](#database-setup)
4. [AI Model Configuration](#ai-model-configuration)
5. [Backend Deployment](#backend-deployment)
6. [Frontend Deployment](#frontend-deployment)
7. [Production Deployment Options](#production-deployment-options)
8. [Monitoring & Maintenance](#monitoring--maintenance)

---

## Prerequisites

### Required Tools

```bash
# Java 21
java -version  # Should show OpenJDK 21 or later

# Node.js 20+
node -v  # Should show v20.x or later

# Docker & Docker Compose
docker --version
docker compose version

# Git
git --version
```

### Optional Tools

- **Maven**: Hoặc sử dụng wrapper `./mvnw` có sẵn
- **pnpm**: Quản lý packages nhanh hơn npm (optional)
- **Ollama**: Cho local LLM inference

---

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/iamnguyenvu/airline-service-assistant.git
cd airline-service-assistant
```

### 2. Setup Environment Variables

**Backend:**
```bash
cd backend
cp .env.example .env
# Chỉnh sửa .env với config phù hợp
```

**Frontend:**
```bash
cd frontend
cp .env.example .env.local
# Chỉnh sửa .env.local với config phù hợp
```

### 3. Start Infrastructure Services

```bash
# Từ root directory
docker compose up -d postgres redis ollama
```

**Verify services:**
```bash
# PostgreSQL
docker compose exec postgres psql -U postgres -d airline_db -c "\l"

# Redis
docker compose exec redis redis-cli ping
# Should return: PONG

# Ollama
curl http://localhost:11434/api/tags
```

### 4. Pull Ollama Model

```bash
# Pull Llama 3.1 8B model (recommended, ~4.7GB)
docker compose exec ollama ollama pull llama3.1:8b

# Alternative: Smaller model Mistral 7B (~4.1GB)
docker compose exec ollama ollama pull mistral:7b

# Verify model
docker compose exec ollama ollama list
```

### 5. Run Database Migrations

**Option A: Automatic (via Spring Boot)**
```bash
cd backend
./mvnw spring-boot:run
# Flyway sẽ tự động chạy migrations
```

**Option B: Manual**
```bash
cd backend
./mvnw flyway:migrate
```

### 6. Start Backend

```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

Backend sẽ chạy tại: `http://localhost:8080`

**Verify backend:**
- Health check: http://localhost:8080/actuator/health
- API docs: http://localhost:8080/swagger-ui.html
- Metrics: http://localhost:8080/actuator/prometheus

### 7. Start Frontend

```bash
cd frontend
npm install
# hoặc: pnpm install

npm run dev
```

Frontend sẽ chạy tại: `http://localhost:3000`

---

## Database Setup

### Using Supabase (Free Tier - Recommended for MVP)

**Limits: 500MB database, 1GB storage, 2GB bandwidth/month**

#### 1. Create Supabase Project

1. Đi tới https://supabase.com
2. Tạo tài khoản (miễn phí)
3. Tạo project mới
4. Lưu lại: `Project URL`, `anon key`, và `service_role key`

#### 2. Enable pgvector Extension

```sql
-- Trong SQL Editor của Supabase
CREATE EXTENSION IF NOT EXISTS vector;
```

#### 3. Run Migrations

Sao chép nội dung từ `backend/src/main/resources/db/migration/V1__initial_schema.sql` và chạy trong SQL Editor.

#### 4. Update Backend Config

```properties
# backend/src/main/resources/application.properties hoặc .env
DATABASE_URL=jdbc:postgresql://db.your-project.supabase.co:5432/postgres?sslmode=require
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-database-password
```

#### 5. Setup Storage Bucket

1. Trong Supabase Dashboard → Storage
2. Tạo bucket mới: `airline-policies` (public)
3. Tạo bucket mới: `airline-tickets` (private)
4. Update backend config với Storage URL

### Using Local PostgreSQL (Docker)

Đã được setup trong `docker-compose.yml`. Chỉ cần:

```bash
docker compose up -d postgres
```

---

## AI Model Configuration

### Option 1: Ollama (Local - 100% Free)

**Ưu điểm:**
- ✅ Hoàn toàn miễn phí
- ✅ Privacy tuyệt đối
- ✅ Không giới hạn requests
- ✅ Độ trễ thấp

**Nhược điểm:**
- ❌ Cần RAM/VRAM (8GB+ cho 7B model, 16GB+ cho 8B)
- ❌ Chất lượng thấp hơn GPT-4
- ❌ Setup phức tạp hơn

**Setup:**

```bash
# 1. Pull model
docker compose exec ollama ollama pull llama3.1:8b

# 2. Test model
curl http://localhost:11434/api/generate -d '{
  "model": "llama3.1:8b",
  "prompt": "Hello, how are you?",
  "stream": false
}'

# 3. Configure backend
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=llama3.1:8b
```

**Recommended Models:**
- `llama3.1:8b` - Best balance (4.7GB)
- `mistral:7b` - Faster, good quality (4.1GB)
- `phi3:mini` - Ultra lightweight (2.3GB)

### Option 2: Groq (Free API - Recommended)

**Ưu điểm:**
- ✅ Hoàn toàn miễn phí (14,400 requests/day)
- ✅ Cực kỳ nhanh (~300 tokens/s)
- ✅ Không cần GPU
- ✅ Chất lượng tốt (Llama 3.1 70B)

**Nhược điểm:**
- ❌ Rate limit: 30 req/min
- ❌ Phụ thuộc external service

**Setup:**

1. Đăng ký tại: https://console.groq.com
2. Tạo API key (miễn phí)
3. Configure backend:

```properties
# backend/src/main/resources/application.properties
spring.ai.openai.api-key=${GROQ_API_KEY}
spring.ai.openai.base-url=https://api.groq.com/openai/v1
spring.ai.openai.chat.options.model=llama-3.1-8b-instant
```

```bash
# .env
GROQ_API_KEY=gsk_xxxxxxxxxxxxx
```

### Option 3: OpenRouter (Free Models)

**Setup:**

1. Đăng ký tại: https://openrouter.ai
2. Get free credits ($1 = ~1M tokens)
3. Configure:

```properties
spring.ai.openai.api-key=${OPENROUTER_API_KEY}
spring.ai.openai.base-url=https://openrouter.ai/api/v1
spring.ai.openai.chat.options.model=meta-llama/llama-3.1-8b-instruct:free
```

### Embeddings Configuration (Local - Free)

**Using sentence-transformers API:**

```bash
# 1. Install sentence-transformers
pip install sentence-transformers

# 2. Create simple API server (embedding-server.py)
from sentence_transformers import SentenceTransformer
from flask import Flask, request, jsonify

app = Flask(__name__)
model = SentenceTransformer('all-MiniLM-L6-v2')

@app.route('/embed', methods=['POST'])
def embed():
    texts = request.json['texts']
    embeddings = model.encode(texts)
    return jsonify({'embeddings': embeddings.tolist()})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8001)

# 3. Run server
python embedding-server.py

# 4. Configure backend
EMBEDDING_API_URL=http://localhost:8001/embed
```

---

## Backend Deployment

### Option 1: Railway (Free Tier - 500h/month)

**Limits: 500 hours/month, 512MB RAM, 1GB disk**

1. Đi tới https://railway.app
2. Connect GitHub repository
3. New Project → Deploy from GitHub
4. Chọn repository và folder: `backend`
5. Add environment variables:

```
DATABASE_URL=...
GROQ_API_KEY=...
REDIS_HOST=...
```

6. Deploy!

**Custom Dockerfile:**
```dockerfile
# backend/Dockerfile (đã tạo sẵn)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Option 2: Render (Free Tier)

1. Đi tới https://render.com
2. New → Web Service
3. Connect repository, chọn `backend`
4. Build Command: `./mvnw clean package -DskipTests`
5. Start Command: `java -jar target/backend-0.0.1-SNAPSHOT.jar`
6. Add environment variables
7. Deploy!

### Option 3: Fly.io (Free Tier)

```bash
# 1. Install flyctl
curl -L https://fly.io/install.sh | sh

# 2. Login
fly auth login

# 3. Launch app
cd backend
fly launch

# 4. Set secrets
fly secrets set DATABASE_URL="..." GROQ_API_KEY="..."

# 5. Deploy
fly deploy
```

---

## Frontend Deployment

### Option 1: Vercel (Recommended - Free)

**Limits: 100GB bandwidth/month, unlimited sites**

1. Đi tới https://vercel.com
2. Import Project từ GitHub
3. Framework Preset: Next.js
4. Root Directory: `frontend`
5. Environment Variables:

```
NEXT_PUBLIC_SUPABASE_URL=https://your-project.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=...
NEXT_PUBLIC_API_URL=https://your-backend.railway.app
```

6. Deploy!

### Option 2: Netlify (Free)

```bash
# 1. Install Netlify CLI
npm install -g netlify-cli

# 2. Login
netlify login

# 3. Deploy
cd frontend
npm run build
netlify deploy --prod --dir=.next
```

### Option 3: Cloudflare Pages (Free)

1. Đi tới https://pages.cloudflare.com
2. Connect GitHub
3. Build command: `npm run build`
4. Build output directory: `.next`
5. Environment variables: thêm như Vercel
6. Deploy!

---

## Production Deployment Options

### All-in-One: Oracle Cloud Free Tier

**FREE FOREVER:**
- 4 ARM CPU cores
- 24 GB RAM
- 200 GB storage
- 10 TB bandwidth/month

**Setup:**

1. Tạo Oracle Cloud account
2. Tạo VM instance (Ampere A1)
3. Install Docker & Docker Compose
4. Clone repository
5. Setup environment variables
6. Run: `docker compose --profile full up -d`

**Install Docker on Oracle Cloud:**
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install docker-compose-plugin

# Add user to docker group
sudo usermod -aG docker $USER
```

### Hybrid Approach (Recommended for Production)

**Architecture:**
- Frontend: Vercel (free)
- Backend: Railway ($5/month) or Render ($7/month)
- Database: Supabase Pro ($25/month) or Neon Scale ($19/month)
- AI: Groq paid tier ($0.10/1M tokens)
- Cache: Upstash Redis (free 10k commands/day)

**Monthly Cost: ~$0-50 depending on traffic**

---

## Monitoring & Maintenance

### Health Checks

**Backend:**
```bash
curl http://your-backend.railway.app/actuator/health
```

**Database Connection:**
```bash
curl http://your-backend.railway.app/actuator/health/db
```

### Metrics

**Prometheus metrics:**
```
http://your-backend.railway.app/actuator/prometheus
```

### Logs

**Railway:**
```bash
railway logs
```

**Render:**
- View logs in dashboard

**Fly.io:**
```bash
fly logs
```

### Database Maintenance

**Clean old flight_raw data (run monthly):**
```sql
DELETE FROM flight_raw 
WHERE created_at < NOW() - INTERVAL '90 days';
```

**Vacuum database:**
```sql
VACUUM ANALYZE;
```

**Check database size:**
```sql
SELECT pg_size_pretty(pg_database_size('airline_db'));
```

---

## Troubleshooting

### Backend không kết nối được Database

```bash
# Kiểm tra connection string
echo $DATABASE_URL

# Test connection
psql "$DATABASE_URL" -c "\l"
```

### Ollama không pull được model

```bash
# Kiểm tra disk space
docker compose exec ollama df -h

# Restart Ollama
docker compose restart ollama

# Pull model manually
docker compose exec ollama sh
ollama pull llama3.1:8b
```

### Frontend không gọi được Backend API

1. Kiểm tra CORS configuration trong backend
2. Verify `NEXT_PUBLIC_API_URL` trong frontend
3. Check browser console for errors

### Out of Memory

**Backend:**
```bash
# Giảm JVM heap size
JAVA_OPTS="-Xmx512m -Xms256m"
```

**Ollama:**
```bash
# Dùng model nhỏ hơn
docker compose exec ollama ollama pull phi3:mini
```

---

## Next Steps

1. **Setup monitoring**: Integrate Sentry/LogRocket cho error tracking
2. **Setup analytics**: Google Analytics hoặc Plausible
3. **Add tests**: Viết integration tests cho critical flows
4. **Performance optimization**: Add Redis caching cho frequent queries
5. **Security audit**: Review CORS, rate limiting, authentication
6. **Documentation**: API documentation với Swagger
7. **Backup strategy**: Setup automated backups cho Postgres

---

## Support & Resources

- **GitHub Issues**: https://github.com/iamnguyenvu/airline-service-assistant/issues
- **Technical Spec**: `docs/TECH_SPEC.md`
- **API Documentation**: http://localhost:8080/swagger-ui.html (when running)

---

## Cost Estimation

### MVP/Testing (Free)

| Service | Free Tier | Estimated Usage |
|---------|-----------|-----------------|
| Supabase DB | 500MB | ~300MB |
| Supabase Storage | 1GB | ~500MB |
| Groq API | 14,400 req/day | ~1,000/day |
| Vercel | 100GB bandwidth | ~10GB/month |
| Railway | 500h/month | 730h (need paid) |

**Total: $0-5/month**

### Production (Minimal Cost)

| Service | Cost | Notes |
|---------|------|-------|
| Backend (Railway Hobby) | $5/month | 8GB RAM, always on |
| Database (Neon Scale) | $19/month | 3GB storage |
| AI (Groq) | ~$10/month | Pay per use |
| Frontend (Vercel) | $0 | Free tier sufficient |
| Redis (Upstash) | $0 | Free tier sufficient |

**Total: ~$34/month**

### Scale (Oracle Cloud)

| Service | Cost | Notes |
|---------|------|-------|
| VM (ARM 4 cores, 24GB RAM) | $0/month | Free forever |
| Storage 200GB | $0/month | Free forever |
| Bandwidth 10TB | $0/month | Free forever |

**Total: $0/month** (self-hosted, requires maintenance)
