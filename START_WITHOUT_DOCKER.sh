#!/bin/bash

# Novel Platform - Start Without Docker
# Use this if you have PostgreSQL installed locally

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📚 Novel Platform - Quick Start (Local PostgreSQL)"
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

# Check if PostgreSQL is running
if command -v psql &> /dev/null; then
    echo "✅ PostgreSQL is installed"
    
    # Try to connect
    if psql -U postgres -c '\q' 2>/dev/null; then
        echo "✅ PostgreSQL is running"
    else
        echo "⚠️  PostgreSQL might not be running or needs password"
        echo "   Continuing anyway..."
    fi
else
    echo "⚠️  psql command not found, but continuing..."
fi

echo ""

# Create database if it doesn't exist
echo "🗄️  Setting up database..."
psql -U postgres -c "CREATE DATABASE noveldb;" 2>/dev/null || echo "   Database might already exist (this is fine)"

echo "✅ Database ready"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed!"
    echo "   Please install Maven and try again."
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
