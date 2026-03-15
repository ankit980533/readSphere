# OpenAI Cost Calculator for Novel Platform

## Cost Breakdown for 200 Novels

### OpenAI GPT-3.5-turbo Pricing (Current)
- **Input:** $0.50 per 1M tokens (~750,000 words)
- **Output:** $1.50 per 1M tokens (~750,000 words)

### Per Novel Processing

Assuming average novel = 80,000 words (~107,000 tokens)

| AI Feature | Input Tokens | Output Tokens | Cost per Novel |
|------------|--------------|---------------|----------------|
| **Chapter Detection** | 10,000 | 500 | $0.0058 |
| **Genre Detection** | 1,500 | 50 | $0.0008 |
| **Summary Generation** | 6,000 | 150 | $0.0032 |
| **Content Moderation** | 4,000 | 100 | $0.0022 |
| **TOTAL per Novel** | 21,500 | 800 | **$0.0120** |

### Cost for 200 Novels

```
200 novels × $0.012 = $2.40
```

## Detailed Breakdown

### 1. Chapter Detection
- **Input:** ~8,000 words of novel text = 10,000 tokens
- **Output:** JSON with chapter positions = 500 tokens
- **Cost:** (10,000 × $0.50 / 1M) + (500 × $1.50 / 1M) = $0.0058

### 2. Genre Detection
- **Input:** Title + description + 1,000 word sample = 1,500 tokens
- **Output:** Genre name = 50 tokens
- **Cost:** (1,500 × $0.50 / 1M) + (50 × $1.50 / 1M) = $0.0008

### 3. Summary Generation
- **Input:** First 5,000 words = 6,000 tokens
- **Output:** 2-3 sentence summary = 150 tokens
- **Cost:** (6,000 × $0.50 / 1M) + (150 × $1.50 / 1M) = $0.0032

### 4. Content Moderation
- **Input:** 3,000 word sample = 4,000 tokens
- **Output:** Moderation result JSON = 100 tokens
- **Cost:** (4,000 × $0.50 / 1M) + (100 × $1.50 / 1M) = $0.0022

## Cost Scenarios

### Scenario 1: All Features Enabled (Recommended)
```
200 novels × $0.012 = $2.40
```

### Scenario 2: Chapter Detection Only
```
200 novels × $0.0058 = $1.16
```

### Scenario 3: Without Content Moderation
```
200 novels × $0.0098 = $1.96
```

### Scenario 4: 1,000 Novels (Scale)
```
1,000 novels × $0.012 = $12.00
```

## Monthly Cost Estimates

| Novels/Month | Cost/Month | Cost/Year |
|--------------|------------|-----------|
| 50 | $0.60 | $7.20 |
| 100 | $1.20 | $14.40 |
| 200 | $2.40 | $28.80 |
| 500 | $6.00 | $72.00 |
| 1,000 | $12.00 | $144.00 |
| 5,000 | $60.00 | $720.00 |

## OpenAI Free Credit

New OpenAI accounts get **$5 free credit** which covers:
- **416 novels** with all AI features
- **862 novels** with chapter detection only
- Valid for 3 months

## Cost Optimization Tips

### 1. Disable Features You Don't Need
```yaml
# In application.yml
ai:
  features:
    chapter-detection: true    # Keep
    genre-detection: false     # Disable if you manually set genres
    summary-generation: false  # Disable if you write descriptions
    content-moderation: true   # Keep for safety
```

**Savings:** ~40% ($0.0072 per novel)

### 2. Use Smaller Text Samples
```java
// Reduce sample size for genre detection
String sample = text.substring(0, Math.min(500, text.length())); // Was 1000
```

**Savings:** ~10% ($0.0108 per novel)

### 3. Cache Results
```java
@Cacheable("genre-cache")
public String detectGenre(String title, String description, String sample)
```

**Savings:** 100% for duplicate requests

### 4. Batch Processing
Process multiple novels in one API call when possible.

**Savings:** ~20% ($0.0096 per novel)

### 5. Use GPT-3.5-turbo-instruct (Cheaper)
```yaml
openai:
  model: gpt-3.5-turbo-instruct  # 50% cheaper
```

**Savings:** 50% ($0.0060 per novel)

## Comparison with Alternatives

| Provider | 200 Novels | Quality | Speed |
|----------|-----------|---------|-------|
| **OpenAI GPT-3.5** | $2.40 | Excellent | Fast |
| **OpenAI GPT-4** | $24.00 | Best | Fast |
| **Anthropic Claude** | $3.60 | Excellent | Fast |
| **Ollama (Local)** | $0.00 | Good | Slower |
| **Hugging Face** | $0.00* | Good | Medium |

*Free tier: 30,000 requests/month

## Real-World Example

### Startup Phase (First 6 months)
- Upload 200 novels
- **Cost:** $2.40
- **With $5 free credit:** $0 (still have $2.60 left)

### Growth Phase (Year 1)
- Upload 100 novels/month
- **Cost:** $1.20/month = $14.40/year

### Scale Phase (Year 2+)
- Upload 500 novels/month
- **Cost:** $6.00/month = $72.00/year

## Budget Recommendation

### Conservative Budget
```
Monthly: $10
Covers: ~833 novels/month
```

### Moderate Budget
```
Monthly: $25
Covers: ~2,083 novels/month
```

### Aggressive Budget
```
Monthly: $50
Covers: ~4,166 novels/month
```

## Cost vs. Value

### Manual Alternative Costs
- Hire someone to split chapters: $5/novel
- Hire someone to write summaries: $10/novel
- Manual moderation: $3/novel
- **Total manual cost:** $18/novel

### AI Cost
- **$0.012/novel**
- **Savings:** 99.93% vs manual
- **ROI:** 1,500x

## Setup for OpenAI

### 1. Get API Key
```bash
# Sign up at https://platform.openai.com/signup
# Get $5 free credit (no credit card required initially)
# Go to https://platform.openai.com/api-keys
# Create new key
```

### 2. Configure Application
```bash
export OPENAI_API_KEY="sk-your-key-here"
```

```yaml
# application.yml
ai:
  enabled: true
  provider: openai

openai:
  api:
    key: ${OPENAI_API_KEY}
    model: gpt-3.5-turbo  # Most cost-effective
```

### 3. Monitor Usage
```bash
# Check usage at https://platform.openai.com/usage
```

## Cost Alerts

Set up billing alerts in OpenAI dashboard:
- Alert at $5 (free credit exhausted)
- Alert at $10
- Hard limit at $20 (safety)

## Final Answer

### For 200 Novels:
```
Total Cost: $2.40
With $5 free credit: $0 (FREE)
Remaining credit: $2.60 (enough for 216 more novels)
```

### Recommendation:
✅ **Use OpenAI with free credit**
- Process your 200 novels for FREE
- Excellent quality
- Fast processing
- Still have credit left over

### When to Switch to Free Alternatives:
- After processing 416 novels (free credit exhausted)
- If processing >1,000 novels/month ($12+/month)
- If budget is extremely tight

## Cost Tracking Code

Add this to monitor costs:

```java
@Aspect
@Component
public class AiCostTracker {
    
    private AtomicInteger totalTokens = new AtomicInteger(0);
    
    @Around("execution(* AiService.call*(..))")
    public Object trackCost(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        
        // Estimate tokens (rough)
        int estimatedTokens = 2000; // Average per call
        totalTokens.addAndGet(estimatedTokens);
        
        double cost = (totalTokens.get() / 1_000_000.0) * 0.50;
        log.info("Total AI cost so far: ${}", String.format("%.4f", cost));
        
        return result;
    }
}
```

## Summary

**200 novels = $2.40 (or FREE with $5 credit)**

This is incredibly cheap compared to:
- Manual processing: $3,600
- Hiring developers: $5,000+
- Building your own AI: $50,000+

**Verdict:** OpenAI is extremely cost-effective for your use case! 🎉
