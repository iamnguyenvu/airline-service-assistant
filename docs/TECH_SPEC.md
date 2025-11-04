# Airline Service Assistant — Technical Specification (Zero-Cost Deployment)

## 1) Mục tiêu & Triết lý

**Mục tiêu**: Trợ lý AI (VI/EN) "hành xử như nhân viên CSKH của hãng": tư vấn, tìm/đặt/giữ/hủy, trả lời quy định vé/hành lý, gợi ý rebook khi gián đoạn, kiểm tra trạng thái chuyến bay gần realtime.

**Triết lý**: Tool-first, no-hallucination — mọi thông tin giá/chính sách đều có citation (nguồn + ngày snapshot / version policy).

## 2) Đối tượng & Use-cases

**Khách lẻ**: tìm → đặt/giữ → hỏi quy định → hủy/đổi.

**CSKH/Đại lý**: tra điều kiện vé, phí đổi/hoàn ước tính, rebook nhanh.

### Use-cases chính

- Tìm chuyến (one-way/round-trip), lọc theo ngân sách/giờ/hành lý/hãng.
- Explainable ranking: điểm tổng hợp + breakdown (Giá/Thời lượng/CO₂/Sở thích).
- Policy Q&A (đổi/hoàn/no-show/baggage/đồ đặc biệt) kèm nguồn & version.
- Booking mock: tạo PNR, PDF e-ticket, hold timer.
- IRROPS: delay/cancel → đề xuất rebook + ước tính phí/điều kiện.
- Live status (nhẹ): cache 60–120s.
- User prefs: ghế, ngân sách, hãng ưa thích → tự áp dụng vào ranking.

## 3) Phạm vi dữ liệu & Tuân thủ

**Policy**: thu thập từ tài liệu công khai (PDF/HTML), parse → fare_rule_norm, đồng thời chunk & embed cho RAG (pgvector).

**Flight snapshots**: cập nhật hằng ngày khung T+1 → T+30 cho các tuyến phổ biến; tùy chọn live status theo yêu cầu.

**Compliance**: tôn trọng robots.txt/ToS; lưu raw_payload có kiểm soát; hiển thị disclaimer với số liệu ước tính.

## 4) Kiến trúc tổng thể

**Frontend**: Next.js 16 (App Router) + Tailwind 4 + shadcn/ui, TanStack Query, Supabase (Auth/Realtime/Storage).

**Backend**: Spring Boot 3.5.x + Spring AI (ChatClient + Tool Calling).

**LLM Strategy (100% Free)**:
- **Production/Cloud**: Gemini 2.5 Flash API (Free tier: 15 RPM, 1M TPM, 1500 RPD)
- **Local Development**: Ollama with Llama 3.1 8B (self-hosted, no cost)
- **Embedding**: Ollama nomic-embed-text (384-dim, self-hosted, no cost)

**Modules**:
- ai-orchestrator (POST /api/chat/ask)
- flight-service (GET /api/flights/search, POST /api/booking/confirm, prefs)
- ingestion-service (scheduler snapshot & policy ingestion)
- infra (Flyway migrations)

**Data**: PostgreSQL 16 + pgvector, Redis cache; Supabase Storage lưu PDF vé & policy gốc.

## 5) Orchestration & Ranking

System prompt bắt buộc dùng tools, luôn trả citation.

**Tools**:
- searchFlights(params) → đọc snapshot mới nhất theo tuyến/ngày.
- getFareRule(airline,cabin,fare_family) → đọc fare_rule_norm (chuẩn hóa).
- ragPolicy(query, airline?) → vector search service_doc_chunks.
- estimateCO2(from,to,aircraft?) → ước tính great-circle.
- liveStatus(flightNo,date?) → provider nhẹ + cache.

**Scoring** (mặc định): `score = 0.4*price + 0.3*duration + 0.2*co2 + 0.1*prefs` (chuẩn hóa theo tuyến/ngày).

UI hiển thị breakdown + badge 🌿 CO₂ (so với trung vị tuyến).

## 6) API bề mặt

- `POST /api/chat/ask` → NL → tools → câu trả lời có giải thích + citations.
- `GET /api/flights/search?from=SGN&to=HAN&date=YYYY-MM-DD&…` → danh sách + điểm + snapshot meta.
- `POST /api/booking/confirm` → tạo PNR mock + PDF e-ticket (signed URL).
- `GET/PUT /api/prefs/{userId}` → sở thích người dùng.

## 7) Mô hình dữ liệu (chính)

```sql
flight_snapshot(
  id, snapshot_date, dep_iata, arr_iata, dep_time, arr_time, 
  carrier, flight_no, duration_min, stops, fare_family, 
  baggage_kg, price_cents, currency, source, created_at
)

flight_raw(
  snapshot_id → flight_snapshot.id, 
  payload jsonb, 
  created_at
)

route_stats_daily(
  route_key, date, 
  min/p50/p90_price_cents, 
  avg_duration_min, avg_co2_kg
)

service_docs(
  id, source_url, airline_code, doc_type, version_tag, 
  raw_text?, storage_url?, created_at
)

service_doc_chunks(
  id, doc_id, section_title, chunk, embedding vector
)

fare_rule_norm(
  id, airline_code, cabin, fare_family, 
  change_fee, refund_fee, refund_allowed, 
  change_deadline, no_show_fee, 
  carry_on_kg, checked_bag_kg, 
  effective_from, effective_to
)

bookings_mock(
  pnr, user_id, flight_snapshot_id, 
  status, pdf_url, payload, created_at
)

users_public(
  id, full_name, created_at
)
```

## 8) Chiến lược lưu lịch sử 3 tháng → 1 năm

### Phân tầng dữ liệu

**HOT (0–90 ngày)**
- Bảng flight_snapshot (skinny) + flight_raw (payload chi tiết).
- Mục tiêu: trả lời nhanh, audit đầy đủ, phục vụ so sánh ngắn hạn.
- TTL flight_raw = 90 ngày (xóa payload cũ, vẫn giữ row skinny).

**WARM (91–365 ngày)**
- Giữ flight_snapshot dạng skinny (không payload).
- Duy trì route_stats_daily (min/p50/p90, avg_duration, avg_co2).
- Cho phép truy vấn lịch sử đến 1 năm với chi tiết cơ bản.

**COLD (> 1 năm) — tùy chọn**
- Kết xuất Parquet theo tháng/tuyến lên Supabase Storage.
- Giữ trong DB chỉ bảng thống kê (daily/monthly).

### Kỹ thuật tiết kiệm dung lượng

- Partition theo tháng cho flight_snapshot (Postgres 16).
- Index tối thiểu: (dep_iata, arr_iata, snapshot_date).
- Chuẩn hóa cột nhỏ (enum/short code) cho currency, carrier, fare_family.
- Policy gốc lưu Storage + trường version_tag trong DB.

## 9) Bảo mật & Tuân thủ

**Auth**: Supabase OAuth (Google/GitHub); Backend là Resource Server verify JWKS.

**Phân quyền**: RLS cho bảng đọc từ frontend; backend enforce userId theo token.

**Secrets**: .env (local), GitHub Secrets (CI); không commit khoá.

**ToS/robots**: chỉ crawl policy công khai; giá/lịch dùng snapshots từ provider hợp lệ; UI/answer luôn hiển thị nguồn.

## 10) NFR & SLO

**Hiệu năng**: p95 < 1.2s cho /api/chat/ask & /flights/search (DB/cache).

**Độ tin cậy**: snapshot job thành công ≥99%/tháng (retry + backoff).

**Quan sát**: OpenTelemetry traces; Prometheus metrics; Grafana dashboard.

**Chất lượng**: Testcontainers (Postgres/Redis), RestAssured, ESLint/Prettier, Spotless/Checkstyle.

## 11) Tech Stack — Zero-Cost Deployment

### Backend (Java / Spring)

- **Java 21** + **Spring Boot 3.5.x**: nền tảng dịch vụ REST, cấu hình gọn, ecosystem phong phú.
- **Spring Web, Validation**: expose API, validate tham số search/booking.
- **Spring Data JPA** (Hibernate): truy vấn Postgres, mapping entity rõ ràng.
- **Spring Security** (Resource Server): xác thực JWT từ Supabase JWKS; bảo vệ API.
- **Spring AI 1.x** (ChatClient + Tool Calling): điều phối AI "tool-first".
- **Scheduler**: @Scheduled / Quartz cho cron ingestion.
- **Cache**: Redis 7 để cache kết quả search ngắn hạn & live status.
- **OpenAPI** (springdoc): tài liệu API tự sinh (Swagger UI).

### Data & Retrieval (Zero-Cost Options)

- **PostgreSQL 16**: Supabase Free Tier (500MB DB) hoặc local Docker.
- **pgvector**: lưu embedding và tìm kiếm vector cho RAG.
- **Supabase Storage** (Free: 1GB): lưu file PDF e-ticket, policy gốc.
- **Parser**: Apache Tika + PDFBox + jsoup.

### Frontend (Web App)

- **Next.js 16** (App Router) + **React 19** + **TypeScript**
- **UI**: Tailwind CSS 4 + shadcn/ui (Radix UI)
- **Data fetching**: TanStack Query
- **Charts**: Recharts
- **Auth**: Supabase JS (OAuth Google/GitHub)

### AI & Orchestration (Zero-Cost Strategy)

**Option 1: Ollama (Local, 100% Free)**
- Spring AI tích hợp Ollama: chạy Llama 3.1 8B hoặc Mistral 7B
- Embeddings: sentence-transformers (all-MiniLM-L6-v2) local REST API hoặc ONNX Runtime
## 10) AI/LLM Strategy (100% Free)

### LLM Options

**Option 1: Google Gemini API (Recommended for Production)**
- **Model**: Gemini 2.5 Flash (fast, efficient, latest)
- **Free Tier**: 
  - 15 requests/minute (RPM)
  - 1 million tokens/minute (TPM)
  - 1,500 requests/day (RPD)
  - No credit card required
- **Pros**: Fast, reliable, multimodal support, function calling, latest model
- **Cons**: Rate limits (sufficient for MVP/small apps)
- **Setup**: Get API key at https://aistudio.google.com/app/apikey

**Option 2: Ollama Local (Recommended for Development)**
- **Model**: Llama 3.1 8B (or 7B for lower RAM)
- **Cost**: 100% free, self-hosted
- **RAM**: 8GB+ for 7B, 16GB+ for 8B
- **Pros**: No rate limits, privacy, offline support
- **Cons**: Requires GPU/CPU, slower inference
- **Setup**: `ollama pull llama3.1:8b`

**Embedding (Always Free)**
- **Ollama nomic-embed-text**: 384-dim, self-hosted, no API cost
- **Alternative**: Gemini text-embedding-004 (free tier included)

**Deployment Strategy**
- **Development**: Ollama local (free, fast iteration)
- **Production**: Gemini API (free tier, reliable)
- **Fallback**: Switch to Ollama if Gemini rate limits hit

### Cost Breakdown (100% Free)

| Service | Free Tier | Usage Estimate |
|---------|-----------|----------------|
| Gemini 2.5 Flash | 15 RPM, 1M TPM, 1.5k RPD | ~500-1000 req/day |
| Ollama (local) | Unlimited | Dev only |
| Supabase DB | 500MB | ~300MB (3mo data) |
| Supabase Storage | 1GB | ~500MB (PDFs) |
| Vercel Hosting | 100GB bandwidth | ~10GB/month |
| Railway Backend | 500h/month | ~730h (always-on) |

**Total Monthly Cost: $0** (all within free tiers)

### Observability & Quality

- **OpenTelemetry** (traces), **Prometheus/Grafana** (metrics)
- **Logging**: JSON + Correlation-ID
- **Testing**: JUnit 5, Testcontainers, RestAssured

### DevOps (Zero-Cost)

- **Docker Compose** (local): Postgres + Redis + Ollama
- **Flyway**: Database migration management
- **GitHub Actions**: CI/CD (2000 min/month free)
- **Hosting**:
  - Backend: Railway (500h/month free) or Render
  - Frontend: Vercel (free tier)
  - Database: Supabase (free tier: 500MB)

### Self-Hosted (Zero-Cost, requires hardware)
- VPS: Oracle Cloud Free Tier (ARM instance, 24GB RAM)
- Docker Compose: Backend + Frontend + Postgres + Redis + Ollama
- Domain: Freenom or use Railway/Render provided domains
