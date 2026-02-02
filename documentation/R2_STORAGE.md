# Cloudflare R2 Storage Configuration

This document explains how to configure Cloudflare R2 for KTP image uploads.

## Overview

The application supports two storage modes:
- **Local**: Files saved to local filesystem (default for development)
- **R2**: Files saved to Cloudflare R2 cloud storage (recommended for production)

---

## Setting Up Cloudflare R2

### Step 1: Create R2 Bucket

1. Log in to [Cloudflare Dashboard](https://dash.cloudflare.com)
2. Navigate to **R2 Object Storage** → **Create bucket**
3. Enter bucket name (e.g., `bootcamp`)
4. Select location (e.g., Asia-Pacific)
5. Click **Create bucket**

After creation, note down:
- **Bucket Name**: `bootcamp`
- **S3 API Endpoint**: `https://<ACCOUNT_ID>.r2.cloudflarestorage.com`

### Step 2: Create API Token

1. Go to **R2 Object Storage** → **Manage R2 API Tokens**
2. Click **Create API Token**
3. Configure:
   | Setting | Value |
   |---------|-------|
   | Token name | `ktp-uploads-token` |
   | Permissions | **Object Read & Write** |
   | Specify bucket(s) | Select your bucket |
4. Click **Create API Token**
5. **Save both values immediately** (secret is only shown once):
   - `Access Key ID` → `R2_ACCESS_KEY`
   - `Secret Access Key` → `R2_SECRET_KEY`

### Step 3: Enable Public Access

1. Go to **R2 Object Storage** → Click your bucket
2. Go to **Settings** tab
3. Under **Public access**, click **Allow Access**
4. Enable **R2.dev subdomain**
5. Copy the URL (e.g., `https://pub-xxx.r2.dev`) → `R2_PUBLIC_URL`

---

## VPS Configuration

### Environment Variables

Add these to your `.env` file in the deploy directory:

```bash
# R2 Storage Configuration
STORAGE_TYPE=r2
R2_ENDPOINT=https://<ACCOUNT_ID>.r2.cloudflarestorage.com
R2_ACCESS_KEY=<your-access-key-id>
R2_SECRET_KEY=<your-secret-access-key>
R2_BUCKET=bootcamp
R2_PUBLIC_URL=https://pub-xxx.r2.dev
```

### Example .env File

```bash
# ===========================================
# Database
# ===========================================
DB_NAME=LoanDB
DB_USER=sa
DB_PASSWORD=YourStrongPassword123!

# ===========================================
# JWT
# ===========================================
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION=86400000

# ===========================================
# Mail (Gmail SMTP)
# ===========================================
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
APP_MAIL_FROM=no-reply@example.com

# ===========================================
# Google OAuth
# ===========================================
GOOGLE_CLIENT_ID=your-google-client-id

# ===========================================
# R2 Storage
# ===========================================
STORAGE_TYPE=r2
R2_ENDPOINT=https://59a1262a0a04ed088283cea2459542c0.r2.cloudflarestorage.com
R2_ACCESS_KEY=your-r2-access-key
R2_SECRET_KEY=your-r2-secret-key
R2_BUCKET=bootcamp
R2_PUBLIC_URL=https://pub-xxxx.r2.dev

# ===========================================
# CORS
# ===========================================
APP_CORS_ALLOWED_ORIGINS=https://your-domain.com
```

### Deployment Steps

1. **SSH into your VPS**:
   ```bash
   ssh user@your-vps-ip
   ```

2. **Navigate to deploy directory**:
   ```bash
   cd /path/to/deploy
   ```

3. **Create/edit .env file**:
   ```bash
   nano .env
   ```

4. **Add the R2 variables** (copy from example above)

5. **Restart the containers**:
   ```bash
   docker compose down
   docker compose up -d
   ```

6. **Verify logs**:
   ```bash
   docker logs loan-app -f
   ```

---

## Testing

### Local Mode (Development)
```bash
# Default - no R2 config needed
mvn spring-boot:run
```
Files will be saved to `./uploads/` directory.

### R2 Mode (Production)
After configuring `.env` with R2 credentials:

1. Upload a KTP image via API:
   ```bash
   curl -X POST http://localhost:8080/api/user-profiles/upload-ktp \
     -H "Authorization: Bearer <jwt-token>" \
     -F "file=@/path/to/ktp.jpg"
   ```

2. Response will contain R2 public URL:
   ```json
   {
     "data": {
       "fileDownloadUri": "https://pub-xxx.r2.dev/ktp/uuid_filename.jpg"
     }
   }
   ```

3. Verify in Cloudflare Dashboard that file appears in bucket.

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `Failed to upload to R2` | Check R2 credentials in `.env` |
| `Access Denied` | Verify API token has Object Read & Write permissions |
| Image URL returns 403 | Enable public access on R2 bucket |
| Images not appearing | Check `STORAGE_TYPE=r2` is set |
