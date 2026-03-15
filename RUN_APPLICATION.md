# How to Run Your Novel Platform Application

## Complete Step-by-Step Guide

### Prerequisites Check

Before starting, make sure you have:
- [ ] Java 17 or higher installed
- [ ] Maven installed
- [ ] Docker installed (for database)
- [ ] Android Studio (for mobile app)
- [ ] OpenAI API key (get free $5 credit)

**Check your installations:**
```bash
java -version    # Should show Java 17+
mvn -version     # Should show Maven 3.6+
docker --version # Should show Docker version
```

---

## Part 1: Backend Setup (10 minutes)

### Step 1: Get OpenAI API Key (2 minutes)

1. Go to https://platform.openai.com/signup
2. Create a free account
3. Go to https://platform.openai.com/api-keys
4. Click "Create new secret key"
5. Copy the key (starts with `sk-`)

### Step 2: Set Environment Variable

**On Linux/Mac:**
```bash
export OPENAI_API_KEY="sk-your-actual-key-here"

# Verify it's set
echo $OPENAI_API_KEY
```

**On Windows (Command Prompt):**
```cmd
set OPENAI_API_KEY=sk-your-actual-key-here

# Verify it's set
echo %OPENAI_API_KEY%
```

**On Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="sk-your-actual-key-here"

# Verify it's set
echo $env:OPENAI_API_KEY
```

### Step 3: Start Database

```bash
# Navigate to project root
cd /path/to/your/project

# Start PostgreSQL with Docker
docker-compose up -d

# Verify it's running
docker ps
```

You should see:
```
CONTAINER ID   IMAGE         STATUS
abc123...      postgres:15   Up 2 seconds
```

### Step 4: Start Backend

```bash
# Navigate to backend folder
cd backend

# Run the application
mvn spring-boot:run
```

**What to expect:**
- First time: Maven will download dependencies (2-5 minutes)
- You'll see logs scrolling
- Look for these success messages:

```
✅ OpenAI API configured successfully
   Provider: OpenAI GPT-3.5-turbo
   Free Credit: $5 (covers ~416 novels)

Started NovelApplication in 8.5 seconds
```

**Backend is ready when you see:**
```
Tomcat started on port(s): 8080 (http)
```

### Step 5: Test Backend

Open a new terminal and test:

```bash
# Test 1: Check if backend is running
curl http://localhost:8080/api/genres

# Should return: [{"id":1,"name":"Romance"},...]

# Test 2: Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@novelplatform.com","password":"admin123"}'

# Should return: {"token":"eyJ...","name":"Admin","role":"ADMIN"}
```

✅ **Backend is working!**

---

## Part 2: Android App Setup (5 minutes)

### Step 1: Open Project in Android Studio

1. Open Android Studio
2. Click "Open"
3. Navigate to your project folder
4. Select the `android` folder
5. Click "OK"

### Step 2: Wait for Gradle Sync

- Android Studio will sync Gradle (2-3 minutes)
- Wait for "Gradle sync finished" message
- You'll see a green play button ▶️ at the top

### Step 3: Run the App

**Option A: Using Emulator**
1. Click the device dropdown (next to play button)
2. Select "Create Device" if no emulator exists
3. Choose "Pixel 5" or any phone
4. Download system image (API 34 recommended)
5. Click "Finish"
6. Click the green play button ▶️
7. Wait for emulator to start (1-2 minutes)

**Option B: Using Physical Device**
1. Enable Developer Options on your phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Settings → Developer Options → USB Debugging
3. Connect phone via USB
4. Click the green play button ▶️
5. Select your device

### Step 4: App Should Launch

You'll see:
- Novel Platform home screen
- List of novels (empty initially)
- Login option

✅ **Android app is working!**

---

## Part 3: Upload Your First Novel (2 minutes)

### Using Command Line

```bash
# 1. Login and get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@novelplatform.com","password":"admin123"}' \
  | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo "Token: $TOKEN"

# 2. Upload a PDF novel
curl -X POST http://localhost:8080/api/admin/upload-pdf \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/your/novel.pdf" \
  -F "title=My First Novel" \
  -F "author=John Doe" \
  -F "genreId=2" \
  -F "description=An amazing adventure story"

# AI will automatically:
# - Extract text from PDF
# - Detect chapters
# - Moderate content
# - Save to database
```

### Using Android App

1. Open the app
2. Login with:
   - Email: `admin@novelplatform.com`
   - Password: `admin123`
3. Navigate to admin panel
4. Click "Upload Novel"
5. Select PDF file
6. Fill in details
7. Click "Upload"

✅ **Novel uploaded and processed by AI!**

---

## Quick Reference Commands

### Start Everything
```bash
# Terminal 1: Start database
docker-compose up -d

# Terminal 2: Start backend
cd backend
mvn spring-boot:run

# Terminal 3: Open Android Studio
# File → Open → Select android folder
# Click Run ▶️
```

### Stop Everything
```bash
# Stop backend: Press Ctrl+C in terminal

# Stop database
docker-compose down

# Stop Android app: Close emulator
```

### Restart Backend
```bash
# In backend terminal, press Ctrl+C
# Then run again
mvn spring-boot:run
```

---

## Troubleshooting

### Problem: "Port 8080 already in use"

**Solution:**
```bash
# Find what's using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or use different port
mvn spring-boot:run -Dserver.port=8081
```

### Problem: "Cannot connect to database"

**Solution:**
```bash
# Check if Docker is running
docker ps

# If not running, start it
docker-compose up -d

# Check logs
docker-compose logs
```

### Problem: "OpenAI API key not set"

**Solution:**
```bash
# Set the environment variable
export OPENAI_API_KEY="sk-your-key-here"

# Restart backend
mvn spring-boot:run
```

### Problem: "Android app can't connect to backend"

**Solution:**

**For Emulator:**
- Backend URL should be: `http://10.0.2.2:8080`
- Check in `RetrofitClient.kt`

**For Physical Device:**
- Find your computer's IP: `ifconfig` (Mac/Linux) or `ipconfig` (Windows)
- Update URL to: `http://YOUR_IP:8080`
- Make sure phone and computer are on same WiFi

### Problem: "Maven dependencies not downloading"

**Solution:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Try again
mvn clean install
```

### Problem: "Gradle sync failed in Android Studio"

**Solution:**
1. File → Invalidate Caches → Invalidate and Restart
2. Or: `./gradlew clean build` in android folder

---

## Testing Your Setup

### Automated Test Script

```bash
# Make script executable
chmod +x backend/test-openai.sh

# Run tests
./backend/test-openai.sh
```

Expected output:
```
✅ All tests completed!
   • OpenAI API: Connected
   • Genre Detection: Working
   • Summary Generation: Working
   • Content Moderation: Working
```

---

## Development Workflow

### Daily Development

```bash
# Morning: Start everything
docker-compose up -d
cd backend && mvn spring-boot:run

# Open Android Studio and run app

# Evening: Stop everything
# Ctrl+C in backend terminal
docker-compose down
```

### Making Changes

**Backend Changes:**
1. Edit Java files
2. Press Ctrl+C to stop
3. Run `mvn spring-boot:run` again

**Android Changes:**
1. Edit Kotlin files
2. Click Run ▶️ (Android Studio will rebuild)

---

## Monitoring

### Check Backend Logs
```bash
# Backend terminal shows all logs
# Look for:
# - API requests
# - AI processing
# - Errors
```

### Check Database
```bash
# Connect to database
docker exec -it noveldb psql -U postgres -d noveldb

# List tables
\dt

# Query novels
SELECT id, title, status FROM novels;

# Exit
\q
```

### Check AI Costs
```bash
curl http://localhost:8080/api/admin/costs/summary \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## URLs Reference

| Service | URL | Credentials |
|---------|-----|-------------|
| Backend API | http://localhost:8080 | - |
| Database | localhost:5432 | postgres/password |
| OpenAI Dashboard | https://platform.openai.com/usage | Your account |
| Admin Login | Via app or API | admin@novelplatform.com / admin123 |

---

## Next Steps

1. ✅ Backend running on http://localhost:8080
2. ✅ Android app running on emulator/device
3. ✅ Upload your first novel
4. ✅ Test AI features
5. ✅ Monitor costs
6. 🚀 Deploy to production (when ready)

---

## Need Help?

- Check logs in backend terminal
- Check `SETUP.md` for detailed setup
- Check `QUICKSTART_OPENAI.md` for OpenAI setup
- Check `TROUBLESHOOTING.md` for common issues

---

## Summary

**To run your application:**

```bash
# 1. Set OpenAI key
export OPENAI_API_KEY="sk-your-key"

# 2. Start database
docker-compose up -d

# 3. Start backend
cd backend && mvn spring-boot:run

# 4. Open Android Studio → Open android folder → Run ▶️

# 5. Upload novels and enjoy! 🎉
```

**That's it! Your novel platform is running!**
