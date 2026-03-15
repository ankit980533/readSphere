# Where and Why to Use AWS S3

## 🎯 Simple Answer

**AWS S3 is used to store FILES, not text data.**

### What Goes in Database (PostgreSQL):
- ✅ Novel metadata (title, author, description)
- ✅ Chapter text content
- ✅ User accounts
- ✅ Bookmarks
- ✅ Reading history
- ✅ **URLs pointing to S3 files**

### What Goes in AWS S3:
- ✅ Original PDF files
- ✅ Cover images (JPG/PNG)
- ✅ User profile pictures
- ✅ Any other files

---

## 📊 Current Flow (Without S3)

```
Admin uploads PDF (5MB file)
         ↓
Extract text from PDF
         ↓
AI splits into chapters
         ↓
Save chapters to PostgreSQL database
         ↓
❌ Original PDF is DELETED (lost forever)
```

**Problems:**
1. Can't download original PDF
2. Can't re-process if AI improves
3. No backup of original file
4. Cover images have no storage

---

## 📊 Recommended Flow (With S3)

```
Admin uploads PDF (5MB file)
         ↓
Save PDF to S3 → Get URL: https://s3.../pdfs/novel.pdf
         ↓
Extract text from PDF
         ↓
AI splits into chapters
         ↓
Save to PostgreSQL:
  - Novel: title, author, pdfUrl (S3 link)
  - Chapters: chapter text
         ↓
✅ Original PDF preserved in S3
✅ Users can download from S3 URL
```

---

## 💾 Storage Comparison

### Example: 100 Novels

**Without S3:**
```
PostgreSQL Database:
- Novel metadata: 10KB × 100 = 1MB
- Chapters: 100KB × 100 = 10MB
- Total: 11MB

Original PDFs: ❌ LOST
```

**With S3:**
```
PostgreSQL Database:
- Novel metadata: 10KB × 100 = 1MB
- Chapters: 100KB × 100 = 10MB
- S3 URLs: 1KB × 100 = 100KB
- Total: 11.1MB

AWS S3:
- Original PDFs: 5MB × 100 = 500MB
- Cover images: 200KB × 100 = 20MB
- Total: 520MB

Cost: $0.01/month
```

---

## 🔄 Data Flow Diagram

### Novel Upload Process

```
┌─────────────────────────────────────────────────────────┐
│ 1. Admin uploads PDF via API                            │
│    POST /api/admin/upload-pdf                           │
│    File: novel.pdf (5MB)                                │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ 2. S3Service.uploadPdf()                                │
│    → Uploads to S3                                      │
│    → Returns URL: https://s3.../pdfs/uuid-novel.pdf    │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ 3. PdfProcessingService.processPdfUpload()             │
│    → Extract text from PDF                              │
│    → AI detects chapters                                │
│    → AI moderates content                               │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ 4. Save to PostgreSQL Database                          │
│                                                          │
│    novels table:                                        │
│    ┌──────────────────────────────────────────┐        │
│    │ id: 1                                     │        │
│    │ title: "My Novel"                         │        │
│    │ author: "John Doe"                        │        │
│    │ pdfUrl: "https://s3.../pdfs/uuid.pdf"   │ ← S3   │
│    │ coverImage: "https://s3.../covers/x.jpg" │ ← S3   │
│    └──────────────────────────────────────────┘        │
│                                                          │
│    chapters table:                                      │
│    ┌──────────────────────────────────────────┐        │
│    │ id: 1, novel_id: 1, number: 1            │        │
│    │ title: "Chapter 1"                        │        │
│    │ content: "Once upon a time..." (TEXT)     │        │
│    └──────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────┘
```

---

## 📱 User Download Flow

```
User clicks "Download PDF" in app
         ↓
App requests novel details: GET /api/novels/1
         ↓
Backend returns:
{
  "id": 1,
  "title": "My Novel",
  "pdfUrl": "https://novel-platform.s3.ap-south-1.amazonaws.com/pdfs/uuid-novel.pdf"
}
         ↓
App opens S3 URL directly
         ↓
User downloads PDF from S3 (not from your server!)
```

**Benefits:**
- ✅ Your server doesn't handle large file downloads
- ✅ S3 handles bandwidth and CDN
- ✅ Faster downloads for users
- ✅ Lower server costs

---

## 💰 Cost Breakdown

### Scenario: 1,000 Novels

**Storage:**
- 1,000 PDFs × 5MB = 5GB
- 1,000 covers × 200KB = 200MB
- Total: 5.2GB
- Cost: 5.2GB × $0.023 = **$0.12/month**

**Uploads:**
- 1,000 PDF uploads
- 1,000 cover uploads
- Cost: 2,000 × $0.005/1000 = **$0.01/month**

**Downloads:**
- 10,000 PDF downloads/month
- 10,000 × 5MB = 50GB transfer
- Cost: 50GB × $0.109 = **$5.45/month**

**Total: $5.58/month for 1,000 novels**

---

## 🚀 When to Use S3

### Use S3 When:
- ✅ Storing files (PDFs, images, videos)
- ✅ Need public URLs for files
- ✅ Want to allow downloads
- ✅ Need file backups
- ✅ Files are large (>1MB)

### Don't Use S3 When:
- ❌ Storing text data (use database)
- ❌ Storing user credentials (use database)
- ❌ Storing small metadata (use database)
- ❌ Need to query/search content (use database)

---

## 🔧 Implementation Status

### Currently Implemented:
- ✅ S3Service.java created
- ✅ Configuration in application.yml
- ✅ Upload methods ready

### Not Yet Integrated:
- ⏳ PdfProcessingService doesn't use S3Service
- ⏳ Novel model doesn't have pdfUrl field
- ⏳ AdminController doesn't save S3 URLs

### To Enable S3:
1. Add `pdfUrl` field to Novel model
2. Inject S3Service in PdfProcessingService
3. Call `s3Service.uploadPdf()` before processing
4. Save returned URL to database

---

## 📝 Quick Setup Checklist

### For Development (Skip S3):
- [ ] Just use database
- [ ] PDFs processed and discarded
- [ ] Works fine for testing

### For Production (Use S3):
- [ ] Create AWS account
- [ ] Create S3 bucket
- [ ] Create IAM user with S3 access
- [ ] Set environment variables:
  ```bash
  export AWS_S3_ENABLED=true
  export AWS_S3_BUCKET=your-bucket-name
  export AWS_ACCESS_KEY_ID=AKIA...
  export AWS_SECRET_ACCESS_KEY=...
  ```
- [ ] Update Novel model (add pdfUrl field)
- [ ] Integrate S3Service in PdfProcessingService
- [ ] Test upload and download

---

## 🎓 Summary

**Simple Rule:**
- **Files** → AWS S3
- **Data** → PostgreSQL Database

**Your Application:**
- Chapter text → Database ✅
- Original PDFs → S3 (not implemented yet)
- Cover images → S3 (not implemented yet)

**Cost:** ~$0.01/month for 100 novels

**Required:** No (optional for production)

**Benefit:** Keep original files, allow downloads, better scalability

---

## 📚 Related Documentation

- Full implementation guide: [AWS_S3_INTEGRATION.md](AWS_S3_INTEGRATION.md)
- Configuration guide: [CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md)
- Setup guide: [SETUP.md](SETUP.md)
