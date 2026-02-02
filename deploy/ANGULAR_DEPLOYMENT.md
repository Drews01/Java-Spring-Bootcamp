# 🅰️ Angular Deployment Guide (VPS + Nginx)

This guide explains how to deploy your Angular application to your VPS so it is accessible directly via your IP address (Port 80/443), instead of using the development port 4200.

## 🏗️ The Concept

1.  **Development**: You use `ng serve` (port 4200). This is for development only.
2.  **Production**: You **build** the app into static files (HTML, CSS, JS).
3.  **Serving**: Nginx (web server) serves these static files on port 80. It also "reverse proxies" API requests to your Spring Boot backend.

---

## 🚀 Step 1: Prepare the VPS (One-time setup)

Your VPS needs Nginx installed to serve the files.

1.  **SSH into your VPS**:
    ```bash
    ssh user@your-vps-ip
    ```

2.  **Install Nginx**:
    ```bash
    sudo apt update
    sudo apt install nginx -y
    sudo systemctl enable nginx
    sudo systemctl start nginx
    ```

3.  **Create the Frontend Directory**:
    This is where your Angular files will live.
    ```bash
    # Create directory
    sudo mkdir -p /var/www/frontend
    
    # Give your user permission to write to this folder
    sudo chown -R $USER:$USER /var/www/frontend
    ```

4.  **Configure Nginx**:
    Use the `vps-nginx.conf` file provided in your `deploy` folder.
    
    ```bash
    # Go to your deploy folder on VPS
    cd /home/github-actions-deploy/backend/Java-Spring-Bootcamp/deploy
    
    # Remove default config
    sudo rm /etc/nginx/sites-enabled/default
    
    # Copy new config configuration to sites-available
    sudo cp vps-nginx.conf /etc/nginx/sites-available/app_config
    
    # Create symlink to enable it
    sudo ln -s /etc/nginx/sites-available/app_config /etc/nginx/sites-enabled/
    
    # Test configuration
    sudo nginx -t
    
    # Reload Nginx
    sudo systemctl reload nginx
    ```

---

## 📦 Step 2: Build & Deploy Angular App

Do this every time you want to update your website.

### 1. Build locally (on your machine)
Run this command in your Angular project folder:

```bash
# Build for production
ng build --configuration production
```

This creates a `dist/` folder.  
*Example path: `dist/my-angular-app/browser/`* (check your exact folder structure inside `dist`).

### 2. Upload to VPS
Copy the **contents** of the build folder to the VPS`/var/www/frontend` directory.

**Using SCP (Command Line):**
```bash
# Replace 'dist/my-app/browser/*' with your actual build path
scp -r dist/your-project-name/browser/* user@your-vps-ip:/var/www/frontend/
```

**Using FileZilla / WinSCP:**
1. Connect to your VPS via SFTP.
2. Navigate to `/var/www/frontend`.
3. Drag and drop all files from your local `dist/your-project-name/browser/` folder.

---

## ✅ Step 3: Verify

Open your browser and visit: `http://your-vps-ip/`

- **Frontend**: Should load your Angular app (Port 80).
- **Backend API**: The app should call API at `/backend/...` which Nginx forwards to Spring Boot (Port 8080).

---

## 🔧 Troubleshooting

**1. "403 Forbidden" or "404 Not Found"**
- Check file permissions:
  ```bash
  sudo chmod -R 755 /var/www/frontend
  ```
- Ensure files are directly in `/var/www/frontend`, not in a subfolder like `/var/www/frontend/browser`.

**2. Browser Refresh gives 404**
- The `vps-nginx.conf` already handles this with `try_files $uri $uri/ /index.html;`.
- Ensure you reloaded Nginx: `sudo systemctl reload nginx`.
