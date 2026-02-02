package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Storage service interface for file uploads. Implementations can use local filesystem or cloud
 * storage (e.g., Cloudflare R2).
 */
public interface StorageService {

  /**
   * Upload a file to storage.
   *
   * @param file the file to upload
   * @param folder the folder/prefix to store the file under
   * @return the public URL or path to access the uploaded file
   */
  String uploadFile(MultipartFile file, String folder);

  /**
   * Delete a file from storage.
   *
   * @param key the file key/path to delete
   */
  void deleteFile(String key);
}
