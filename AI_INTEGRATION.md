# AI Integration Guide

## Overview

The platform uses AI for four key features:
1. **Chapter Detection** - Automatically split novels into chapters
2. **Genre Detection** - Identify novel genre from content
3. **Auto Summary** - Generate compelling descriptions
4. **Content Moderation** - Filter inappropriate content

## Current Implementation

### AI Provider: OpenAI GPT-3.5-turbo

The system uses OpenAI's API with fallback to regex-based detection if AI is disabled or fails.

## Configuration

### 1. Set OpenAI API Key

**Option A: Environment Variable (Recommended)**
```bash
export OPENAI_API_KEY="sk-your-api-key-here"
```

**Option B: application.yml**
```yaml
openai:
  api:
    key: sk-your-api-key-here
```

### 2. Enable/Disable AI

```yaml
ai:
  enabled: true  # Set to false to use regex fallback
```

## AI Features

### 1. Chapter Detection

**Endpoint:** `POST /api/ai/detect-chapters`

**Request:**
```json
{
  "text": "Chapter 1: The Beginning\n\nOnce upon a time..."
}
```

**Response:**
```json
[
  {
    "chapterNumber": 1,
    "title": "Chapter 1: The Beginning",
    "startPosition": 0,
    "endPosition": 1500
  }
]
```

**How it works:**
- AI analyzes text structure
- Identifies chapter markers
- Returns structured chapter data
- Falls back to regex if AI fails

### 2. Genre Detection

**Endpoint:** `POST /api/ai/detect-genre`

**Request:**
```json
{
  "title": "The Dragon's Quest",
  "description": "A young hero embarks on a magical journey",
  "sampleText": "The dragon soared through the mystical clouds..."
}
```

**Response:**
```json
{
  "genre": "Fantasy"
}
```

**Supported Genres:**
- Romance
- Fantasy
- Mystery
- Thriller
- Horror
- Sci-Fi
- Adventure
- Historical

### 3. Auto Summary Generation

**Endpoint:** `POST /api/ai/generate-summary`

**Request:**
```json
{
  "text": "Full novel text here..."
}
```

**Response:**
```json
{
  "summary": "A young prince begins a dangerous journey to reclaim his kingdom from dark forces."
}
```

**Features:**
- 2-3 sentence summaries
- Focuses on main plot hook
- Optimized for reader engagement

### 4. Content Moderation

**Endpoint:** `POST /api/ai/moderate-content`

**Request:**
```json
{
  "text": "Novel content to check..."
}
```

**Response:**
```json
{
  "isAppropriate": true,
  "issues": [],
  "severity": "low"
}
```

**Checks for:**
- Explicit violence
- Hate speech
- Spam content
- Adult content

**Severity Levels:**
- `low` - Minor issues
- `medium` - Moderate concerns
- `high` - Serious violations

## PDF Upload with AI

When uploading a PDF, AI is automatically used:

```bash
curl -X POST http://localhost:8080/api/admin/upload-pdf \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@novel.pdf" \
  -F "title=My Novel" \
  -F "author=Author Name" \
  -F "description=" \
  -F "genreId="
```

**AI Processing Flow:**
1. Extract text from PDF
2. **AI Chapter Detection** - Split into chapters
3. **AI Content Moderation** - Check for inappropriate content
4. **AI Genre Detection** - Auto-detect genre (if not provided)
5. **AI Summary Generation** - Create description (if empty)
6. Save to database

## Alternative AI Providers

### Using Anthropic Claude

```java
// In AiService.java, modify callOpenAI method:
private String callClaude(String prompt, int maxTokens) {
    // Anthropic API implementation
    String url = "https://api.anthropic.com/v1/messages";
    // ... implementation
}
```

### Using Local AI (Ollama)

```yaml
openai:
  api:
    url: http://localhost:11434/api/generate
```

```java
// Modify for Ollama format
requestBody.put("model", "llama2");
requestBody.put("prompt", prompt);
```

## Cost Optimization

### Token Usage Estimates

| Feature | Avg Tokens | Cost (GPT-3.5) |
|---------|-----------|----------------|
| Chapter Detection | 2,500 | $0.005 |
| Genre Detection | 500 | $0.001 |
| Summary Generation | 1,500 | $0.003 |
| Content Moderation | 1,000 | $0.002 |

**Per Novel Upload:** ~$0.011

### Optimization Tips

1. **Cache Results**
```java
@Cacheable("genre-detection")
public String detectGenre(String title, String description, String sampleText)
```

2. **Batch Processing**
```java
// Process multiple chapters in one API call
```

3. **Use Fallback**
```yaml
ai:
  enabled: false  # Use regex for development
```

## Testing AI Features

### Test Chapter Detection
```bash
curl -X POST http://localhost:8080/api/ai/detect-chapters \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"text":"Chapter 1\nContent here\n\nChapter 2\nMore content"}'
```

### Test Genre Detection
```bash
curl -X POST http://localhost:8080/api/ai/detect-genre \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Space Odyssey","description":"A journey through stars","sampleText":"The spaceship launched..."}'
```

## Fallback Behavior

If AI is disabled or fails:

1. **Chapter Detection** → Regex pattern matching
2. **Genre Detection** → Returns "Fiction"
3. **Summary Generation** → Generic message
4. **Content Moderation** → Allows all content

## Error Handling

```java
try {
    return callOpenAI(prompt, maxTokens);
} catch (Exception e) {
    logger.error("AI service failed", e);
    return fallbackMethod();
}
```

## Monitoring

Track AI usage:
```java
@Aspect
public class AiMonitoringAspect {
    @Around("execution(* AiService.*(..))")
    public Object monitor(ProceedingJoinPoint joinPoint) {
        // Log AI calls, track costs
    }
}
```

## Security

1. **Never expose API keys** in code
2. **Use environment variables**
3. **Rate limit AI endpoints**
4. **Validate input length** (max 10,000 chars)

## Future Enhancements

- [ ] Support for multiple AI providers
- [ ] Caching layer for repeated queries
- [ ] Batch processing for multiple novels
- [ ] Custom fine-tuned models
- [ ] AI-powered recommendations
- [ ] Sentiment analysis
- [ ] Character extraction
- [ ] Plot summary generation
