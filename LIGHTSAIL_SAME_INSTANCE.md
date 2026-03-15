# Deploy ReadSphere on Existing Lightsail Instance

Your instance: `makanview-app` (2GB RAM, 2 vCPUs) at `13.235.184.27`

---

## Step 1: SSH into your instance

```bash
ssh -i your-key.pem ubuntu@13.235.184.27
```

---

## Step 2: Install Java 17 (if not installed)

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
java -version
```

---

## Step 3: Install Node.js (if not installed)

```bash
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
node -v
```

---

## Step 4: Install PostgreSQL (or use existing)

```bash
# Install PostgreSQL
sudo apt install -y postgresql postgresql-contrib

# Create database
sudo -u postgres psql -c "CREATE DATABASE noveldb;"
sudo -u postgres psql -c "CREATE USER noveluser WITH PASSWORD 'your-secure-password';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE noveldb TO noveluser;"
```

---

## Step 5: Clone your repo

```bash
cd /home/ubuntu
git clone https://github.com/ankit980533/readSphere.git readsphere
cd readsphere
```


---

## Step 6: Create environment file

```bash
sudo nano /home/ubuntu/novel-platform/backend/.env
```

Add:
```
DATABASE_URL=jdbc:postgresql://localhost:5432/noveldb
DATABASE_USER=noveluser
DATABASE_PASSWORD=your-secure-password
JWT_SECRET=your-64-character-secret-key-here-make-it-long-and-random
OPENAI_API_KEY=sk-your-openai-key
```

---

## Step 7: Create systemd service for backend

```bash
sudo nano /etc/systemd/system/novel-backend.service
```

Add:
```ini
[Unit]
Description=Novel Platform Backend
After=network.target postgresql.service

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/novel-platform/backend
EnvironmentFile=/home/ubuntu/novel-platform/backend/.env
ExecStart=/usr/bin/java -jar target/novel-backend-1.0.0.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable novel-backend
sudo systemctl start novel-backend
```

---

## Step 8: Build and deploy

```bash
# Build backend
cd /home/ubuntu/novel-platform/backend
chmod +x mvnw
./mvnw clean package -DskipTests

# Build frontend
cd ../web
npm install
npm run build

# Create web directory
sudo mkdir -p /var/www/novel-platform
sudo cp -r dist/* /var/www/novel-platform/
```


---

## Step 9: Configure Nginx

```bash
sudo nano /etc/nginx/sites-available/readsphere
```

**Option A: Subdomain (recommended)**
```nginx
server {
    listen 80;
    server_name readsphere.yourdomain.com;

    location / {
        root /var/www/readsphere;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        client_max_body_size 50M;
    }
}
```

**Option B: Path-based (if no subdomain)**
```nginx
# Add to your existing nginx config
location /readsphere {
    alias /var/www/readsphere;
    index index.html;
    try_files $uri $uri/ /readsphere/index.html;
}

location /readsphere/api {
    proxy_pass http://127.0.0.1:8080/api;
    proxy_set_header Host $host;
    client_max_body_size 50M;
}
```

Enable site:
```bash
sudo ln -s /etc/nginx/sites-available/readsphere /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

---

## Step 10: GitHub Secrets for Auto-Deploy

Add these secrets in GitHub repo → Settings → Secrets:

| Secret | Value |
|--------|-------|
| `LIGHTSAIL_HOST` | `13.235.184.27` |
| `LIGHTSAIL_USER` | `ubuntu` |
| `LIGHTSAIL_SSH_KEY` | Your private SSH key content |

---

## How Auto-Deploy Works

1. Push code to `main` branch
2. GitHub Actions SSHs into your Lightsail
3. Pulls latest code
4. Rebuilds backend & frontend
5. Restarts services

---

## Useful Commands

```bash
# Check backend status
sudo systemctl status novel-backend

# View backend logs
sudo journalctl -u novel-backend -f

# Restart backend
sudo systemctl restart novel-backend

# Check nginx
sudo nginx -t
sudo systemctl status nginx
```
