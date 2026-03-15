# AWS S3 Integration Guide

## Why Use AWS S3?

Currently, your application:
- ✅ Extracts text from PDF
- ✅ Splits into chapters
- ✅ Saves chapters to database
- ❌ **Discards the original PDF file**
- ❌ **No storage for cover images**

### Problems with Current Approach:
1. **Lost PDFs**: Original files are deleted after processing
2. **No Downloads**: Users can't download original PDFs
3. **No Re-processing**: Can't re-run AI if it improves
4. **No Backups**: If database fails, everything is lost
5. **Cover Images**: No reliable storage for book covers

### Solution: AWS S3
Store files in S3, metadata in database:
- ✅ Keep original PDFs forever
- ✅ Store cover images reliably
- ✅ Allow PDF downloads
- ✅ Re-process novels anytime
- ✅ Backup and disaster recovery

---

## What to Store in S3

### 1. Original PDF Files
```
s3://your-bucket/pdfs/uuid-novel-title.pdf
```
**Why:** Backup, re-processing, user downloads

### 2. Cover Images
```
s3://your-bucket/covers/uuid-novel-title.jpg
```
**Why:** Display on app, reliable image hosting

### 3. User Profile Pictures (Future)
```
s3://your-bucket/avatars/uuid-username.jpg
```

---

## Current vs. Recommended Flow

### Current Flow (No S3)
```
1. Admin uploads PDF
2. Extract text from PDF
3. AI splits into chapters
4. Save chapters to database
5. ❌ PDF is discarded
6. ❌ No cover image storage
```

### Recommended Flow (With S3)
```
1. Admin uploads PDF
2. ✅ Save PDF to S3 → Get URL
3. Extract text from PDF
4. AI splits into chapters
5. Save to database:
   - Novel metadata
   - PDF URL (from S3)
   - Cover image URL (from S3)
   - Chapters
6. ✅ Original PDF preserved in S3
```

---

## Implementation

### Step 1: Update Database Model

Add PDF URL field to Novel entity:

```java
// backend/src/main/java/com/novelplatform/model/Novel.java

@Entity
public class Novel {
    // ... existing fields
    
    private String pdfUrl;        // ← Add this
    private String coverImageUrl; // ← Already exists, but now from S3
    
    // Getters and setters
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
}
```

### Step 2: Configure AWS S3

```yaml
# backend/src/main/resources/application.yml

aws:
  s3:
    enabled: true  # Set to false to disable
    bucket: ${AWS_S3_BUCKET:novel-platform-storage}
    region: ${AWS_REGION:ap-south-1}
  credentials:
    access-key: ${AWS_ACCESS_KEY_ID:}
    secret-key: ${AWS_SECRET_ACCESS_KEY:}
```

### Step 3: Set Environment Variables

```bash
export AWS_S3_ENABLED=true
export AWS_S3_BUCKET=your-bucket-name
export AWS_REGION=ap-south-1
export AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
export AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCY
```

### Step 4: Update PDF Processing

```java
// backend/src/main/java/com/novelplatform/service/PdfProcessingService.java

@Autowired(required = false)
private S3Service s3Service;

public Novel processPdfUpload(MultipartFile file, String title, ...) {
    
    // 1. Upload PDF to S3 (if enabled)
    String pdfUrl = null;
    if (s3Service != null && s3Service.isEnabled()) {
        pdfUrl = s3Service.uploadPdf(file, title);
    }
    
    // 2. Extract text
    String text = extractTextFromPdf(file);
    
    // 3. AI processing
    List<ChapterDetectionResult> chapters = detectChaptersWithAI(text);
    
    // 4. Save novel with PDF URL
    Novel novel = new Novel();
    novel.setTitle(title);
    novel.setPdfUrl(pdfUrl);  // ← Save S3 URL
    // ... rest of the code
    
    return novelRepository.save(novel);
}
```

---

## How to Set Up AWS S3

### Step 1: Create AWS Account
1. Go to https://aws.amazon.com
2. Click "Create an AWS Account"
3. Follow signup process (requires credit card)
4. **Free Tier:** 5GB storage, 20,000 GET requests, 2,000 PUT requests/month

### Step 2: Create S3 Bucket

```bash
# Using AWS CLI
aws s3 mb s3://novel-platform-storage --region ap-south-1

# Or via AWS Console:
# 1. Go to https://s3.console.aws.amazon.com
# 2. Click "Create bucket"
# 3. Bucket name: novel-platform-storage
# 4. Region: Asia Pacific (Mumbai) - ap-south-1
# 5. Block all public access: OFF (for public URLs)
# 6. Click "Create bucket"
```

### Step 3: Create IAM User

```bash
# 1. Go to https://console.aws.amazon.com/iam
# 2. Users → Add user
# 3. User name: novel-platform-app
# 4. Access type: Programmatic access
# 5. Permissions: Attach existing policies
#    - AmazonS3FullAccess
# 6. Create user
# 7. Copy Access Key ID and Secret Access Key
```

### Step 4: Set Bucket Policy (Public Read)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::novel-platform-storage/*"
    }
  ]
}
```

Apply this policy:
1. Go to S3 bucket
2. Permissions tab
3. Bucket Policy
4. Paste JSON above
5. Save

---

## Cost Estimation

### AWS S3 Pricing (ap-south-1 region)

**Storage:**
- First 50 TB: $0.023 per GB/month
- Example: 100 PDFs × 5MB = 500MB = $0.01/month

**Requests:**
- PUT (upload): $0.005 per 1,000 requests
- GET (download): $0.0004 per 1,000 requests

**Data Transfer:**
- First 10 TB/month: $0.109 per GB
- Example: 1,000 downloads × 5MB = 5GB = $0.55/month

### Cost Examples

| Scenario | Storage | Requests | Transfer | Total/Month |
|----------|---------|----------|----------|-------------|
| 100 novels | 500MB | 100 uploads | 100 downloads | $0.02 |
| 1,000 novels | 5GB | 1,000 uploads | 1,000 downloads | $0.20 |
| 10,000 novels | 50GB | 10,000 uploads | 10,000 downloads | $2.00 |

**Free Tier (First 12 months):**
- 5GB storage
- 20,000 GET requests
- 2,000 PUT requests
- 15GB data transfer out

---

## Testing S3 Integration

### Test 1: Check if S3 is Enabled

```bash
curl http://localhost:8080/api/admin/s3/status \
  -H "Authorization: Bearer $TOKEN"

# Response:
{
  "enabled": true,
  "bucket": "novel-platform-storage",
  "region": "ap-south-1"
}
```

### Test 2: Upload PDF with S3

```bash
curl -X POST http://localhost:8080/api/admin/upload-pdf \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@novel.pdf" \
  -F "title=Test Novel" \
  -F "author=Test Author" \
  -F "genreId=1"

# Response includes pdfUrl:
{
  "id": 1,
  "title": "Test Novel",
  "pdfUrl": "https://novel-platform-storage.s3.ap-south-1.amazonaws.com/pdfs/uuid-test-novel.pdf"
}
```

### Test 3: Download PDF

```bash
# Get novel details
curl http://localhost:8080/api/novels/1

# Response:
{
  "id": 1,
  "title": "Test Novel",
  "pdfUrl": "https://..."
}

# Download PDF directly from S3
curl -O "https://novel-platform-storage.s3.ap-south-1.amazonaws.com/pdfs/uuid-test-novel.pdf"
```

---

## Without AWS S3 (Current Behavior)

If you don't set up AWS S3:
- ✅ Application still works
- ✅ Chapters saved to database
- ❌ Original PDFs not saved
- ❌ Cover images not stored
- ⚠️  `pdfUrl` will be `null` in database

---

## Migration Strategy

### Phase 1: Enable S3 for New Uploads
1. Set up AWS S3
2. Configure credentials
3. New PDFs go to S3
4. Old novels remain without PDFs

### Phase 2: Re-upload Old Novels (Optional)
1. Admin re-uploads old PDFs
2. System updates existing novels
3. All novels now have S3 URLs

---

## Security Best Practices

### 1. Use IAM Roles (Production)
Instead of access keys, use IAM roles:
```java
S3Client.builder()
    .region(Region.of(region))
    .build();  // Uses IAM role automatically
```

### 2. Bucket Encryption
Enable server-side encryption:
```bash
aws s3api put-bucket-encryption \
  --bucket novel-platform-storage \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }]
  }'
```

### 3. Versioning (Backup)
Enable versioning for backups:
```bash
aws s3api put-bucket-versioning \
  --bucket novel-platform-storage \
  --versioning-configuration Status=Enabled
```

---

## Troubleshooting

### Error: "Access Denied"
**Solution:** Check bucket policy allows public read

### Error: "Bucket does not exist"
**Solution:** Create bucket in correct region

### Error: "Invalid credentials"
**Solution:** Verify AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY

### Files not uploading
**Solution:** Check `aws.s3.enabled=true` in config

---

## Summary

**Without S3:**
- PDFs processed and discarded
- No file storage
- Works fine for testing

**With S3:**
- PDFs stored permanently
- Cover images hosted reliably
- Users can download PDFs
- Better for production

**Cost:** ~$0.02/month for 100 novels

**Setup Time:** 15 minutes

**Recommendation:** Set up S3 before going to production!
