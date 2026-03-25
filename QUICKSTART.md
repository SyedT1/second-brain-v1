# Second Brain API - Quick Start Guide

## Prerequisites Check

Before you begin, make sure you have:

- [ ] Java 17 or higher installed
- [ ] PostgreSQL 15+ running
- [ ] OpenAI or Groq API key
- [ ] Maven (or use included `./mvnw`)

## 5-Minute Setup

### 1. Clone and Navigate
```bash
cd /home/thinker/IdeaProjects/second-brain-v1
```

### 2. Setup PostgreSQL Database
```bash
# Create database
createdb secondbrain

# Enable pgvector extension
psql secondbrain -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### 3. Configure Environment
```bash
# Copy example env file
cp .env.example .env

# Edit .env with your credentials
nano .env
```

Required variables:
```bash
DB_USERNAME=postgres
DB_PASSWORD=your-password
OPENAI_API_KEY=sk-your-key-here
```

### 4. Run the Application
```bash
# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# Or if Maven is installed
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

### 5. Test It Works

Create your first entry:
```bash
curl -X POST http://localhost:8080/api/entries \
  -H "Content-Type: application/json" \
  -d '{
    "type": "NOTE",
    "title": "My First Note",
    "content": "This is a test note to verify everything works!"
  }'
```

Search for it:
```bash
curl "http://localhost:8080/api/search?query=test"
```

## Using Groq Instead of OpenAI

To use Groq's llama3-8b model for cheaper/faster tagging:

1. Get a Groq API key from https://console.groq.com
2. Update your `.env`:
```bash
OPENAI_BASE_URL=https://api.groq.com/openai/v1
OPENAI_API_KEY=gsk_your-groq-key
AI_CHAT_MODEL=llama3-8b-8192
```

**Note**: You still need OpenAI for embeddings since Groq doesn't provide embedding models.

## Troubleshooting

### "Database does not exist"
```bash
createdb secondbrain
```

### "extension vector does not exist"
```bash
# Install pgvector first
brew install pgvector  # macOS
sudo apt-get install postgresql-15-pgvector  # Ubuntu

# Then enable it
psql secondbrain -c "CREATE EXTENSION vector;"
```

### "Connection refused" to PostgreSQL
```bash
# Start PostgreSQL
brew services start postgresql  # macOS
sudo systemctl start postgresql # Linux
```

### Port 8080 already in use
Change port in `application.properties`:
```properties
server.port=8081
```

## Next Steps

- Read the full [README.md](README.md) for API documentation
- Try different search modes: `?mode=fts`, `?mode=semantic`, `?mode=hybrid`
- Build a frontend or CLI to interact with the API
- Import your existing notes/links

## API Quick Reference

```bash
# Create entry
POST /api/entries

# Get entry
GET /api/entries/{id}

# Search
GET /api/search?query={query}&mode={fts|semantic|hybrid}

# Find similar
GET /api/search/similar/{id}

# Search by tag
GET /api/search/tags/{tag}
```

Enjoy your Second Brain! 🧠
