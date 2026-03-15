# Install Prerequisites

## What You Need

1. Java 17 or higher
2. Maven 3.6 or higher
3. Docker Desktop
4. Android Studio (for mobile app)

---

## 1. Install Java 17

### Check if Already Installed
```bash
java -version
```

If you see `java version "17"` or higher, skip to Maven.

### Mac
```bash
# Using Homebrew
brew install openjdk@17

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Verify
java -version
```

### Ubuntu/Debian Linux
```bash
sudo apt update
sudo apt install openjdk-17-jdk

# Verify
java -version
```

### Windows
1. Download from: https://adoptium.net/temurin/releases/
2. Choose:
   - Version: 17 (LTS)
   - Operating System: Windows
   - Architecture: x64
3. Download and run installer
4. Verify in Command Prompt:
```cmd
java -version
```

---

## 2. Install Maven

### Check if Already Installed
```bash
mvn -version
```

If you see `Apache Maven 3.6` or higher, skip to Docker.

### Mac
```bash
brew install maven

# Verify
mvn -version
```

### Ubuntu/Debian Linux
```bash
sudo apt update
sudo apt install maven

# Verify
mvn -version
```

### Windows
1. Download from: https://maven.apache.org/download.cgi
2. Download `apache-maven-3.9.x-bin.zip`
3. Extract to `C:\Program Files\Apache\maven`
4. Add to PATH:
   - Right-click "This PC" → Properties
   - Advanced system settings → Environment Variables
   - System variables → Path → Edit
   - Add: `C:\Program Files\Apache\maven\bin`
5. Verify in Command Prompt:
```cmd
mvn -version
```

---

## 3. Install Docker Desktop

### Mac
1. Download from: https://www.docker.com/products/docker-desktop
2. Choose "Mac with Intel chip" or "Mac with Apple chip"
3. Open downloaded .dmg file
4. Drag Docker to Applications
5. Open Docker Desktop
6. Wait for "Docker Desktop is running"
7. Verify:
```bash
docker --version
```

### Windows
1. Download from: https://www.docker.com/products/docker-desktop
2. Run installer
3. Follow installation wizard
4. Restart computer if prompted
5. Open Docker Desktop
6. Wait for "Docker Desktop is running"
7. Verify in Command Prompt:
```cmd
docker --version
```

### Ubuntu Linux
```bash
# Install Docker
sudo apt update
sudo apt install docker.io

# Start Docker
sudo systemctl start docker
sudo systemctl enable docker

# Add user to docker group (no sudo needed)
sudo usermod -aG docker $USER

# Log out and log back in

# Verify
docker --version
```

---

## 4. Install Android Studio (Optional - for mobile app)

### All Platforms
1. Download from: https://developer.android.com/studio
2. Run installer
3. Follow setup wizard
4. Install:
   - Android SDK
   - Android SDK Platform
   - Android Virtual Device
5. Open Android Studio
6. Configure → SDK Manager
7. Install:
   - Android 14.0 (API 34)
   - Android SDK Build-Tools
   - Android Emulator

---

## 5. Verify All Installations

Run this script to check everything:

```bash
#!/bin/bash

echo "Checking prerequisites..."
echo ""

# Check Java
if command -v java &> /dev/null; then
    echo "✅ Java installed:"
    java -version
else
    echo "❌ Java NOT installed"
fi

echo ""

# Check Maven
if command -v mvn &> /dev/null; then
    echo "✅ Maven installed:"
    mvn -version | head -1
else
    echo "❌ Maven NOT installed"
fi

echo ""

# Check Docker
if command -v docker &> /dev/null; then
    echo "✅ Docker installed:"
    docker --version
else
    echo "❌ Docker NOT installed"
fi

echo ""

# Check if Docker is running
if docker info &> /dev/null; then
    echo "✅ Docker is running"
else
    echo "❌ Docker is NOT running (start Docker Desktop)"
fi

echo ""
echo "Done!"
```

Save as `check-prerequisites.sh` and run:
```bash
chmod +x check-prerequisites.sh
./check-prerequisites.sh
```

---

## 🎯 Quick Install (All at Once)

### Mac
```bash
# Install Homebrew if not installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install everything
brew install openjdk@17 maven

# Download Docker Desktop manually from:
# https://www.docker.com/products/docker-desktop
```

### Ubuntu
```bash
# Install everything
sudo apt update
sudo apt install -y openjdk-17-jdk maven docker.io

# Start Docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Log out and log back in
```

---

## 🆘 Common Issues

### Java: "JAVA_HOME not set"
```bash
# Mac
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Linux
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Windows (Command Prompt)
set JAVA_HOME=C:\Program Files\Java\jdk-17
```

### Maven: "mvn: command not found"
- Make sure Maven bin directory is in PATH
- Restart terminal after installation

### Docker: "Cannot connect to Docker daemon"
- Make sure Docker Desktop is running
- Check system tray (Mac/Windows) or `systemctl status docker` (Linux)

---

## ✅ Ready to Start

Once all prerequisites are installed:

```bash
# Verify everything
java -version    # Should show 17+
mvn -version     # Should show 3.6+
docker --version # Should show version
docker ps        # Should not error

# Start your application
./START.sh
```

---

## 📚 Next Steps

- [START_HERE.md](START_HERE.md) - Start your application
- [RUN_APPLICATION.md](RUN_APPLICATION.md) - Detailed guide
- [QUICKSTART_OPENAI.md](QUICKSTART_OPENAI.md) - OpenAI setup
