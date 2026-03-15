# How to Upload Novels 📚

There are multiple ways to upload novels to your platform:

## Method 1: Using the Upload Script (Easiest) ⭐

We've created a simple script for you:

```bash
./upload-novel.sh /path/to/your-novel.pdf
```

**Example:**
```bash
./upload-novel.sh ~/Documents/my-fantasy-novel.pdf
```

The script will:
1. Check if backend is running
2. Login as admin
3. Ask for novel details (title, author, description)
4. Upload the PDF
5. Show you the results

---

## Method 2: Using cURL (Command Line)

### Step 1: Login and get token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@novelplatform.com","password":"admin123"}' \
  | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "Token: $TOKEN"
```

### Step 2: Upload the novel
```bash
curl -X POST http://localhost:8080/api/novels/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/your-novel.pdf" \
  -F "title=My Amazing Novel" \
  -F "author=John Doe" \
  -F "description=An epic tale of adventure and magic"
```

---

## Method 3: Using Postman or Insomnia

### Step 1: Login
- **Method**: POST
- **URL**: `http://localhost:8080/api/auth/login`
- **Headers**: `Content-Type: application/json`
- **Body** (JSON):
```json
{
  "email": "admin@novelplatform.com",
  "password": "admin123"
}
```
- **Copy the token** from the response

### Step 2: Upload Novel
- **Method**: POST
- **URL**: `http://localhost:8080/api/novels/upload`
- **Headers**: 
  - `Authorization: Bearer YOUR_TOKEN_HERE`
- **Body** (form-data):
  - `file`: (select your PDF file)
  - `title`: "Your Novel Title"
  - `author`: "Author Name"
  - `description`: "Novel description"

---

## Method 4: Using the Android App

1. Open the Android app in Android Studio
2. Run the app on an emulator or device
3. Login with admin credentials
4. Navigate to "Upload Novel" section
5. Select PDF file and fill in details
6. Click "Upload"

---

## What Happens When You Upload?

When you upload a PDF novel, the system automatically:

1. **📄 Extracts Text** - Reads all text from the PDF using Apache PDFBox
2. **📖 Detects Chapters** - Uses AI to identify chapter boundaries
3. **🏷️ Detects Genre** - Analyzes content to determine genre (Fantasy, Romance, etc.)
4. **📝 Generates Summary** - Creates an engaging summary
5. **🛡️ Moderates Content** - Checks for inappropriate content
6. **💾 Saves to Database** - Stores novel and chapters
7. **☁️ Uploads to S3** (if enabled) - Stores PDF in cloud storage

---

## Upload Requirements

- **File Format**: PDF only
- **Max File Size**: 50MB
- **Recommended**: 
  - Clear, readable text (not scanned images)
  - Properly formatted chapters
  - Complete novel (not excerpts)

---

## API Endpoints Reference

### Upload Novel
```
POST /api/novels/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

Parameters:
- file: PDF file (required)
- title: Novel title (required)
- author: Author name (required)
- description: Novel description (optional)
```

### Get All Novels
```
GET /api/novels
```

### Get Novel by ID
```
GET /api/novels/{id}
```

### Get Novel Chapters
```
GET /api/novels/{id}/chapters
```

### Update Novel
```
PUT /api/novels/{id}
Authorization: Bearer {token}
Content-Type: application/json

Body:
{
  "title": "Updated Title",
  "author": "Updated Author",
  "description": "Updated description"
}
```

### Delete Novel
```
DELETE /api/novels/{id}
Authorization: Bearer {token}
```

---

## Testing with Sample Data

If you don't have a PDF novel, you can:

1. **Create a test PDF**: Use any word processor to create a simple story and export as PDF
2. **Download free novels**: Visit Project Gutenberg (gutenberg.org) for public domain books
3. **Use sample text**: Create a small PDF with a few chapters for testing

---

## Troubleshooting

### Upload fails with "File too large"
- Your PDF exceeds 50MB
- Solution: Split the novel into volumes or compress the PDF

### Upload fails with "Bad credentials"
- Your token expired or is invalid
- Solution: Login again to get a new token

### Chapters not detected properly
- PDF might have unusual formatting
- Solution: Ensure chapters are clearly marked (e.g., "Chapter 1", "Chapter One")

### Genre detection incorrect
- AI needs more context
- Solution: Provide a detailed description when uploading

### Backend not responding
- Backend might not be running
- Solution: Run `./START_WITHOUT_DOCKER.sh`

---

## Example: Complete Upload Flow

```bash
# 1. Make sure backend is running
./START_WITHOUT_DOCKER.sh

# 2. Upload a novel (in a new terminal)
./upload-novel.sh ~/Documents/fantasy-novel.pdf

# 3. Enter details when prompted:
#    Title: The Dragon's Quest
#    Author: Jane Smith
#    Description: An epic fantasy adventure

# 4. Wait for processing (may take 30-60 seconds for AI)

# 5. Check the result
curl http://localhost:8080/api/novels

# 6. View specific novel
curl http://localhost:8080/api/novels/1
```

---

## Next Steps After Upload

1. **View in Android App**: Open the app and browse your uploaded novels
2. **Read Chapters**: Navigate through auto-detected chapters
3. **Check AI Results**: See the generated genre and summary
4. **Monitor Costs**: Check OpenAI usage at https://platform.openai.com/usage
5. **Upload More**: Add more novels to build your library

---

## Admin Credentials

```
Email: admin@novelplatform.com
Password: admin123
```

**⚠️ Important**: Change these credentials in production!

---

## Need Help?

- Check backend logs for errors
- Run `./TEST_API.sh` to verify system health
- Ensure you have enough OpenAI credits
- Check PDF file is valid and readable

Happy reading! 📖✨
