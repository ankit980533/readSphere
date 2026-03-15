#!/bin/bash

# Novel Upload Script
# Usage: ./upload-novel.sh <pdf-file-path>

if [ -z "$1" ]; then
    echo "❌ Error: No PDF file specified"
    echo ""
    echo "Usage: ./upload-novel.sh <pdf-file-path>"
    echo ""
    echo "Example:"
    echo "  ./upload-novel.sh /path/to/my-novel.pdf"
    exit 1
fi

PDF_FILE="$1"

if [ ! -f "$PDF_FILE" ]; then
    echo "❌ Error: File not found: $PDF_FILE"
    exit 1
fi

echo "📚 Novel Upload Tool"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if backend is running
echo "🔍 Checking backend..."
if ! curl -s http://localhost:8080/api/genres > /dev/null; then
    echo "❌ Backend is not running!"
    echo "   Please run: ./START_WITHOUT_DOCKER.sh"
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
echo ""

# Get novel details
echo "📝 Enter novel details:"
echo ""
read -p "Novel Title: " TITLE
read -p "Author Name: " AUTHOR
read -p "Description: " DESCRIPTION

echo ""
echo "🚀 Uploading novel..."
echo "   File: $PDF_FILE"
echo "   Title: $TITLE"
echo "   Author: $AUTHOR"
echo ""

# Upload the novel
UPLOAD_RESPONSE=$(curl -s -X POST http://localhost:8080/api/novels/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@$PDF_FILE" \
  -F "title=$TITLE" \
  -F "author=$AUTHOR" \
  -F "description=$DESCRIPTION")

echo "Response: $UPLOAD_RESPONSE"
echo ""

# Check if upload was successful
if echo "$UPLOAD_RESPONSE" | grep -q '"id"'; then
    NOVEL_ID=$(echo $UPLOAD_RESPONSE | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "✅ Novel uploaded successfully!"
    echo ""
    echo "📖 Novel ID: $NOVEL_ID"
    echo "📚 Title: $TITLE"
    echo "✍️  Author: $AUTHOR"
    echo ""
    echo "🤖 AI Processing:"
    echo "   • Chapters detected and split"
    echo "   • Genre automatically detected"
    echo "   • Summary generated"
    echo "   • Content moderated"
    echo ""
    echo "🔗 View novel:"
    echo "   http://localhost:8080/api/novels/$NOVEL_ID"
    echo ""
    echo "📱 Open in Android app to start reading!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
else
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "❌ Upload failed!"
    echo ""
    echo "Possible reasons:"
    echo "  • PDF file is too large (max 50MB)"
    echo "  • PDF file is corrupted"
    echo "  • Backend error (check logs)"
    echo ""
    echo "Full response:"
    echo "$UPLOAD_RESPONSE"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    exit 1
fi
