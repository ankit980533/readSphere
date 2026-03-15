# Free AI Setup Guide

## Overview

You have 3 FREE AI options for the novel platform:

| Provider | Cost | Setup Time | Quality | Best For |
|----------|------|------------|---------|----------|
| **Ollama** | 100% Free | 5 min | Good | Local development |
| **Hugging Face** | Free tier | 2 min | Good | Production (limited) |
| **OpenAI** | $5 credit | 2 min | Excellent | Production |

## Option 1: Ollama (Recommended - 100% Free)

### What is Ollama?
- Runs AI models locally on your computer
- No API keys needed
- No usage limits
- Works offline
- Supports multiple models (Llama, Mistral, etc.)

### Installation

**macOS/Linux:**
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

**Windows:**
Download from https://ollama.com/download

**Verify Installation:**
```bash
ollama --version
```

### Download AI Model

```bash
# Recommended: Llama 3.2 (2GB)
ollama pull llama3.2

# Alternative: Mistral (4GB, better quality)
ollama pull mistral

# Lightweight: Phi-2 (1.6GB, faster)
ollama pull phi
```

### Start Ollama Server

```bash
ollama serve
```

Server runs on `http://localhost:11434`

### Configure Application

Edit `application.yml`:
```yaml
ai:
  enabled: true
  provider: ollama

ollama:
  api:
    url: http://localhost:11434
  model: llama3.2
```

### Test It

```bash
# Test Ollama directly
curl http://localhost:11434/api/generate -d '{
  "model": "llama3.2",
  "prompt": "What is a novel?",
  "stream": false
}'

# Test via your API
curl -X POST http://localhost:8080/api/ai/detect-genre \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Space Adventure","description":"Journey to Mars","sampleText":"The rocket launched..."}'
```

### Pros & Cons

✅ **Pros:**
- Completely free
- No API keys
- No usage limits
- Works offline
- Privacy (data stays local)

❌ **Cons:**
- Requires local installation
- Uses computer resources (RAM/CPU)
- Slower than cloud APIs
- Need to download models (1-4GB)

---

## Option 2: Hugging Face (Free Tier)

### What is Hugging Face?
- Cloud-based AI platform
- Free tier: 30,000 requests/month
- No credit card required
- Good quality models

### Setup

1. **Create Account:**
   - Go to https://huggingface.co/join
   - Sign up (free)

2. **Get API Key:**
   - Go to https://huggingface.co/settings/tokens
   - Click "New token"
   - Name: "novel-platform"
   - Role: "read"
   - Copy the token

3. **Configure Application:**

```bash
export HUGGINGFACE_API_KEY="hf_your_token_here"
```

Edit `application.yml`:
```yaml
ai:
  enabled: true
  provider: huggingface

huggingface:
  api:
    key: ${HUGGINGFACE_API_KEY}
```

### Test It

```bash
curl -X POST http://localhost:8080/api/ai/detect-genre \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Mystery Novel","description":"A detective story","sampleText":"The detective arrived..."}'
```

### Free Tier Limits

- 30,000 requests/month
- ~1,000 requests/day
- Rate limit: 100 requests/hour

### Pros & Cons

✅ **Pros:**
- No installation needed
- Cloud-based (fast)
- Free tier generous
- Good quality

❌ **Cons:**
- Requires internet
- Monthly limits
- Need API key
- Rate limits

---

## Option 3: OpenAI (Free $5 Credit)

### Setup

1. **Create Account:**
   - Go to https://platform.openai.com/signup
   - New accounts get $5 free credit

2. **Get API Key:**
   - Go to https://platform.openai.com/api-keys
   - Create new key

3. **Configure:**

```bash
export OPENAI_API_KEY="sk-your-key-here"
```

```yaml
ai:
  enabled: true
  provider: openai

openai:
  api:
    key: ${OPENAI_API_KEY}
```

### Cost Estimate

With $5 credit:
- ~450 novel uploads
- ~10,000 chapter detections
- ~20,000 genre detections

### Pros & Cons

✅ **Pros:**
- Best quality
- Fast
- Reliable

❌ **Cons:**
- Costs money after $5
- Requires credit card
- Usage limits

---

## Comparison

### For Development (Local Testing)
**Use Ollama** - Free, unlimited, works offline

### For Production (Small Scale)
**Use Hugging Face** - Free tier covers ~1,000 novels/month

### For Production (High Quality)
**Use OpenAI** - Best results, pay as you go

---

## Quick Start Commands

### Ollama Setup (5 minutes)
```bash
# Install
curl -fsSL https://ollama.com/install.sh | sh

# Download model
ollama pull llama3.2

# Start server
ollama serve

# Configure app
# Set ai.provider=ollama in application.yml

# Run backend
mvn spring-boot:run
```

### Hugging Face Setup (2 minutes)
```bash
# Get API key from https://huggingface.co/settings/tokens

# Set environment variable
export HUGGINGFACE_API_KEY="hf_your_token"

# Configure app
# Set ai.provider=huggingface in application.yml

# Run backend
mvn spring-boot:run
```

---

## Switching Between Providers

Just change the `ai.provider` in `application.yml`:

```yaml
ai:
  provider: ollama      # Use Ollama (free, local)
  # provider: huggingface  # Use Hugging Face (free tier)
  # provider: openai       # Use OpenAI (paid)
```

No code changes needed!

---

## Troubleshooting

### Ollama: "Connection refused"
```bash
# Make sure Ollama is running
ollama serve

# Check if it's running
curl http://localhost:11434/api/tags
```

### Ollama: "Model not found"
```bash
# Download the model
ollama pull llama3.2

# List installed models
ollama list
```

### Hugging Face: "401 Unauthorized"
```bash
# Check API key is set
echo $HUGGINGFACE_API_KEY

# Verify key at https://huggingface.co/settings/tokens
```

### Hugging Face: "Model is loading"
```bash
# First request may take 20-30 seconds
# Model needs to "wake up"
# Retry after 30 seconds
```

---

## Performance Comparison

| Task | Ollama (Local) | Hugging Face | OpenAI |
|------|----------------|--------------|--------|
| Chapter Detection | 3-5s | 2-3s | 1-2s |
| Genre Detection | 1-2s | 1-2s | 0.5-1s |
| Summary Generation | 2-4s | 2-3s | 1-2s |
| Content Moderation | 1-2s | 1-2s | 0.5-1s |

---

## Recommended Setup

**Development:**
```yaml
ai:
  provider: ollama  # Free, unlimited
```

**Production (Low Budget):**
```yaml
ai:
  provider: huggingface  # Free tier
```

**Production (High Quality):**
```yaml
ai:
  provider: openai  # Best results
```

---

## Cost Breakdown

### Ollama
- Setup: Free
- Usage: Free
- Monthly: $0

### Hugging Face
- Setup: Free
- Usage: Free (30k requests/month)
- Monthly: $0 (within limits)

### OpenAI
- Setup: $5 free credit
- Usage: ~$0.01 per novel
- Monthly: ~$10-50 (depending on volume)

---

## My Recommendation

**Start with Ollama:**
1. 100% free
2. No limits
3. Works offline
4. Easy setup

**Upgrade to Hugging Face if:**
- You need cloud-based solution
- Ollama is too slow
- You want to deploy to server without GPU

**Upgrade to OpenAI if:**
- You need best quality
- You have budget
- Speed is critical
