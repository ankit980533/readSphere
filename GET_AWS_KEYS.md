# How to Get AWS Access Key and Secret Key

## Step-by-Step Guide (10 minutes)

---

## Step 1: Create AWS Account (5 minutes)

### 1.1 Go to AWS Website
- Open: https://aws.amazon.com
- Click "Create an AWS Account" (top right)

### 1.2 Fill in Account Details
- **Email address:** Your email
- **Password:** Choose a strong password
- **AWS account name:** "Novel Platform" (or any name)
- Click "Continue"

### 1.3 Contact Information
- **Account type:** Personal (or Business)
- **Full name:** Your name
- **Phone number:** Your phone
- **Country:** Your country
- **Address:** Your address
- Click "Create Account and Continue"

### 1.4 Payment Information
- **Credit/Debit card:** Enter card details
- ⚠️ **Note:** AWS requires a card but won't charge you if you stay in free tier
- **Free tier:** 5GB storage, 20,000 requests/month for 12 months
- Click "Verify and Add"

### 1.5 Identity Verification
- **Phone verification:** Enter phone number
- **Receive code:** Via SMS or call
- **Enter code:** 4-digit code
- Click "Continue"

### 1.6 Select Support Plan
- Choose "Basic support - Free"
- Click "Complete sign up"

✅ **Account created!** You'll receive a confirmation email.

---

## Step 2: Sign in to AWS Console (1 minute)

### 2.1 Go to AWS Console
- Open: https://console.aws.amazon.com
- Click "Sign in to the Console"
- Enter your email and password
- Click "Sign in"

✅ **You're now in AWS Console!**

---

## Step 3: Create IAM User (3 minutes)

### 3.1 Open IAM Service
- In AWS Console, search bar at top: Type "IAM"
- Click "IAM" (Identity and Access Management)
- Or go directly to: https://console.aws.amazon.com/iam

### 3.2 Create New User
- Left sidebar: Click "Users"
- Click "Create user" button (orange button)

### 3.3 User Details
- **User name:** `novel-platform-app`
- ✅ Check "Provide user access to the AWS Management Console" (optional)
- Click "Next"

### 3.4 Set Permissions
- Select "Attach policies directly"
- Search for: `AmazonS3FullAccess`
- ✅ Check the box next to "AmazonS3FullAccess"
- Click "Next"

### 3.5 Review and Create
- Review the details
- Click "Create user"

✅ **User created!**

---

## Step 4: Create Access Keys (2 minutes)

### 4.1 Open User Details
- Click on the user you just created: `novel-platform-app`
- Click "Security credentials" tab

### 4.2 Create Access Key
- Scroll down to "Access keys" section
- Click "Create access key" button

### 4.3 Select Use Case
- Choose "Application running outside AWS"
- ✅ Check "I understand the above recommendation"
- Click "Next"

### 4.4 Description (Optional)
- **Description tag:** "Novel Platform Backend"
- Click "Create access key"

### 4.5 Download Keys
- ✅ **Access key ID:** Shows like `AKIAIOSFODNN7EXAMPLE`
- ✅ **Secret access key:** Shows like `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`

**⚠️ IMPORTANT:**
- Click "Download .csv file" (save it safely!)
- **This is the ONLY time you'll see the secret key!**
- If you lose it, you'll need to create a new one

### 4.6 Done
- Click "Done"

✅ **Access keys created!**

---

## Step 5: Create S3 Bucket (2 minutes)

### 5.1 Open S3 Service
- In AWS Console, search: "S3"
- Click "S3"
- Or go to: https://s3.console.aws.amazon.com

### 5.2 Create Bucket
- Click "Create bucket" button

### 5.3 Bucket Settings
- **Bucket name:** `novel-platform-storage-YOUR-NAME`
  - Must be globally unique
  - Example: `novel-platform-storage-john123`
  - Only lowercase letters, numbers, hyphens
  
- **AWS Region:** Choose closest to you
  - India: `ap-south-1` (Mumbai)
  - US East: `us-east-1` (N. Virginia)
  - Europe: `eu-west-1` (Ireland)

### 5.4 Block Public Access
- ❌ **Uncheck** "Block all public access"
- ✅ Check "I acknowledge that the current settings..."
- (This allows public URLs for PDFs and images)

### 5.5 Create
- Scroll down
- Click "Create bucket"

✅ **Bucket created!**

---

## Step 6: Set Bucket Policy (1 minute)

### 6.1 Open Bucket
- Click on your bucket name

### 6.2 Go to Permissions
- Click "Permissions" tab

### 6.3 Edit Bucket Policy
- Scroll to "Bucket policy"
- Click "Edit"

### 6.4 Paste Policy
Replace `YOUR-BUCKET-NAME` with your actual bucket name:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::YOUR-BUCKET-NAME/*"
    }
  ]
}
```

Example:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::novel-platform-storage-john123/*"
    }
  ]
}
```

### 6.5 Save
- Click "Save changes"

✅ **Bucket is now public for reading!**

---

## Step 7: Configure Your Application

### 7.1 Set Environment Variables

**On Linux/Mac:**
```bash
export AWS_S3_ENABLED=true
export AWS_S3_BUCKET="novel-platform-storage-john123"
export AWS_REGION="ap-south-1"
export AWS_ACCESS_KEY_ID="AKIAIOSFODNN7EXAMPLE"
export AWS_SECRET_ACCESS_KEY="wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
```

**On Windows (Command Prompt):**
```cmd
set AWS_S3_ENABLED=true
set AWS_S3_BUCKET=novel-platform-storage-john123
set AWS_REGION=ap-south-1
set AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
set AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

**On Windows (PowerShell):**
```powershell
$env:AWS_S3_ENABLED="true"
$env:AWS_S3_BUCKET="novel-platform-storage-john123"
$env:AWS_REGION="ap-south-1"
$env:AWS_ACCESS_KEY_ID="AKIAIOSFODNN7EXAMPLE"
$env:AWS_SECRET_ACCESS_KEY="wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
```

### 7.2 Or Create .env File

```bash
# backend/.env
AWS_S3_ENABLED=true
AWS_S3_BUCKET=novel-platform-storage-john123
AWS_REGION=ap-south-1
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

---

## Step 8: Test Your Setup

### 8.1 Start Your Application
```bash
./START.sh
```

### 8.2 Check Logs
Look for:
```
✅ AWS S3 initialized: bucket=novel-platform-storage-john123, region=ap-south-1
```

### 8.3 Test Upload
```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@novelplatform.com","password":"admin123"}' \
  | grep -o '"token":"[^"]*' | cut -d'"' -f4)

# Upload PDF
curl -X POST http://localhost:8080/api/admin/upload-pdf \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test.pdf" \
  -F "title=Test Novel" \
  -F "author=Test Author" \
  -F "genreId=1"
```

### 8.4 Verify in S3
- Go to S3 Console
- Open your bucket
- You should see: `pdfs/uuid-test-novel.pdf`

✅ **AWS S3 is working!**

---

## 🔐 Security Best Practices

### 1. Never Commit Keys to Git
```bash
# .gitignore already includes:
.env
*.env
```

### 2. Rotate Keys Regularly
- Every 90 days, create new keys
- Delete old keys

### 3. Use IAM Roles in Production
- For EC2, ECS, Lambda: Use IAM roles instead of keys
- More secure, no keys to manage

### 4. Limit Permissions
- Only give S3 access, not full AWS access
- Use `AmazonS3FullAccess` policy only

---

## 📊 Your Keys Summary

After completing all steps, you should have:

```
✅ AWS Account created
✅ IAM User: novel-platform-app
✅ Access Key ID: AKIA...
✅ Secret Access Key: wJal...
✅ S3 Bucket: novel-platform-storage-xxx
✅ Bucket Region: ap-south-1 (or your choice)
✅ Bucket Policy: Public read enabled
```

---

## 🆘 Troubleshooting

### Problem: "Access Denied" when uploading

**Solution 1:** Check IAM user has S3 permissions
- Go to IAM → Users → novel-platform-app
- Permissions tab
- Should have "AmazonS3FullAccess"

**Solution 2:** Check bucket policy
- Go to S3 → Your bucket → Permissions
- Bucket policy should allow public read

### Problem: "Bucket does not exist"

**Solution:** Check bucket name and region match
```bash
echo $AWS_S3_BUCKET
echo $AWS_REGION
```

### Problem: "Invalid credentials"

**Solution:** Verify keys are correct
```bash
echo $AWS_ACCESS_KEY_ID
echo $AWS_SECRET_ACCESS_KEY
```

### Problem: Lost secret key

**Solution:** Create new access key
1. Go to IAM → Users → novel-platform-app
2. Security credentials tab
3. Delete old key
4. Create new access key
5. Update environment variables

---

## 💰 Cost Monitoring

### Check Your Usage
1. Go to: https://console.aws.amazon.com/billing
2. Click "Bills" in left sidebar
3. See current month charges

### Set Billing Alert
1. Go to: https://console.aws.amazon.com/billing
2. Click "Billing preferences"
3. Enable "Receive Billing Alerts"
4. Go to CloudWatch: https://console.aws.amazon.com/cloudwatch
5. Create alarm for billing > $5

---

## 📝 Quick Reference

### AWS Console URLs
- Main Console: https://console.aws.amazon.com
- IAM: https://console.aws.amazon.com/iam
- S3: https://s3.console.aws.amazon.com
- Billing: https://console.aws.amazon.com/billing

### Key Format
- Access Key ID: `AKIA` + 16 characters
- Secret Access Key: 40 characters (letters, numbers, symbols)

### Bucket Naming Rules
- 3-63 characters
- Lowercase letters, numbers, hyphens only
- Must be globally unique
- No underscores, spaces, or uppercase

---

## ✅ Checklist

- [ ] AWS account created
- [ ] Signed in to AWS Console
- [ ] IAM user created
- [ ] Access keys downloaded
- [ ] S3 bucket created
- [ ] Bucket policy set (public read)
- [ ] Environment variables set
- [ ] Application tested
- [ ] Keys stored safely
- [ ] .csv file backed up

---

## 🎉 You're Done!

Your AWS S3 is now configured and ready to store files!

**Next steps:**
1. Start your application: `./START.sh`
2. Upload a novel with PDF
3. Check S3 bucket for uploaded file
4. Download PDF from S3 URL

**Cost:** ~$0.01/month for 100 novels (within free tier)

---

## 📚 Related Guides

- [WHERE_TO_USE_AWS.md](WHERE_TO_USE_AWS.md) - Why use S3
- [AWS_S3_INTEGRATION.md](AWS_S3_INTEGRATION.md) - Technical details
- [CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md) - All config options
