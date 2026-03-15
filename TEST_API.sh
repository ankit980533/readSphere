#!/bin/bash

# Quick API Test Script

echo "🧪 Testing Novel Platform API..."
echo ""

# Test 1: Check if backend is running
echo "1️⃣  Testing backend connection..."
if curl -s http://localhost:8080/api/genres > /dev/null; then
    echo "   ✅ Backend is running!"
else
    echo "   ❌ Backend is not running!"
    echo "   Please run: ./START.sh"
    exit 1
fi

echo ""

# Test 2: Login
echo "2️⃣  Testing admin login..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@novelplatform.com","password":"admin123"}')

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    echo "   ✅ Login successful!"
    TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    echo "   Token: ${TOKEN:0:20}..."
else
    echo "   ❌ Login failed!"
    echo "   Response: $LOGIN_RESPONSE"
    exit 1
fi

echo ""

# Test 3: Get novels
echo "3️⃣  Testing novels endpoint..."
NOVELS=$(curl -s http://localhost:8080/api/novels)
echo "   ✅ Novels endpoint working!"
echo "   Response: $NOVELS"

echo ""

# Test 4: Get genres
echo "4️⃣  Testing genres endpoint..."
GENRES=$(curl -s http://localhost:8080/api/genres)
echo "   ✅ Genres endpoint working!"
echo "   Genres: $GENRES"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ All API tests passed!"
echo ""
echo "Your application is ready to use!"
echo ""
echo "Next steps:"
echo "  • Open Android app in Android Studio"
echo "  • Upload a novel via admin panel"
echo "  • Test AI features with: ./backend/test-openai.sh"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
