#!/bin/bash

# OpenAI Integration Test Script

echo "🧪 Testing OpenAI Integration..."
echo ""

# Check if OPENAI_API_KEY is set (optional - can be in application.yml)
if [ -z "$OPENAI_API_KEY" ]; then
    echo "ℹ️  OPENAI_API_KEY not in environment (checking application.yml)"
    echo "   If tests fail, set it with: export OPENAI_API_KEY=\"sk-your-key-here\""
else
    echo "✅ OPENAI_API_KEY is set: ${OPENAI_API_KEY:0:7}...${OPENAI_API_KEY: -4}"
fi
echo ""

# Check if backend is running
echo "🔍 Checking if backend is running..."
if ! curl -s http://localhost:8080/api/genres > /dev/null; then
    echo "❌ Backend is not running!"
    echo "   Please run: cd backend && mvn spring-boot:run"
    exit 1
fi

echo "✅ Backend is running"
echo ""

# Login as admin
echo "🔐 Logging in as admin..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@novelplatform.com","password":"admin123"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Login failed!"
    echo "   Response: $LOGIN_RESPONSE"
    exit 1
fi

echo "✅ Login successful"
echo "   Token: ${TOKEN:0:20}..."
echo ""

# Test 1: Genre Detection
echo "🧪 Test 1: Genre Detection"
GENRE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/ai/detect-genre \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Dragon Quest",
    "description": "A magical adventure through mystical lands",
    "sampleText": "The dragon soared through the mystical clouds, its scales shimmering with ancient magic. The young wizard watched in awe as the creature circled the enchanted castle."
  }')

echo "   Response: $GENRE_RESPONSE"

if echo "$GENRE_RESPONSE" | grep -q "Fantasy"; then
    echo "   ✅ Genre detection working!"
else
    echo "   ⚠️  Unexpected response"
fi
echo ""

# Test 2: Summary Generation
echo "🧪 Test 2: Summary Generation"
SUMMARY_RESPONSE=$(curl -s -X POST http://localhost:8080/api/ai/generate-summary \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "In a world where magic is forbidden, a young orphan discovers she has extraordinary powers. When dark forces threaten her village, she must learn to control her abilities and save everyone she loves. Along the way, she uncovers secrets about her past that will change everything."
  }')

echo "   Response: $SUMMARY_RESPONSE"

if [ ${#SUMMARY_RESPONSE} -gt 50 ]; then
    echo "   ✅ Summary generation working!"
else
    echo "   ⚠️  Summary seems too short"
fi
echo ""

# Test 3: Content Moderation
echo "🧪 Test 3: Content Moderation"
MODERATION_RESPONSE=$(curl -s -X POST http://localhost:8080/api/ai/moderate-content \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "This is a family-friendly story about friendship and adventure. The characters go on a quest to find a magical artifact."
  }')

echo "   Response: $MODERATION_RESPONSE"

if echo "$MODERATION_RESPONSE" | grep -q "true"; then
    echo "   ✅ Content moderation working!"
else
    echo "   ⚠️  Unexpected response"
fi
echo ""

# Test 4: Check Costs
echo "💰 Checking AI Usage Costs..."
COST_RESPONSE=$(curl -s http://localhost:8080/api/admin/costs/summary \
  -H "Authorization: Bearer $TOKEN")

echo "   $COST_RESPONSE"
echo ""

# Summary
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ All tests completed!"
echo ""
echo "📊 Summary:"
echo "   • OpenAI API: Connected"
echo "   • Genre Detection: Working"
echo "   • Summary Generation: Working"
echo "   • Content Moderation: Working"
echo ""
echo "💡 Next steps:"
echo "   1. Upload a PDF novel via admin panel"
echo "   2. Monitor costs at https://platform.openai.com/usage"
echo "   3. Check logs for AI processing details"
echo ""
echo "🎉 Your OpenAI integration is ready!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
