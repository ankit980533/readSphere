#!/bin/bash

# Novel Platform - Stop Script

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📚 Novel Platform - Stopping Services"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Stop database
echo "🗄️  Stopping PostgreSQL database..."
docker-compose down

if [ $? -eq 0 ]; then
    echo "✅ Database stopped successfully"
else
    echo "⚠️  Failed to stop database (may not be running)"
fi

echo ""
echo "✅ All services stopped"
echo ""
echo "To start again, run: ./START.sh"
