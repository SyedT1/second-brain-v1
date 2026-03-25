# Second Brain API

A personal knowledge management backend built with Spring Boot. Capture any piece of information (notes, links, ideas, files) via REST endpoints and let AI automatically organize, tag, and make everything searchable.

## Features

### 🧠 Three-Layer Intelligence Pipeline

1. **Instant Capture** - Save anything via REST API, no waiting
2. **Async Enrichment** - Background processing automatically:
   - Generates tags and summaries using LLM (Groq/OpenAI)
   - Creates semantic embeddings with pgvector
   - Scrapes link metadata with Jsoup
3. **Smart Search** - Three search modes working together:
   - **Full-text search** - PostgreSQL FTS for exact keyword matching
   - **Semantic search** - pgvector cosine similarity for "show me things like this"
   - **Hybrid search** - Combines both for best results

### 📦 Core Capabilities

- **CRUD Operations** - Create, read, update, delete entries
- **Multiple Entry Types** - NOTE, LINK, IDEA, FILE
- **Auto-tagging** - AI-generated tags from content
- **Auto-summarization** - Concise summaries for quick scanning
- **Link Enrichment** - Automatic extraction of Open Graph metadata
- **Vector Similarity** - Find related entries even without exact keywords
- **Tag-based Search** - Browse by auto-generated or manual tags

## Tech Stack

- **Spring Boot 4.0.4** - Modern Java framework
- **PostgreSQL + pgvector** - Database with vector similarity search
- **Spring AI 2.0** - AI integration (OpenAI/Groq)
- **Jsoup** - HTML parsing and metadata extraction
- **Flyway** - Database migrations
- **Lombok** - Reduce boilerplate code

## Project Structure

```
src/main/java/com/secondbrain/
├── config/
│   ├── AsyncConfig.java          # @EnableAsync + thread pool
│   ├── AiConfig.java             # Spring AI ChatClient bean
│   └── SecurityConfig.java       # CORS configuration
│
├── controller/
│   ├── EntryController.java      # POST /entries, GET /entries/:id
│   └── SearchController.java     # GET /search, GET /similar/:id
│
├── service/
│   ├── EntryService.java         # Save, fetch, delete operations
│   ├── EnrichmentService.java    # Orchestrates tagging + embedding
│   ├── TaggingService.java       # Calls LLM → returns tags + summary
│   ├── EmbeddingService.java     # Generates + stores pgvector embeddings
│   └── LinkEnricherService.java  # Jsoup → og:title, description
│
├── repository/
│   ├── EntryRepository.java      # JPA + custom @Query for FTS
│   └── EntrySearchRepository.java# Native SQL for vector search
│
├── model/
│   ├── Entry.java                # @Entity - core table
│   └── EntryType.java            # Enum: NOTE, LINK, IDEA, FILE
│
├── dto/
│   ├── CreateEntryRequest.java   # Inbound payload + @Valid
│   ├── EntryResponse.java        # Outbound shape
│   └── SearchResponse.java       # Search results + scores
│
├── event/
│   └── EntryCreatedEvent.java    # Spring ApplicationEvent
│
└── SecondBrainApplication.java
```

## Prerequisites

1. **Java 17+**
2. **PostgreSQL 15+** with pgvector extension
3. **OpenAI API key** or **Groq API key**
4. **Maven 3.8+**

## Setup

### 1. Install PostgreSQL and pgvector

```bash
# macOS
brew install postgresql pgvector

# Ubuntu/Debian
sudo apt-get install postgresql-15 postgresql-15-pgvector

# Start PostgreSQL
brew services start postgresql  # macOS
sudo systemctl start postgresql # Linux
```

### 2. Create Database

```bash
createdb secondbrain

# Enable pgvector extension
psql secondbrain -c "CREATE EXTENSION vector;"
```

### 3. Configure Environment Variables

Copy `.env.example` to `.env` (or set environment variables):

```bash
cp .env.example .env
```

Edit `.env`:

```bash
# Database
DB_USERNAME=postgres
DB_PASSWORD=your-password

# OpenAI (default)
OPENAI_API_KEY=sk-...

# OR use Groq instead
OPENAI_BASE_URL=https://api.groq.com/openai/v1
OPENAI_API_KEY=gsk_...
AI_CHAT_MODEL=llama3-8b-8192
```

**Note on Groq**: Groq doesn't provide embedding models yet, so you'll still need OpenAI for embeddings. You can use Groq for tagging/summarization by setting the base URL.

### 4. Build and Run

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`

## API Endpoints

### Entry Management

#### Create Entry
```bash
POST /api/entries
Content-Type: application/json

{
  "type": "NOTE",
  "title": "Spring Boot Tips",
  "content": "Always use constructor injection instead of field injection for better testability.",
  "metadata": {}
}
```

#### Get Entry
```bash
GET /api/entries/{id}
```

#### Get All Entries
```bash
GET /api/entries
```

#### Delete Entry
```bash
DELETE /api/entries/{id}
```

### Search

#### Full-Text Search
```bash
GET /api/search?query=spring+boot&mode=fts&limit=20
```

#### Semantic Search
```bash
GET /api/search?query=dependency+injection&mode=semantic&limit=20
```

#### Hybrid Search (default)
```bash
GET /api/search?query=async+processing&limit=20
```

#### Find Similar Entries
```bash
GET /api/search/similar/{id}?limit=10
```

#### Search by Tag
```bash
GET /api/search/tags/java
```

## Search Modes Explained

1. **FTS (Full-Text Search)** - Uses PostgreSQL's `tsvector` for exact keyword matching
   - Best for: Finding specific terms, names, or technical keywords
   - Example: "Spring Boot", "PostgreSQL", "dependency injection"

2. **Semantic Search** - Uses pgvector cosine similarity on embeddings
   - Best for: Conceptual searches, finding related ideas
   - Example: "how to improve performance" matches entries about caching, optimization, etc.

3. **Hybrid** (Recommended) - Combines both FTS and semantic search
   - Best for: Most use cases - balances precision and recall
   - Weights: 50% FTS + 50% semantic similarity

## How It Works

### Entry Creation Flow

```
1. POST /api/entries
   └─> EntryController.createEntry()
       └─> EntryService.save()
           ├─> Save to database (immediate)
           └─> Publish EntryCreatedEvent

2. Background (async via @EventListener)
   └─> EnrichmentService.handleEntryCreated()
       ├─> TaggingService.generateTagsAndSummary()
       │   └─> Calls LLM (Groq/OpenAI)
       ├─> EmbeddingService.generateEmbedding()
       │   └─> Calls OpenAI text-embedding-3-small
       └─> LinkEnricherService.enrichLinkMetadata() (if type=LINK)
           └─> Jsoup scrapes Open Graph tags
```

### Search Flow

```
Hybrid Search:
1. Generate embedding for query
2. Execute two queries in parallel:
   ├─> Full-text search with ts_rank
   └─> Vector similarity with pgvector
3. Combine results with scoring:
   └─> final_score = (fts_score * 0.5) + (similarity_score * 0.5)
4. Return top N results
```

## Database Schema

```sql
CREATE TABLE entries (
    id                BIGSERIAL PRIMARY KEY,
    type              VARCHAR(20) NOT NULL,
    content           TEXT NOT NULL,
    title             VARCHAR(500),
    url               TEXT,
    file_path         TEXT,
    metadata          JSONB DEFAULT '{}'::jsonb,
    tags              JSONB DEFAULT '[]'::jsonb,
    summary           TEXT,
    embedding         vector(1536),
    content_tsvector  tsvector,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    enriched_at       TIMESTAMP
);

-- Indexes
CREATE INDEX idx_entries_embedding ON entries
    USING ivfflat(embedding vector_cosine_ops) WITH (lists = 100);
CREATE INDEX idx_entries_content_tsvector ON entries
    USING GIN(content_tsvector);
CREATE INDEX idx_entries_tags ON entries
    USING GIN(tags);
```

## Configuration

Key settings in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/secondbrain

# AI Models
spring.ai.openai.chat.options.model=gpt-4o-mini
spring.ai.openai.embedding.options.model=text-embedding-3-small

# Async Thread Pool
spring.task.execution.pool.core-size=2
spring.task.execution.pool.max-size=5

# PGVector
spring.ai.vectorstore.pgvector.dimensions=1536
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE
```

## Frontend Integration

This is a backend-only API. You can build any frontend:

- **React/Vue/Svelte** web app
- **CLI** for quick capture from terminal
- **Raycast extension** for macOS
- **Obsidian plugin** to sync with your vault
- **Mobile app** (React Native/Flutter)

All you need is HTTP client to call the REST endpoints.

## Example Usage

### Save a Quick Note
```bash
curl -X POST http://localhost:8080/api/entries \
  -H "Content-Type: application/json" \
  -d '{
    "type": "NOTE",
    "content": "Remember to review the Spring AI documentation for vector store configuration"
  }'
```

### Save a Link
```bash
curl -X POST http://localhost:8080/api/entries \
  -H "Content-Type: application/json" \
  -d '{
    "type": "LINK",
    "url": "https://spring.io/blog/2024/03/spring-ai-announcement",
    "content": "Spring AI announcement - new framework for building AI-powered applications"
  }'
```

### Search Your Brain
```bash
# Semantic search
curl "http://localhost:8080/api/search?query=vector+databases&mode=semantic"

# Find similar to an entry
curl "http://localhost:8080/api/search/similar/42"
```

## Development

### Run Tests
```bash
./mvnw test
```

### Hot Reload
Spring Boot DevTools is included - code changes will auto-reload.

### Database Migrations

Flyway migrations are in `src/main/resources/db/migration/`

Create new migration:
```bash
# Create V2__add_new_feature.sql
touch src/main/resources/db/migration/V2__add_new_feature.sql
```

## Troubleshooting

### Database Connection Failed
- Ensure PostgreSQL is running: `pg_ctl status`
- Check credentials in `.env`
- Verify database exists: `psql -l | grep secondbrain`

### pgvector Extension Missing
```bash
psql secondbrain -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### Enrichment Not Working
- Check OpenAI/Groq API key is set
- Look at logs: `logging.level.com.secondbrain=DEBUG`
- Verify async executor is running

### Slow Searches
- Ensure indexes are created (happens via Flyway migration)
- For large datasets, tune pgvector `lists` parameter
- Consider adding more specific indexes for your use case

## Roadmap

- [ ] Bulk import from markdown files
- [ ] Export to Obsidian/Notion format
- [ ] OCR for image entries
- [ ] Audio transcription for voice notes
- [ ] Graph view of related entries
- [ ] Scheduled summaries (daily digest)
- [ ] Multi-user support with authentication

## License

MIT License - feel free to use this as your personal knowledge backend!

## Contributing

This is a personal project, but PRs are welcome for bug fixes and improvements.

---

**Built with ❤️ for personal knowledge management**
