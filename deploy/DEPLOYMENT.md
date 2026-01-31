# 🚀 GCP VPS Deployment Guide

Complete guide to deploy the Loan Application to your GCP VPS.

## Prerequisites

- GCP VPS running Linux (Ubuntu 20.04+ recommended)
- Domain name pointed to your VPS IP (optional, for SSL)
- GitHub repository with this code

---

## 1️⃣ Initial VPS Setup

SSH into your VPS and run:

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER

# Install Docker Compose v2
sudo apt install docker-compose-plugin -y

# Install Native Nginx
sudo apt install nginx -y
sudo systemctl enable nginx
sudo systemctl start nginx

# Logout and login again for filter changes
exit
```

---

## 2️⃣ Configure Firewall

```bash
# Allow HTTP, HTTPS, and SSH
sudo ufw allow 22/tcp
sudo ufw allow 'Nginx Full'
sudo ufw enable
```

---

## 3️⃣ Clone & Configure Backend

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

### Required `.env` values (Same as before)
Set `DOCKER_IMAGE`, `DB_PASSWORD`, `JWT_SECRET`, etc.

---

## 4️⃣ Configure Nginx (Global Router)

```bash
# Remove default config
sudo rm /etc/nginx/sites-enabled/default

# Copy our config
sudo cp vps-nginx.conf /etc/nginx/sites-available/app_config

# Enable it
sudo ln -s /etc/nginx/sites-available/app_config /etc/nginx/sites-enabled/

# Test config
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

Now Nginx is listening on port 80.
- Requests to `/backend/*` -> go to Docker (localhost:8080)
- Requests to `/` -> go to `/var/www/frontend` (see below)

---

## 5️⃣ Deployment: Frontend (Static) & Backend (Docker)

### Backend (Docker)
```bash
cd /opt/app/deploy
docker compose pull
docker compose up -d
```
Your backend is now running effectively at `http://localhost:8080`, but Nginx exposes it at `http://YOUR_IP/backend/`.

### Frontend (Static Files)
If you have a frontend build (React/Vue/etc):
```bash
sudo mkdir -p /var/www/frontend
sudo chown -R $USER:$USER /var/www/frontend

# Copy your build files (index.html, css, js) here
# scp -r build/* user@vps-ip:/var/www/frontend/
```

---

## 6️⃣ SSL Setup (Native Nginx - Easy Mode!)

```bash
# Install Certbot for Nginx
sudo apt install certbot python3-certbot-nginx -y

# Run Certbot (Follow the prompts)
sudo certbot --nginx -d yourdomain.com
```
Certbot will **automatically edit** your Nginx config to add SSL lines and redirect HTTP to HTTPS. No manual config needed!

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
