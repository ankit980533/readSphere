# Quick Start with OpenAI

## Step-by-Step Setup (5 minutes)

### 1. Get OpenAI API Key

1. **Sign up for OpenAI:**
   - Go to https://platform.openai.com/signup
   - Create account (email + password)
   - **Get $5 free credit** (no credit card required initially)

2. **Create API Key:**
   - Go to https://platform.openai.com/api-keys
   - Click "Create new secret key"
   - Name it: "novel-platform"
   - **Copy the key** (starts with `sk-`)
   - ⚠️ Save it somewhere safe - you won't see it again!

### 2. Configure Your Application

**Option A: Environment Variable (Recommended)**
```bash
export OPENAI_API_KEY="sk-your-actual-key-here"
```

**Option B: Add to .env file**
```bash
# Create .env file in backend/
echo "OPENAI_API_KEY=sk-your-actual-key-here" > backend/.env
```

**Option C: Direct in application.yml (Not recommended for production)**
```yaml
openai:
  api:
    key: sk-your-actual-key-here
```

### 3. Verify Configuration

```bash
# Check environment variable is set
echo $OPENAI_API_KEY

# Should output: sk-your-key...
```

### 4. Start the Application

```bash
# Start database
docker-compose up -d

# Start backend
cd backend
mvn spring-boot:run
```

You should see in logs:
```
AI Service initialized with provider: openai
OpenAI API configured successfully
```

### 5. Test AI Features

**Test 1: Login as Admin**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@novelplatform.com","password":"admin123"}'
```

Copy the token from response.

**Test 2: Test Genre Detection**
```bash
TOKEN="your-token-here"

curl -X POST http://localhost:8080/api/ai/detect-genre \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "The Dragon Quest",
    "description": "A magical adventure",
    "sampleText": "The dragon soared through mystical clouds..."
  }'
```

Expected response:
```json
{
  "genre": "Fantasy"
}
```

**Test 3: Upload a PDF Novel**
```bash
curl -X POST http://localhost:8080/api/admin/upload-pdf \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/your/novel.pdf" \
  -F "title=My First Novel" \
  -F "author=Author Name" \
  -F "genreId=2" \
  -F "description=An amazing story"
```

AI will automatically:
- ✅ Detect chapters
- ✅ Moderate content
- ✅ Generate summary (if description empty)
- ✅ Detect genre (if genreId not provided)

### 6. Monitor Costs

```bash
curl http://localhost:8080/api/admin/costs/summary \
  -H "Authorization: Bearer $TOKEN"
```

Response:
```json
{
  "totalCalls": 4,
  "inputTokens": 21500,
  "outputTokens": 800,
  "totalCost": 0.012
}
```

## Troubleshooting

### Error: "Invalid API key"
```bash
# Check your key is set correctly
echo $OPENAI_API_KEY

# Make sure it starts with "sk-"
# Verify at https://platform.openai.com/api-keys
```

### Error: "Rate limit exceeded"
```bash
# Free tier limits:
# - 3 requests per minute
# - 200 requests per day

# Wait 60 seconds and try again
```

### Error: "Insufficient quota"
```bash
# Check your usage at https://platform.openai.com/usage
# You may have exhausted your $5 credit

# Add payment method at https://platform.openai.com/account/billing
```

### AI calls are slow
```bash
# Normal response times:
# - Chapter Detection: 1-2 seconds
# - Genre Detection: 0.5-1 second
# - Summary Generation: 1-2 seconds

# If slower, check your internet connection
```

## Configuration Options

### Use GPT-4 (Better Quality, Higher Cost)
```yaml
openai:
  api:
    model: gpt-4  # Default is gpt-3.5-turbo
```

Cost: ~$0.12 per novel (10x more expensive)

### Disable Specific AI Features
```yaml
ai:
  enabled: true
  features:
    chapter-detection: true
    genre-detection: false  # Disable if you always provide genre
    summary-generation: false  # Disable if you always provide description
    content-moderation: true
```

### Adjust AI Temperature (Creativity)
```java
// In AiService.java
requestBody.put("temperature", 0.3);  // Lower = more consistent (0.0-1.0)
```

## Cost Management

### Set Billing Alerts
1. Go to https://platform.openai.com/account/billing/limits
2. Set soft limit: $10
3. Set hard limit: $20
4. Enable email notifications

### Monitor Usage
- Dashboard: https://platform.openai.com/usage
- Check daily usage
- Track costs per project

### Optimize Costs
```yaml
# Use cheaper model
openai:
  api:
    model: gpt-3.5-turbo-instruct  # 50% cheaper

# Reduce sample sizes
ai:
  sample-sizes:
    chapter-detection: 5000  # words
    genre-detection: 500
    summary-generation: 3000
```

## Production Checklist

- [ ] API key stored in environment variable (not in code)
- [ ] Billing alerts configured
- [ ] Hard limit set ($20 recommended)
- [ ] Cost tracking enabled
- [ ] Error handling tested
- [ ] Fallback to regex if AI fails
- [ ] Rate limiting implemented
- [ ] Logs configured

## Next Steps

1. ✅ Upload your first novel
2. ✅ Check the AI-generated chapters
3. ✅ Review the auto-generated summary
4. ✅ Monitor costs in dashboard
5. ✅ Deploy to production

## Support

- OpenAI Docs: https://platform.openai.com/docs
- API Status: https://status.openai.com
- Community: https://community.openai.com

## Summary

You're all set! 🎉

```bash
# Quick recap:
export OPENAI_API_KEY="sk-your-key"
docker-compose up -d
cd backend && mvn spring-boot:run

# Upload novels and let AI do the magic!
```

**Your 200 novels will be processed for FREE with the $5 credit!**
