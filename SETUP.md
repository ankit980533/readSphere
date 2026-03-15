# Setup Guide

## Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- Android Studio (for mobile app)
- PostgreSQL 15 (or use Docker)
- **OpenAI API Key** (get $5 free credit at https://platform.openai.com/signup)

## Step-by-Step Setup

### 1. Get OpenAI API Key

```bash
# 1. Sign up at https://platform.openai.com/signup
# 2. Go to https://platform.openai.com/api-keys
# 3. Create new key
# 4. Copy the key (starts with sk-)

# Set environment variable
export OPENAI_API_KEY="sk-your-actual-key-here"

# Verify it's set
echo $OPENAI_API_KEY
```

### 2. Database Setup

**Option A: Using Docker (Recommended)**
```bash
docker-compose up -d
```

**Option B: Local PostgreSQL**
```bash
createdb noveldb
psql noveldb < backend/src/main/resources/data.sql
```

### 3. Backend Setup

```bash
cd backend

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

**Verify it's running:**
```bash
curl http://localhost:8080/api/genres
```

### 4. Test OpenAI Integration

```bash
# Run automated test
./backend/test-openai.sh
```

You should see:
```
✅ All tests completed!
   • OpenAI API: Connected
   • Genre Detection: Working
   • Summary Generation: Working
   • Content Moderation: Working
```

### 5. Android App Setup

1. Open Android Studio
2. File → Open → Select `android/` folder
3. Wait for Gradle sync to complete
4. Run on emulator or physical device

**Note:** The app is configured to connect to:
- Emulator: `http://10.0.2.2:8080`
- Physical device: Update BASE_URL in `RetrofitClient.kt` to your computer's IP

### 6. Test the Application

**Backend API Test:**
```bash
# Signup
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Get novels
curl http://localhost:8080/api/novels
```

**Admin Login:**
- Email: `admin@novelplatform.com`
- Password: `admin123`

### 7. Upload a Novel with AI Processing

```bash
# Get admin token first
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@novelplatform.com","password":"admin123"}' \
  | jq -r '.token')

# Upload PDF
curl -X POST http://localhost:8080/api/admin/upload-pdf \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/novel.pdf" \
  -F "title=My Novel" \
  -F "author=Author Name" \
  -F "genreId=1" \
  -F "description=A great story"
```

## Configuration

### Backend Configuration

Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/noveldb
    username: your_username
    password: your_password

jwt:
  secret: your-secret-key-min-256-bits
  expiration: 86400000  # 24 hours

aws:
  s3:
    bucket: your-bucket-name
    region: your-region
```

### Android Configuration

Edit `android/app/src/main/java/com/novelplatform/app/data/api/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "http://YOUR_IP:8080/"
```

## Troubleshooting

### Backend won't start
- Check PostgreSQL is running: `docker ps` or `pg_isready`
- Verify database credentials in `application.yml`
- Check port 8080 is not in use: `lsof -i :8080`

### Android app can't connect
- Emulator: Use `10.0.2.2` instead of `localhost`
- Physical device: Use your computer's IP address
- Check firewall allows connections on port 8080
- Verify backend is running: `curl http://localhost:8080/api/genres`

### PDF upload fails
- Check file size (default max: 10MB)
- Verify PDF is not encrypted
- Check server logs for detailed error

## Next Steps

1. Create sample novels via admin panel
2. Test reading experience on mobile
3. Configure AWS S3 for production file storage
4. Set up proper JWT secret for production
5. Enable HTTPS for production deployment
