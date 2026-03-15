# Novel Platform Deployment Guide

## Project Components
- **Backend**: Spring Boot 3.2 (Java 17)
- **Frontend**: React + Vite
- **Database**: PostgreSQL 15
- **Android**: Kotlin app (optional)

---

## ⚠️ CRITICAL: Security Fixes Before Deployment

Your `application.yml` contains exposed secrets. Create environment-based configuration:

### 1. Create Production Configuration

Create `backend/src/main/resources/application-prod.yml`:

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USER}
    password: ${DATABASE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # Never use 'update' in production
    show-sql: false

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

openai:
  api:
    key: ${OPENAI_API_KEY}
    url: https://api.openai.com/v1/chat/completions

aws:
  s3:
    bucket: ${AWS_S3_BUCKET}
    region: ${AWS_REGION}
    enabled: ${AWS_S3_ENABLED:false}
  credentials:
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}

server:
  port: ${PORT:8080}
```


---

## Option 1: Deploy to AWS (Recommended for Production)

### A. Using AWS Elastic Beanstalk

```bash
# 1. Build the backend JAR
cd backend
./mvnw clean package -DskipTests

# 2. Build the frontend
cd ../web
npm install
npm run build

# 3. Install EB CLI
pip install awsebcli

# 4. Initialize Elastic Beanstalk
cd ../backend
eb init -p java-17 novel-platform --region ap-south-1

# 5. Create environment
eb create novel-prod --single --instance-type t3.small

# 6. Set environment variables
eb setenv \
  DATABASE_URL=jdbc:postgresql://your-rds-endpoint:5432/noveldb \
  DATABASE_USER=postgres \
  DATABASE_PASSWORD=your-secure-password \
  JWT_SECRET=your-512-bit-secret-key \
  OPENAI_API_KEY=your-openai-key \
  AWS_ACCESS_KEY=your-access-key \
  AWS_SECRET_KEY=your-secret-key \
  SPRING_PROFILES_ACTIVE=prod

# 7. Deploy
eb deploy
```

### B. Database: AWS RDS PostgreSQL

1. Go to AWS Console → RDS → Create Database
2. Choose PostgreSQL 15
3. Select "Free tier" or appropriate size
4. Set master username/password
5. Enable public access if needed
6. Note the endpoint URL


---

## Option 2: Deploy with Docker (Any Cloud Provider)

### Create Production Dockerfile for Backend

Create `backend/Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

### Create Dockerfile for Frontend

Create `web/Dockerfile`:

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### Create nginx.conf for Frontend

Create `web/nginx.conf`:

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```


### Production docker-compose.yml

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: noveldb
      POSTGRES_USER: ${DATABASE_USER}
      POSTGRES_PASSWORD: ${DATABASE_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: always

  backend:
    build: ./backend
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/noveldb
      DATABASE_USER: ${DATABASE_USER}
      DATABASE_PASSWORD: ${DATABASE_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      AWS_ACCESS_KEY: ${AWS_ACCESS_KEY}
      AWS_SECRET_KEY: ${AWS_SECRET_KEY}
      SPRING_PROFILES_ACTIVE: prod
    depends_on:
      - postgres
    restart: always

  web:
    build: ./web
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: always

volumes:
  postgres_data:
```

### Deploy with Docker

```bash
# Create .env file with your secrets
cat > .env << EOF
DATABASE_USER=postgres
DATABASE_PASSWORD=your-secure-password
JWT_SECRET=your-512-bit-secret-key-here
OPENAI_API_KEY=sk-your-key
AWS_ACCESS_KEY=your-key
AWS_SECRET_KEY=your-secret
EOF

# Build and run
docker-compose -f docker-compose.prod.yml up -d --build
```


---

## Option 3: Deploy to Railway/Render (Easiest)

### Railway Deployment

1. Go to [railway.app](https://railway.app)
2. Connect your GitHub repository
3. Add PostgreSQL service
4. Add environment variables in Railway dashboard
5. Deploy automatically on push

### Render Deployment

1. Go to [render.com](https://render.com)
2. Create Web Service for backend (Docker)
3. Create Static Site for frontend
4. Create PostgreSQL database
5. Set environment variables

---

## Option 4: Deploy to VPS (DigitalOcean/Linode)

```bash
# On your VPS (Ubuntu 22.04)

# 1. Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# 2. Install Docker Compose
sudo apt install docker-compose-plugin

# 3. Clone your repo
git clone https://github.com/your-repo/novel-platform.git
cd novel-platform

# 4. Create .env file with secrets

# 5. Run
docker compose -f docker-compose.prod.yml up -d

# 6. Setup SSL with Certbot (optional but recommended)
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com
```


---

## Frontend Configuration for Production

Update `web/vite.config.js` for production API URL:

```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080'
    }
  },
  define: {
    'import.meta.env.VITE_API_URL': JSON.stringify(
      process.env.VITE_API_URL || ''
    )
  }
})
```

---

## Android App Configuration

Update `android/app/src/main/java/.../RetrofitClient.kt`:

```kotlin
// Change BASE_URL to your production backend
private const val BASE_URL = "https://your-domain.com/api/"
```

---

## Pre-Deployment Checklist

- [ ] Remove hardcoded secrets from application.yml
- [ ] Generate strong JWT secret (512+ bits)
- [ ] Set up production database with backups
- [ ] Configure CORS for your domain
- [ ] Enable HTTPS/SSL
- [ ] Set up monitoring (CloudWatch, Datadog, etc.)
- [ ] Configure log aggregation
- [ ] Test all API endpoints
- [ ] Build and test Android APK with production URL

---

## Quick Commands

```bash
# Build backend JAR
cd backend && ./mvnw clean package -DskipTests

# Build frontend
cd web && npm run build

# Build Android APK
cd android && ./gradlew assembleRelease
```
