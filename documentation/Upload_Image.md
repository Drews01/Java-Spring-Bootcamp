# Upload Image Documentation

This document explains how to use the image upload feature (specifically for KTP) and provides a guide for setting up Cloudflare R2 for production storage.

## Overview

The image upload feature allows authenticated users to upload their KTP (identity card) image. The image is currently stored locally in the `uploads/` directory on the server. The path to the image is saved in the `UserProfile` table.

## Implementation Details

### DTOs
- `UploadImageResponse`: Contains details about the uploaded file (filename, download URI, type, size).

### Folder Structure
- `uploads/`: Created at the root of the project to store uploaded files.

### API Usage

#### Endpoint: Upload KTP
- **URL**: `/api/user-profiles/upload-ktp`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`
- **Authorization**: Bearer Token required

#### Request Parameters
| Key | Type | Description |
|---|---|---|
| `file` | File | The image file to upload (e.g., .jpg, .png) |

#### Success Response
```json
{
    "status": "SUCCESS",
    "message": "KTP image uploaded successfully",
    "data": {
        "fileName": "uuid_filename.jpg",
        "fileDownloadUri": "http://localhost:8080/uploads/uuid_filename.jpg",
        "fileType": "image/jpeg",
        "size": 1024
    },
    "timestamp": "2024-01-27T10:00:00"
}
```

## Frontend Integration Flow

It is important to understand that **Upload Image** and **Update Profile Data** are separate operations.

1.  **Image Upload**:
    - When the user selects a file, your frontend should call `POST /api/user-profiles/upload-ktp`.
    - The backend **automatically updates** the `ktp_path` in the database upon successful upload.
    - You do **not** need to manually send the `ktp_path` to the update profile endpoint afterwards. The backend is configured to ignore `null` values for `ktp_path` during updates, so your existing image will be safe.

2.  **Profile Data Update**:
    - When the user fills out text fields (Address, Phone, etc.), call `PUT /api/user-profiles` (or `POST` for creation) with the JSON body.

**Example Scenario**:
User fills out the form and selects an image.
1.  Frontend uploads image -> specific `ktp_path` is saved in DB.
2.  Frontend submits text form -> other fields are updated in DB.

---

## Testing with Postman

Here is a step-by-step guide to test the flow manually using Postman.

### Prerequisites
1.  **Login** to get a JWT Token.
    - POST `/api/auth/login`
    - Copy the `token` from the response.
2.  In all subsequent requests, go to **Authorization** tab -> Select **Bearer Token** -> Paste the token.

### Step 1: Upload KTP
1.  Create a new request: `POST http://localhost:8081/api/user-profiles/upload-ktp`
2.  Go to **Body** tab -> Select **form-data**.
3.  Add a key named `file`.
4.  Hover over the "value" field for `file` and change the type from "Text" to **File**.
5.  Select an image file from your computer.
6.  Send the request.
7.  **Check**: You should receive a 200 OK response with the file details.

### Step 2: Retrieve the Image
1.  From the response in Step 1, copy the `fileDownloadUri` (e.g., `http://localhost:8081/uploads/uuid_filename.jpg`).
2.  Open your browser or create a **GET** request in Postman.
3.  Paste the URL.
4.  **Check**: The image should be displayed.

### Step 3: Update Profile Data
1.  Create a new request: `POST http://localhost:8080/api/user-profiles` (or `PUT` if updating)
2.  Go to **Body** tab -> Select **raw** -> **JSON**.
3.  Enter the profile data JSON. **Important**: You can omit `ktpPath` or set it to `null`.
    ```json
    {
      "address": "123 Main St, Jakarta",
      "nik": "1234567890123456",
      "phoneNumber": "08123456789",
      "accountNumber": "987654321",
      "bankName": "BCA"
    }
    ```
4.  Send the request.
5.  **Check**: The response should return the updated profile. The `ktpPath` field should still contain the path from Step 1 (e.g., `uploads/uuid_filename.jpg`).

---

## Production: Saving to Cloudflare R2

For production environments, it is recommended to use cloud storage like Cloudflare R2 (which is AWS S3 compatible) instead of local storage.

### 1. Add AWS SDK Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
    <version>1.12.600</version> <!-- Check for latest version -->
</dependency>
```

### 2. Configuration

Add the following properties to your `application.yml` (or environment variables):

```yaml
cloud:
  aws:
    credentials:
      access-key: YOUR_R2_ACCESS_KEY_ID
      secret-key: YOUR_R2_SECRET_ACCESS_KEY
    region:
      static: auto # R2 uses 'auto'
    s3:
      endpoint: https://<ACCOUNT_ID>.r2.cloudflarestorage.com
      bucket: YOUR_BUCKET_NAME
```

### 3. Service Implementation (Cloudflare R2 / S3)

Create a new service or modify `UserProfileService` to use `AmazonS3` client.

```java
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class S3Service {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private AmazonS3 s3Client;

    @PostConstruct
    public void init() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, "auto"))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public String uploadFile(MultipartFile file, String folder) {
        String fileName = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            
            s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
            
            // Return public URL or presigned URL depending on bucket settings
            return s3Client.getUrl(bucketName, fileName).toString(); 
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to R2", e);
        }
    }
}
```

Then, inject this `S3Service` into `UserProfileService` and use it to upload the file instead of `Files.copy()`.
