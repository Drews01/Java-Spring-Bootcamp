# 🚀 GCP VPS Deployment Guide

Complete guide to deploy the Loan Application to your GCP VPS.

## Prerequisites

- GCP VPS running Linux (Ubuntu 20.04+ recommended)
- Domain name pointed to your VPS IP (optional, for SSL)
- GitHub repository with this code

---

## 1️⃣ Initial VPS Setup (One-time)

SSH into your VPS and run:

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER

# Install Docker Compose v2
sudo apt install docker-compose-plugin -y

# Logout and login again for group changes
exit
```

After re-logging in, verify:
```bash
docker --version
docker compose version
```

---

## 2️⃣ Configure Firewall

```bash
# Allow HTTP, HTTPS, and SSH
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

---

## 3️⃣ Clone & Configure

```bash
# Clone repository
cd /opt
sudo mkdir -p app && sudo chown $USER:$USER app
cd app
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git .

# Navigate to deploy folder
cd deploy

# Create environment file from template
cp .env.example .env

# Edit with your values
nano .env
```

### Required `.env` values:
| Variable | Description |
|----------|-------------|
| `DOCKER_IMAGE` | `ghcr.io/your-username/loan-app:latest` |
| `DB_PASSWORD` | Strong password (min 8 chars, mixed case, number, special) |
| `JWT_SECRET` | Run: `openssl rand -base64 64` |
| `MAIL_USERNAME` | Your email |
| `MAIL_PASSWORD` | App password (not regular password) |

---

## 4️⃣ Firebase Setup (For Push Notifications)

```bash
# Copy your Firebase service account JSON to deploy folder
# Name it: firebase-service-account.json
nano firebase-service-account.json
# Paste your Firebase Admin SDK JSON
```

---

## 5️⃣ GitHub Container Registry Login

```bash
# Login to GHCR (use GitHub Personal Access Token with packages:read scope)
echo "YOUR_GITHUB_PAT" | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

---

## 6️⃣ Start the Application

```bash
# Pull images and start
docker compose pull
docker compose up -d

# Check status
docker compose ps

# View logs
docker compose logs -f app
```

---

## 7️⃣ SSL Setup (Optional but Recommended)

### Using Certbot:
```bash
# Install certbot
sudo apt install certbot -y

# Stop nginx temporarily
docker compose stop nginx

# Get certificate
sudo certbot certonly --standalone -d yourdomain.com

# Copy certs
sudo mkdir -p ssl
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem ssl/
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem ssl/
sudo chown $USER:$USER ssl/*

# Edit nginx.conf to enable SSL (uncomment SSL lines)
nano nginx.conf

# Restart nginx
docker compose up -d nginx
```

---

## 📋 Useful Commands

| Command | Description |
|---------|-------------|
| `docker compose ps` | Check container status |
| `docker compose logs -f app` | View app logs |
| `docker compose logs -f sqlserver` | View database logs |
| `docker compose down` | Stop all containers |
| `docker compose up -d` | Start all containers |
| `docker compose pull && docker compose up -d` | Update to latest image |

---

## 🔧 Troubleshooting

### App not starting?
```bash
# Check app logs
docker compose logs app

# Common issues:
# - DB_PASSWORD too weak (needs uppercase, lowercase, number, special char)
# - Firebase JSON missing or malformed
# - JWT_SECRET not set
```

### Database connection issues?
```bash
# Check if SQL Server is healthy
docker compose ps sqlserver

# View SQL Server logs
docker compose logs sqlserver
```

### Reset everything:
```bash
docker compose down -v  # WARNING: Deletes all data!
docker compose up -d
```

---

## 🔄 CI/CD Auto-Deployment

Once GitHub Actions is configured, every push to `main` will:
1. Build & test the code
2. Build Docker image
3. Push to GHCR
4. SSH to your VPS and update containers

See `.github/workflows/ci-cd.yml` for the pipeline configuration.
