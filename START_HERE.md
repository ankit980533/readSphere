# 🚀 START YOUR APPLICATION - Simple Guide

## ⚡ Quick Start (3 Steps)

### Step 1: Get OpenAI API Key (2 minutes)
```bash
# 1. Go to: https://platform.openai.com/signup
# 2. Create free account (get $5 credit)
# 3. Go to: https://platform.openai.com/api-keys
# 4. Click "Create new secret key"
# 5. Copy the key (starts with sk-)

# 6. Set it:
export OPENAI_API_KEY="sk-paste-your-key-here"
```

### Step 2: Start Application (1 command)
```bash
./START.sh
```

### Step 3: Wait for Success Message
Look for:
```
✅ OpenAI API configured successfully
Started NovelApplication in 8.5 seconds
```

**That's it! Backend is running on http://localhost:8080**

---

## 📱 Start Android App (Optional)

1. Open Android Studio
2. File → Open → Select `android` folder
3. Wait for Gradle sync (2-3 minutes)
4. Click Run ▶️ button
5. Select emulator or device

---

## 🧪 Test if Working

Open new terminal:
```bash
./TEST_API.sh
```

Should show:
```
✅ All API tests passed!
Your application is ready to use!
```

---

## 🛑 Stop Application

```bash
# Press Ctrl+C in the terminal where backend is running

# Or run:
./STOP.sh
```

---

## 📋 Prerequisites

Before starting, make sure you have:
- [ ] Java 17+ installed (`java -version`)
- [ ] Maven installed (`mvn -version`)
- [ ] Docker installed (`docker --version`)
- [ ] OpenAI API key

**Don't have these?** See [INSTALL_PREREQUISITES.md](INSTALL_PREREQUISITES.md)

---

## 🆘 Troubleshooting

### Error: "OPENAI_API_KEY not set"
```bash
export OPENAI_API_KEY="sk-your-key-here"
./START.sh
```

### Error: "Port 8080 already in use"
```bash
# Find what's using port 8080
lsof -i :8080

# Kill it
kill -9 <PID>

# Or use different port
cd backend
mvn spring-boot:run -Dserver.port=8081
```

### Error: "Docker not running"
```bash
# Start Docker Desktop
# Then run:
./START.sh
```

### Error: "mvn: command not found"
```bash
# Install Maven first
# Mac: brew install maven
# Ubuntu: sudo apt install maven
# Windows: Download from https://maven.apache.org
```

---

## 📖 Detailed Guides

- **Complete setup:** [RUN_APPLICATION.md](RUN_APPLICATION.md)
- **Configuration:** [CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md)
- **OpenAI setup:** [QUICKSTART_OPENAI.md](QUICKSTART_OPENAI.md)

---

## ✅ Success Checklist

After starting, you should have:
- [ ] Backend running on http://localhost:8080
- [ ] Database running in Docker
- [ ] OpenAI API configured
- [ ] Test API passes

---

## 🎯 What's Next?

1. **Upload a novel:**
   ```bash
   # Login as admin
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@novelplatform.com","password":"admin123"}'
   
   # Upload PDF (replace with your token)
   curl -X POST http://localhost:8080/api/admin/upload-pdf \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -F "file=@novel.pdf" \
     -F "title=My Novel" \
     -F "author=Author Name" \
     -F "genreId=2"
   ```

2. **Open Android app** and browse novels

3. **Monitor AI costs:**
   ```bash
   curl http://localhost:8080/api/admin/costs/summary \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

---

## 💡 Tips

- First run takes 2-5 minutes (downloading dependencies)
- Subsequent runs take ~30 seconds
- Keep terminal open to see logs
- Press Ctrl+C to stop

---

## 🎉 You're Ready!

Your novel platform is now running with AI-powered chapter detection!

**Cost:** $0 (using $5 free OpenAI credit)
