# Novel Reading Platform

Mobile novel reading application with Spring Boot backend and Android Kotlin frontend.

## 🚀 Quick Start (5 minutes)

### 1. Get OpenAI API Key
```bash
# Sign up at https://platform.openai.com/signup (get $5 free credit)
# Get key from https://platform.openai.com/api-keys

export OPENAI_API_KEY="sk-your-key-here"
```

### 2. Run the Application
```bash
# Start everything with one command
./START.sh
```

That's it! Backend will start on http://localhost:8080

### 3. Open Android App
- Open `android/` folder in Android Studio
- Click Run ▶️

**Detailed guide:** See [RUN_APPLICATION.md](RUN_APPLICATION.md)

---

## Project Structure

```
├── backend/          # Spring Boot REST API
├── android/          # Android Kotlin app
├── web/              # React web portal
└── docker-compose.yml
```

## 🌐 Access Options

### Web Portal (Recommended for Admins)
```bash
./START_WEB.sh
```
Open http://localhost:3000 in your browser

### Android App (Best for Reading)
Open `android/` folder in Android Studio and run

### CLI (For Advanced Users)
Use `./upload-novel.sh` or curl commands

## Quick Start

### 1. Get OpenAI API Key (2 minutes)
```bash
# Sign up at https://platform.openai.com/signup
# Get your API key from https://platform.openai.com/api-keys
# You get $5 free credit (covers 416 novels)

export OPENAI_API_KEY="sk-your-key-here"
```

### 2. Start PostgreSQL with Docker
```bash
docker-compose up -d
```

### 3. Run Backend
```bash
cd backend
mvn spring-boot:run
```

You should see:
```
✅ OpenAI API configured successfully
   Provider: OpenAI GPT-3.5-turbo
   Free Credit: $5 (covers ~416 novels)
```

Default admin credentials:
- Email: admin@novelplatform.com
- Password: admin123

### 4. Run Android App
1. Open `android/` in Android Studio
2. Build and run on emulator or device
3. API automatically connects to localhost (10.0.2.2:8080 for emulator)

**Detailed OpenAI setup:** See [QUICKSTART_OPENAI.md](QUICKSTART_OPENAI.md)

## API Endpoints

### Authentication
- POST `/api/auth/signup` - Register new user
- POST `/api/auth/login` - Login

### Novels
- GET `/api/novels` - Get all published novels
- GET `/api/novels/{id}` - Get novel details
- GET `/api/novels/genre/{genreId}` - Get novels by genre
- GET `/api/novels/search?query=` - Search novels

### Chapters
- GET `/api/novels/{novelId}/chapters` - Get all chapters
- GET `/api/chapters/{id}` - Get chapter content

### Admin (requires ADMIN role)
- POST `/api/admin/novels` - Create novel
- POST `/api/admin/upload-pdf` - Upload PDF novel
- PUT `/api/admin/novels/{id}/status` - Update novel status

### User Features
- GET `/api/bookmarks` - Get user bookmarks
- POST `/api/bookmarks` - Add bookmark
- GET `/api/history` - Get reading history
- POST `/api/history` - Update reading progress

## Features

✅ User authentication with JWT (Reader, Author, Admin roles)
✅ Browse novels by genre
✅ Chapter-by-chapter reading with scroll
✅ Bookmarks and reading history
✅ PDF upload with automatic chapter detection
✅ Search functionality
✅ Dark mode support (Android)
✅ Responsive Material Design UI

## Tech Stack

**Backend:**
- Spring Boot 3.2
- PostgreSQL 15
- Spring Security + JWT
- Apache PDFBox (PDF processing)
- Maven

**Android:**
- Kotlin
- Jetpack Compose
- Material3
- Retrofit (API client)
- Coroutines & Flow
- ViewModel & StateFlow

## Database Schema

- `users` - User accounts with roles
- `novels` - Novel metadata
- `chapters` - Chapter content (TEXT field)
- `genres` - Novel categories
- `bookmarks` - User bookmarks
- `reading_history` - Reading progress tracking

## Development

### Backend Configuration
Edit `backend/src/main/resources/application.yml`:
- Database credentials
- JWT secret key
- AWS S3 settings (for production)

### Android Configuration
Edit `android/app/src/main/java/com/novelplatform/app/data/api/RetrofitClient.kt`:
- Change BASE_URL for production deployment

## License

MIT


## AI Features

The platform includes AI-powered features:

### 1. Automatic Chapter Detection
Upload a PDF and AI automatically splits it into chapters with proper titles.

### 2. Genre Detection
AI analyzes novel content to suggest the most appropriate genre.

### 3. Auto Summary Generation
AI creates compelling 2-3 sentence summaries for novels.

### 4. Content Moderation
AI checks uploaded content for inappropriate material.

### Setup AI

1. Get OpenAI API key from https://platform.openai.com/api-keys
2. Set environment variable:
```bash
export OPENAI_API_KEY="sk-your-key-here"
```

3. Enable AI in config:
```yaml
ai:
  enabled: true
```

For detailed AI configuration, see [AI_INTEGRATION.md](AI_INTEGRATION.md)

### Disable AI (Development)

To save costs during development:
```bash
# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Or set in application.yml:
```yaml
ai:
  enabled: false
```

When disabled, the system uses regex-based fallbacks.


## OpenAI Cost for 200 Novels

**Total Cost: $2.40** (or FREE with $5 credit)

Breakdown per novel:
- Chapter Detection: $0.0058
- Genre Detection: $0.0008
- Summary Generation: $0.0032
- Content Moderation: $0.0022
- **Total:** $0.012 per novel

With OpenAI's $5 free credit, you can process **416 novels for FREE**!

For detailed cost analysis, see [OPENAI_COST_CALCULATOR.md](OPENAI_COST_CALCULATOR.md)

### Monitor Costs

Check your AI usage:
```bash
curl http://localhost:8080/api/admin/costs/summary \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

Response:
```json
{
  "totalCalls": 800,
  "inputTokens": 4300000,
  "outputTokens": 160000,
  "totalCost": 2.39
}
```
