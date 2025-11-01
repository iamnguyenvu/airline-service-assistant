Airline Service Assistant — Mô tả chi tiết (VNA-first, multi-airline ready)
1) Mục tiêu & Triết lý

Mục tiêu: Trợ lý AI (VI/EN) “hành xử như nhân viên CSKH của hãng”: tư vấn, tìm/đặt/giữ/hủy, trả lời quy định vé/hành lý, gợi ý rebook khi gián đoạn, kiểm tra trạng thái chuyến bay gần realtime.

Triết lý: Tool-first, no-hallucination — mọi thông tin giá/chính sách đều có citation (nguồn + ngày snapshot / version policy).

2) Đối tượng & Use-cases

Khách lẻ: tìm → đặt/giữ → hỏi quy định → hủy/đổi.

CSKH/Đại lý: tra điều kiện vé, phí đổi/hoàn ước tính, rebook nhanh.

Use-cases chính

Tìm chuyến (one-way/round-trip), lọc theo ngân sách/giờ/hành lý/hãng.

Explainable ranking: điểm tổng hợp + breakdown (Giá/Thời lượng/CO₂/Sở thích).

Policy Q&A (đổi/hoàn/no-show/baggage/đồ đặc biệt) kèm nguồn & version.

Booking mock: tạo PNR, PDF e-ticket, hold timer.

IRROPS: delay/cancel → đề xuất rebook + ước tính phí/điều kiện.

Live status (nhẹ): cache 60–120s.

User prefs: ghế, ngân sách, hãng ưa thích → tự áp dụng vào ranking.

3) Phạm vi dữ liệu & Tuân thủ

Policy: thu thập từ tài liệu công khai (PDF/HTML), parse → fare_rule_norm, đồng thời chunk & embed cho RAG (pgvector).

Flight snapshots: cập nhật hằng ngày khung T+1 → T+30 cho các tuyến phổ biến; tùy chọn live status theo yêu cầu.

Compliance: tôn trọng robots.txt/ToS; lưu raw_payload có kiểm soát; hiển thị disclaimer với số liệu ước tính.

4) Kiến trúc tổng thể

Frontend: Next.js (App Router) + Tailwind + shadcn/ui, TanStack Query, Supabase (Auth/Realtime/Storage).

Backend: Spring Boot 3.5.x + Spring AI (ChatClient + Tool Calling).

Modules:

ai-orchestrator (POST /api/chat/ask)

flight-service (GET /api/flights/search, POST /api/booking/confirm, prefs)

ingestion-service (scheduler snapshot & policy ingestion)

infra (Flyway migrations)

Data: PostgreSQL 16 + pgvector, Redis cache; Supabase Storage lưu PDF vé & policy gốc.

5) Orchestration & Ranking

System prompt bắt buộc dùng tools, luôn trả citation.

Tools:

searchFlights(params) → đọc snapshot mới nhất theo tuyến/ngày.

getFareRule(airline,cabin,fare_family) → đọc fare_rule_norm (chuẩn hóa).

ragPolicy(query, airline?) → vector search service_doc_chunks.

estimateCO2(from,to,aircraft?) → ước tính great-circle.

liveStatus(flightNo,date?) → provider nhẹ + cache.

Scoring (mặc định): score = 0.4*price + 0.3*duration + 0.2*co2 + 0.1*prefs (chuẩn hóa theo tuyến/ngày).
UI hiển thị breakdown + badge 🌿 CO₂ (so với trung vị tuyến).

6) API bề mặt

POST /api/chat/ask → NL → tools → câu trả lời có giải thích + citations.

GET /api/flights/search?from=SGN&to=HAN&date=YYYY-MM-DD&… → danh sách + điểm + snapshot meta.

POST /api/booking/confirm → tạo PNR mock + PDF e-ticket (signed URL).

GET/PUT /api/prefs/{userId} → sở thích người dùng.

7) Mô hình dữ liệu (chính)

flight_snapshot(id, snapshot_date, dep_iata, arr_iata, dep_time, arr_time, carrier, flight_no, duration_min, stops, fare_family, baggage_kg, price_cents, currency, source, created_at)

flight_raw(snapshot_id → flight_snapshot.id, payload jsonb, created_at) ❶

route_stats_daily(route_key, date, min/p50/p90_price_cents, avg_duration_min, avg_co2_kg)

service_docs(id, source_url, airline_code, doc_type, version_tag, raw_text?, storage_url?, created_at) ❷

service_doc_chunks(id, doc_id, section_title, chunk, embedding vector)

fare_rule_norm(id, airline_code, cabin, fare_family, change_fee, refund_fee, refund_allowed, change_deadline, no_show_fee, carry_on_kg, checked_bag_kg, effective_from, effective_to)

bookings_mock(pnr, user_id, flight_snapshot_id, status, pdf_url, payload, created_at)

users_public(id, full_name, created_at)

Chú thích quan trọng
❶ Tách flight_raw để không “độn” DB: cho phép TTL ngắn ngày.
❷ Policy gốc (PDF/HTML) ưu tiên đẩy lên Storage (trường storage_url) thay vì nhét toàn bộ raw_text vào DB.

8) Chiến lược lưu lịch sử 3 tháng → 1 năm (đảm bảo vừa quota, vừa tra cứu được)

Phân tầng dữ liệu (đã kiểm chứng thực dụng):

HOT (0–90 ngày)

Bảng flight_snapshot (skinny) + flight_raw (payload chi tiết).

Mục tiêu: trả lời nhanh, audit đầy đủ, phục vụ so sánh ngắn hạn.

TTL flight_raw = 90 ngày (xóa payload cũ, vẫn giữ row skinny).

WARM (91–365 ngày)

Giữ flight_snapshot dạng skinny (không payload), đủ trường: thời gian, giá, duration, baggage, fare_family, source, snapshot_date.

Duy trì route_stats_daily (min/p50/p90, avg_duration, avg_co2).

Cho phép truy vấn lịch sử đến 1 năm với chi tiết cơ bản.

COLD (> 1 năm) — tùy chọn

Kết xuất Parquet theo tháng/tuyến (ví dụ: year=2025/month=11/route=SGN-HAN.parquet) lên Supabase Storage.

Giữ trong DB chỉ bảng thống kê (daily/monthly). Khi cần “đào sâu” quá khứ xa, tải Parquet để phân tích.

Kỹ thuật tiết kiệm dung lượng

Partition theo tháng cho flight_snapshot (Postgres 16), xoá partition cũ rất nhanh.

Index tối thiểu: (dep_iata, arr_iata, snapshot_date) và (tuỳ) (dep_iata, arr_iata, dep_time); tránh index thừa.

Chuẩn hóa cột nhỏ (enum/short code) cho currency, carrier, fare_family.

Policy gốc lưu Storage + trường version_tag trong DB; DB chỉ lưu chunks + embeddings.

Kết quả:

3 tháng: giữ đầy đủ skinny + raw → truy vấn giàu chi tiết.

1 năm: giữ skinny + stats → vẫn trả lời được xu hướng/so sánh, không “phình” DB.

Có thể mở rộng nhiều năm bằng Parquet mà không tốn DB.

9) Bảo mật & Tuân thủ

Auth: Supabase OAuth (Google/GitHub); Backend là Resource Server verify JWKS.

Phân quyền: RLS cho bảng đọc từ frontend; backend enforce userId theo token.

Secrets: .env (local), GitHub Secrets (CI); không commit khoá.

ToS/robots: chỉ crawl policy công khai; giá/lịch dùng snapshots từ provider hợp lệ; UI/answer luôn hiển thị nguồn.

10) NFR & SLO

Hiệu năng: p95 < 1.2s cho /api/chat/ask & /flights/search (DB/cache).

Độ tin cậy: snapshot job thành công ≥99%/tháng (retry + backoff).

Quan sát: OpenTelemetry traces; Prometheus metrics; Grafana dashboard.

Chất lượng: Testcontainers (Postgres/Redis), RestAssured, ESLint/Prettier, Spotless/Checkstyle.

11) Tech stack (tóm tắt)

Backend: Java 21, Spring Boot 3.5.x, Spring AI, Web/Data/Validation, Quartz, Flyway, Redis, OpenPDF/iText, Tika/PDFBox/jsoup.

Data: Postgres 16 + pgvector; JSONB cho raw; partition + minimal indexes.

Frontend: Next.js 15 (App Router), Tailwind + shadcn/ui, TanStack Query, Supabase JS (Auth/Storage/Realtime).

DevOps: Docker Compose (db/redis), GitHub Actions CI, OpenTelemetry/Prometheus/Grafana.

TL;DR lưu trữ

Cam kết 3 tháng “đầy đủ” (skinny + raw), 1 năm “gọn nhẹ” (skinny + stats), và nhiều năm bằng Parquet.

Cấu trúc & job đã chuẩn để không lo vượt dung lượng, vẫn truy vấn nhanh và có số liệu lịch sử phục vụ explain/ranking/trends.


đây là bức tranh tech stack của Airline Service Assistant (VNA-first, mở rộng đa hãng) — kèm lý do chọn và vai trò từng mảnh:

Backend (Java / Spring)

Java 21 + Spring Boot 3.5.x: nền tảng dịch vụ REST, cấu hình gọn, ecosystem phong phú.

Spring Web, Validation: expose API, validate tham số search/booking.

Spring Data JPA (Hibernate): truy vấn Postgres, mapping entity rõ ràng.

Spring Security (Resource Server): xác thực JWT từ Supabase JWKS; bảo vệ API.

Spring AI 1.x (ChatClient + Tool Calling): điều phối AI “tool-first” (searchFlights, getFareRule, ragPolicy, estimateCO2, liveStatus).

Scheduler: @Scheduled / Quartz cho cron ingestion (snapshot flights, parse policy, rollup thống kê).

Cache: Redis 7 để cache kết quả search ngắn hạn & live status.

OpenAPI (springdoc): tài liệu API tự sinh (Swagger UI).

Lý do chọn Spring: chuẩn doanh nghiệp, dễ kiểm thử, dễ tách module (ai-orchestrator / flight-service / ingestion-service).

Data & Retrieval

PostgreSQL 16: dữ liệu giao dịch & lịch sử snapshot; JSONB cho payload linh hoạt.

pgvector: lưu embedding và tìm kiếm vector cho RAG (policy Q&A).

Schema “slim + raw”:

flight_snapshot (trường gọn, giữ lâu)

flight_raw (payload chi tiết, TTL 90 ngày)

fare_rule_norm, service_docs, service_doc_chunks, route_stats_daily.

Supabase Storage: lưu file PDF e-ticket, tài liệu policy gốc (PDF/HTML), xuất Parquet (lưu trữ dài hạn).

Parser: Apache Tika + PDFBox + jsoup để trích xuất & chuẩn hóa chính sách hãng.

(Tùy chọn) Parquet + DuckDB: nén lịch sử >1 năm, phân tích nhanh khi cần.

Lý do chọn Postgres + pgvector: một DB phục vụ cả OLTP nhẹ và RAG, giảm độ phức tạp hạ tầng.

Frontend (Web App)

Next.js 15 (App Router) + React 19 + TypeScript: SSR/ISR cho SEO & tốc độ; Route Handlers làm BFF proxy an toàn đến backend.

UI: Tailwind CSS + shadcn/ui (Radix UI) → phát triển nhanh, giao diện thống nhất.

Data fetching: TanStack Query (cache, retry, stale-while-revalidate).

Charts: Recharts (giá p50/p90, CO₂).

Auth: Supabase JS (OAuth Google/GitHub), đồng bộ session client/server.

Vì sao Next.js: landing/FAQ cần SEO, phần “chat/explain” cần streaming; BFF che API nội bộ & ký URL tải PDF.

AI & Orchestration (Zero-Cost Strategy)

Spring AI tích hợp Ollama (local model, miễn phí 100%): chạy Llama 3.1 8B hoặc Mistral 7B cho tool-calling.

Embeddings: sentence-transformers (all-MiniLM-L6-v2) chạy local qua REST API hoặc ONNX Runtime.

RAG trên service_doc_chunks (pgvector): câu trả lời policy luôn kèm citation (nguồn + version).

Prompting: system prompt "tool-first", format citation & giải thích điểm số flight.

Alternative: Sử dụng free-tier API như Groq (llama-3.1-8b-instant) với rate limit 30 req/min hoặc OpenRouter free models.

Observability & Quality

OpenTelemetry (traces), Prometheus/Grafana (metrics & dashboards).

Logging JSON + Correlation-ID qua các service.

Testing: JUnit 5, Testcontainers (Postgres/Redis) cho integration test; RestAssured (API); ESLint/Prettier (FE); Spotless/Checkstyle (BE).

DevOps

Docker Compose (local): Postgres + Redis + apps.

Flyway: quản lý migration DB chuẩn CI/CD.

GitHub Actions: lint/test/build cho FE/BE; đẩy image (ghcr) & deploy.

Secrets: .env local, GitHub Secrets; tuyệt đối không commit keys.

Bảo mật & Tuân thủ

JWT (Supabase JWKS): BE xác thực chuẩn OAuth2 Resource Server.

RLS (Supabase): nếu FE cần đọc DB trực tiếp (tối thiểu).

Rate-limit: Bucket4j (nếu mở public).

ToS/robots: chỉ crawl policy công khai; giá/lịch lấy từ provider hợp lệ; UI/response luôn hiển thị snapshot date & source.