# AWS Lightsail Deployment with GitHub Auto-Deploy

## Architecture
- **Lightsail Container Service**: Runs your backend + frontend
- **Lightsail Database**: PostgreSQL
- **GitHub Actions**: Auto-deploy on push

---

## Step 1: Create Lightsail Database

1. Go to [Lightsail Console](https://lightsail.aws.amazon.com)
2. Click **Databases** → **Create database**
3. Choose:
   - PostgreSQL 15
   - Standard plan ($15/month minimum for production)
   - Name: `novel-db`
4. Wait for it to be "Available" (takes ~10 min)
5. Note down:
   - Endpoint (e.g., `novel-db.xxxxx.ap-south-1.rds.amazonaws.com`)
   - Port: `5432`
   - Username: `dbmasteruser`
   - Password: (the one you set)

---

## Step 2: Create Lightsail Container Service

1. Go to **Containers** → **Create container service**
2. Choose:
   - Region: `ap-south-1` (Mumbai)
   - Capacity: Nano ($7/month) or Micro ($25/month)
   - Scale: 1
   - Name: `novel-platform`
3. Skip deployment for now, click **Create**


---

## Step 3: Set Up GitHub Secrets

Go to your GitHub repo → **Settings** → **Secrets and variables** → **Actions**

Add these secrets:

| Secret Name | Value |
|-------------|-------|
| `AWS_ACCESS_KEY_ID` | Your AWS access key |
| `AWS_SECRET_ACCESS_KEY` | Your AWS secret key |
| `DATABASE_URL` | `jdbc:postgresql://your-db-endpoint:5432/postgres` |
| `DATABASE_USER` | `dbmasteruser` |
| `DATABASE_PASSWORD` | Your DB password |
| `JWT_SECRET` | A 64+ character random string |
| `OPENAI_API_KEY` | Your OpenAI key |

---

## Step 4: GitHub Actions Workflow

The workflow below will:
1. Build backend JAR
2. Build frontend
3. Create Docker image
4. Push to Lightsail
5. Deploy automatically

This happens on every push to `main` branch.


---

## Files Created for Deployment

| File | Purpose |
|------|---------|
| `.github/workflows/deploy-lightsail.yml` | GitHub Actions auto-deploy |
| `Dockerfile.combined` | Single container with backend + frontend |
| `nginx-lightsail.conf` | Nginx config for routing |
| `start-services.sh` | Startup script |
| `HealthController.java` | Health check endpoint |

---

## Step 5: Enable Database Public Access

1. Go to Lightsail → Databases → `novel-db`
2. Click **Networking** tab
3. Enable **Public mode**
4. Add your Lightsail container service to allowed connections

---

## Step 6: First Deployment

1. Push your code to GitHub `main` branch:
```bash
git add .
git commit -m "Add Lightsail deployment"
git push origin main
```

2. Go to GitHub → Actions tab to watch the deployment

3. Once complete, go to Lightsail → Containers → `novel-platform`
4. Find your public URL (e.g., `novel-platform.xxxxx.ap-south-1.cs.amazonlightsail.com`)

---

## Costs (Estimated)

| Service | Cost/Month |
|---------|------------|
| Container (Nano) | $7 |
| Container (Micro) | $25 |
| Database (Standard) | $15 |
| **Total (Nano)** | **~$22/month** |

---

## Troubleshooting

**View logs:**
```bash
aws lightsail get-container-log \
  --service-name novel-platform \
  --container-name app
```

**Check deployment status:**
```bash
aws lightsail get-container-service-deployments \
  --service-name novel-platform
```
