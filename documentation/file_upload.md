# Secure File Upload Implementation

This document details the security measures implemented for file uploads in the application.

## Key Security Features

### 1. Magic Number Validation (Apache Tika)
We do not rely on file extensions or the `Content-Type` header provided by the client, as these can be easily spoofed. Instead, we use **Apache Tika** to inspect the actual file bytes (magic numbers) to detect the real MIME type.

**Allowed Types:**
- `image/jpeg`
- `image/png`
- `image/gif`
- `application/pdf`

### 2. Consistency Checks
We verify that the file extension, the client-provided `Content-Type`, and the detected MIME type are all consistent. This prevents "chameleon" attacks where a file is valid as one type but served as another.

### 3. File Size Limits
To prevent DoS attacks, file sizes are strictly limited in `application.yml`:
- **Max File Size:** 5MB
- **Max Request Size:** 10MB

### 4. Secure Filename Generation
Files are renamed using a UUID + Original Filename strategy to prevent directory traversal attacks and filename collisions.
`uuid_filename.ext`

### 5. Secure File Serving
Files are served via `/uploads/{filename}` with strict security headers:
- `Content-Disposition: attachment; filename="..."`: Forces download (optional, can be inline if trusted).
- `X-Content-Type-Options: nosniff`: Prevents browsers from MIME-sniffing the response away from the declared content-type.
- `Content-Security-Policy: default-src 'none'`: Prevents the file from executing scripts or loading resources.

---

## Testing Guide

You can test these security features using Postman or `curl`.

### 1. Happy Path (Valid Image)
Upload a valid JPEG or PNG image.
**Result:** 200 OK, File saved.

### 2. File Injection Attempt (Malicious Text File)
1. Create a text file named `malicious.txt` with content: `alert('xss')`.
2. Rename it to `malicious.jpg`.
3. Attempt to upload it.
**Result:** 500 Internal Server Error (Message: "Invalid file type. Detected: text/plain...")

### 3. Consistency Check Failure
1. Take a real PDF file.
2. Rename it to `document.jpg`.
3. Upload it.
**Result:** 500 Internal Server Error (Message: "File content type mismatch...")

### 4. File Size Limit
Attempt to upload a file larger than 5MB.
**Result:** 413 Payload Too Large.

### 5. Verify Security Headers
Download a successfully uploaded file and inspect the response headers.

**Command:**
```bash
curl -I -X GET http://localhost:8081/uploads/{filename}
```


### 6. Testing with CSRF Enabled (Postman)
If you have removed the upload endpoint from `ignoringRequestMatchers` in `SecurityConfig`, you MUST provide a CSRF token.

1.  **Get Token:** Make a `GET` request to `http://localhost:8081/api/csrf-token` (or login).
2.  **Extract Cookie:** Look at the **Cookies** tab in the response. Copy the value of the `XSRF-TOKEN` cookie.
3.  **Add Header:** In your `POST` request to `upload-ktp`, add a header:
    *   **Key:** `X-XSRF-TOKEN`
    *   **Value:** [Paste the token value from the cookie]

> **Note:** The Bearer token handles **Authentication** (who you are), while the XSRF-TOKEN handles **CSRF Protection** (security against malicious cross-site requests). You need **BOTH** for non-GET requests when CSRF is enabled.


---

## Code Reference

**Validation Logic:** `src/main/java/com/example/demo/service/FileValidationService.java`
**Controller:** `src/main/java/com/example/demo/controller/FileController.java`
