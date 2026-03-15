# Configuration Guide - Where to Get Each Value

This guide explains where each configuration value comes from and what you need to change.

## 📋 Configuration Values Explained

### 1. Database Configuration

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/noveldb
DB_USERNAME=postgres
DB_PASSWORD=password
```

**Where these come from:**
- ✅ **Already configured** - These are default values from `docker-compose.yml`
- 🔧 **No changes needed** for local development

**If you want to change them:**
1. Edit `docker-compose.yml`:
```yaml
environment:
  POSTGRES_DB: noveldb        # Change database name
  POSTGRES_USER: postgres     # Change username
  POSTGRES_PASSWORD: password # Change password
```

2. Update `.env` file to match

**For production:**
- Use your cloud database URL (AWS RDS, Google Cloud SQL, etc.)
- Example: `jdbc:postgresql://your-db-host.amazonaws.com:5432/noveldb`

---

### 2. JWT Secret

```bash
JWT_SECRET=your-secret-key-min-256-bits-change-in-production
```

**Where this comes from:**
- ⚠️ **YOU MUST CHANGE THIS** for production
- This is used to sign authentication tokens

**How to generate a secure secret:**

**Option 1: Using OpenSSL (Recommended)**
```bash
openssl rand -base64 64
```

**Option 2: Using Python**
```bash
python3 -c "import secrets; print(secrets.token_urlsafe(64))"
```

**Option 3: Using Node.js**
```bash
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

**Example output:**
```
JWT_SECRET=xK8vN2mP9qR5sT7uW1yZ3aB4cD6eF8gH0iJ2kL4mN6oP8qR0sT2uV4wX6yZ8aB0cD2eF4gH6iJ8kL0mN2oP4qR6sT8u
```

**For development:**
- The default value works fine
- ⚠️ **NEVER use default in production!**

---

### 3. OpenAI API Key (REQUIRED)

```bash
OPENAI_API_KEY=sk-your-openai-api-key-here
```

**Where to get this:**

1. **Sign up for OpenAI:**
   - Go to: https://platform.openai.com/signup
   - Create account (email + password)
   - ✅ Get $5 free credit (no credit card required)

2. **Create API Key:**
   - Go to: https://platform.openai.com/api-keys
   - Click "Create new secret key"
   - Name it: "novel-platform"
   - Copy the key (starts with `sk-`)

3. **Set the key:**
```bash
export OPENAI_API_KEY="sk-proj-abc123xyz..."
```

**Key format:**
- Starts with: `sk-proj-` or `sk-`
- Length: ~50-60 characters
- Example: `sk-proj-abc123def456ghi789jkl012mno345pqr678stu901vwx234yz`

**Cost:**
- Free: $5 credit (covers ~416 novels)
- After credit: ~$0.012 per novel

---

### 4. AWS S3 Configuration (OPTIONAL)

```bash
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_S3_BUCKET=novel-platform-storage
AWS_REGION=us-east-1
```

**Where these come from:**
- ⚠️ **OPTIONAL** - Only needed for production file storage
- 🔧 **Not required** for local development (files stored locally)

**How to get AWS credentials:**

1. **Sign up for AWS:**
   - Go to: https://aws.amazon.com
   - Create account (requires credit card, but has free tier)

2. **Create S3 Bucket:**
   - Go to S3 console: https://s3.console.aws.amazon.com
   - Click "Create bucket"
   - Bucket name: `novel-platform-storage` (must be globally unique)
   - Region: Choose closest to you (e.g., `us-east-1`)
   - Click "Create"

3. **Create IAM User:**
   - Go to IAM: https://console.aws.amazon.com/iam
   - Users → Add user
   - Name: `novel-platform-app`
   - Access type: Programmatic access
   - Permissions: Attach `AmazonS3FullAccess` policy
   - Copy `Access Key ID` and `Secret Access Key`

**Example values:**
```bash
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
AWS_S3_BUCKET=my-novel-platform-bucket
AWS_REGION=us-east-1
```

**For development:**
- Leave these empty
- Files will be stored locally in `backend/uploads/`

---

### 5. AI Configuration

```bash
AI_ENABLED=true
AI_PROVIDER=openai
```

**Where these come from:**
- ✅ **Already configured** - Default values
- 🔧 **No changes needed**

**Options:**
- `AI_ENABLED`: `true` or `false`
- `AI_PROVIDER`: `openai`, `ollama`, or `huggingface`

**To disable AI (use regex fallback):**
```bash
AI_ENABLED=false
```

**To use free local AI:**
```bash
AI_PROVIDER=ollama
```

---

## 🎯 Quick Setup for Different Environments

### For Local Development (Minimal Setup)

**Required:**
```bash
export OPENAI_API_KEY="sk-your-key-here"
```

**Optional (use defaults):**
- Database: Uses Docker defaults
- JWT: Uses default (fine for dev)
- AWS: Not needed (files stored locally)

### For Production Deployment

**Required:**
```bash
# Database (from your cloud provider)
DATABASE_URL=jdbc:postgresql://prod-db.amazonaws.com:5432/noveldb
DB_USERNAME=prod_user
DB_PASSWORD=super_secure_password_here

# JWT (generate new secure key)
JWT_SECRET=$(openssl rand -base64 64)

# OpenAI
OPENAI_API_KEY=sk-your-production-key

# AWS S3 (for file storage)
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCY
AWS_S3_BUCKET=prod-novel-platform
AWS_REGION=us-east-1
```

---

## 📝 Step-by-Step: Setting Up Your Configuration

### Step 1: Copy Example File

```bash
cd backend
cp .env.example .env
```

### Step 2: Edit .env File

```bash
nano .env
# or
vim .env
# or open in your text editor
```

### Step 3: Set Required Values

**Minimum for local development:**
```bash
# .env file
OPENAI_API_KEY=sk-your-actual-key-from-openai
```

**Everything else can use defaults!**

### Step 4: Load Environment Variables

**Option A: Source the file**
```bash
source .env
export $(cat .env | xargs)
```

**Option B: Set manually**
```bash
export OPENAI_API_KEY="sk-your-key"
```

**Option C: Let Spring Boot read .env**
- Spring Boot automatically reads from `application.yml`
- Which reads from environment variables
- Which can be set from `.env`

---

## 🔍 How to Verify Your Configuration

### Check Environment Variables

```bash
# Check if OpenAI key is set
echo $OPENAI_API_KEY

# Should output: sk-proj-...

# Check all variables
env | grep -E "OPENAI|DATABASE|JWT"
```

### Test Configuration on Startup

When you run `./START.sh`, you'll see:

**✅ Success:**
```
✅ OpenAI API configured successfully
   Provider: OpenAI GPT-3.5-turbo
   API Key: sk-proj...xyz
```

**❌ Error:**
```
❌ OpenAI API key is NOT configured!
   Please set OPENAI_API_KEY environment variable
```

---

## 🔐 Security Best Practices

### ✅ DO:
- Use environment variables for secrets
- Generate strong JWT secrets (64+ characters)
- Use different secrets for dev/staging/prod
- Rotate API keys periodically
- Use AWS IAM roles in production (instead of keys)

### ❌ DON'T:
- Commit `.env` file to Git (it's in `.gitignore`)
- Share API keys in chat/email
- Use default JWT secret in production
- Hardcode secrets in code
- Use same credentials across environments

---

## 📊 Configuration Priority

Spring Boot loads configuration in this order (later overrides earlier):

1. `application.yml` (default values)
2. `application-{profile}.yml` (profile-specific)
3. Environment variables (highest priority)

**Example:**
```yaml
# application.yml
jwt:
  secret: default-secret

# Environment variable (overrides above)
export JWT_SECRET="my-secure-secret"

# Result: Uses "my-secure-secret"
```

---

## 🎓 Summary Table

| Variable | Required? | Where to Get | Default OK? |
|----------|-----------|--------------|-------------|
| `DATABASE_URL` | Yes | Docker Compose | ✅ Yes (dev) |
| `DB_USERNAME` | Yes | Docker Compose | ✅ Yes (dev) |
| `DB_PASSWORD` | Yes | Docker Compose | ✅ Yes (dev) |
| `JWT_SECRET` | Yes | Generate yourself | ✅ Yes (dev), ❌ No (prod) |
| `OPENAI_API_KEY` | **YES** | https://platform.openai.com/api-keys | ❌ Must set |
| `AWS_ACCESS_KEY_ID` | No | AWS IAM Console | ✅ Optional |
| `AWS_SECRET_ACCESS_KEY` | No | AWS IAM Console | ✅ Optional |
| `AWS_S3_BUCKET` | No | AWS S3 Console | ✅ Optional |
| `AI_ENABLED` | No | Your choice | ✅ Yes (true) |
| `AI_PROVIDER` | No | Your choice | ✅ Yes (openai) |

---

## 🚀 Quick Start Checklist

For local development, you only need:

- [ ] Get OpenAI API key from https://platform.openai.com/api-keys
- [ ] Set environment variable: `export OPENAI_API_KEY="sk-..."`
- [ ] Run: `./START.sh`

**That's it!** Everything else uses sensible defaults.

---

## 💡 Pro Tips

### Tip 1: Use .env file for convenience
```bash
# Create .env file
cat > backend/.env << EOF
OPENAI_API_KEY=sk-your-key-here
EOF

# Load it
source backend/.env
```

### Tip 2: Add to your shell profile
```bash
# Add to ~/.bashrc or ~/.zshrc
echo 'export OPENAI_API_KEY="sk-your-key"' >> ~/.bashrc
source ~/.bashrc
```

### Tip 3: Use different profiles
```bash
# Development
mvn spring-boot:run -Dspring.profiles.active=dev

# Production
mvn spring-boot:run -Dspring.profiles.active=prod
```

---

## 🆘 Still Confused?

**Just do this for local development:**

```bash
# 1. Get OpenAI key
# Go to: https://platform.openai.com/api-keys

# 2. Set it
export OPENAI_API_KEY="sk-paste-your-key-here"

# 3. Start app
./START.sh
```

**Everything else is already configured with defaults!**
