#!/bin/bash

# Novel Platform - Quick Start Script

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📚 Novel Platform - Quick Start"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if OPENAI_API_KEY is set (optional - can be in application.yml)
if [ -z "$OPENAI_API_KEY" ]; then
    echo "ℹ️  OpenAI API key not found in environment variable"
    echo "   Will use key from application.yml if configured"
    echo ""
else
    echo "✅ OpenAI API key found in environment"
    echo ""
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running!"
    echo "   Please start Docker Desktop and try again."
    exit 1
fi

echo "✅ Docker is running"
echo ""

# Start database
echo "🗄️  Starting PostgreSQL database..."
docker-compose up -d

if [ $? -eq 0 ]; then
    echo "✅ Database started successfully"
else
    echo "❌ Failed to start database"
    exit 1
fi

echo ""
echo "⏳ Waiting for database to be ready..."
sleep 5

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed!"
    echo "   Please install Maven and try again."
    echo "   Visit: https://maven.apache.org/install.html"
    exit 1
fi

echo "✅ Maven is installed"
echo ""

# Start backend
echo "🚀 Starting backend server..."
echo "   This may take a few minutes on first run..."
echo ""

cd backend

# Check if it's first run
if [ ! -d "target" ]; then
    echo "📦 First run detected - downloading dependencies..."
    echo "   This will take 2-5 minutes..."
    echo ""
fi

mvn spring-boot:run

# This line will only execute if mvn fails or is stopped
echo ""
echo "Backend stopped."
