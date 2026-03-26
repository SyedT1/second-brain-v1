#!/bin/bash

# Second Brain API - Startup Script
# This script loads environment variables from .env and starts the application

echo "🚀 Starting Second Brain API..."

# Load environment variables from .env file
if [ -f .env ]; then
    echo "📝 Loading environment variables from .env..."
    set -a
    source .env
    set +a
    echo "✓ Environment variables loaded"
else
    echo "⚠️  Warning: .env file not found!"
    exit 1
fi

# Verify GROQ_API_KEY is set
if [ -z "$GROQ_API_KEY" ]; then
    echo "❌ Error: GROQ_API_KEY not set in .env file"
    exit 1
fi

echo "✓ GROQ_API_KEY found: ${GROQ_API_KEY:0:20}..."

# Start the application
echo "🏃 Starting Spring Boot application..."
./mvnw spring-boot:run
