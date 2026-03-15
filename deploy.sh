#!/bin/bash

# Novel Platform Deployment Script

set -e

echo "🚀 Novel Platform Deployment"
echo "============================"

# Check if .env exists
if [ ! -f .env ]; then
    echo "❌ .env file not found!"
    echo "   Copy .env.example to .env and fill in your values:"
    echo "   cp .env.example .env"
    exit 1
fi

# Load environment variables
source .env

# Validate required variables
if [ -z "$DATABASE_PASSWORD" ] || [ "$DATABASE_PASSWORD" = "your-secure-password-here" ]; then
    echo "❌ Please set DATABASE_PASSWORD in .env"
    exit 1
fi

if [ -z "$JWT_SECRET" ] || [ ${#JWT_SECRET} -lt 64 ]; then
    echo "❌ JWT_SECRET must be at least 64 characters"
    exit 1
fi

echo "✅ Environment validated"

# Build and deploy
echo "📦 Building and deploying containers..."
docker compose -f docker-compose.prod.yml up -d --build

echo ""
echo "✅ Deployment complete!"
echo "   Web app: http://localhost"
echo "   Backend: http://localhost/api"
echo ""
echo "📋 Useful commands:"
echo "   View logs: docker compose -f docker-compose.prod.yml logs -f"
echo "   Stop: docker compose -f docker-compose.prod.yml down"
