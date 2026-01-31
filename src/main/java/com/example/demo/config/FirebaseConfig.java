package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/** Firebase configuration for FCM (Firebase Cloud Messaging) push notifications. */
@Configuration
@Slf4j
public class FirebaseConfig {

  @Value("${firebase.service-account-path:firebase-service-account.json}")
  private String serviceAccountPath;

  // Filesystem path for Docker volume mount
  private static final String DOCKER_MOUNT_PATH = "/app/firebase-service-account.json";

  /**
   * Initialize Firebase and return FirebaseMessaging bean. This method combines initialization and
   * bean creation to avoid timing issues between @PostConstruct and @Bean.
   */
  @Bean
  @ConditionalOnMissingBean(FirebaseMessaging.class)
  public FirebaseMessaging firebaseMessaging() {
    try {
      // Initialize Firebase if not already done
      if (FirebaseApp.getApps().isEmpty()) {
        InputStream serviceAccount = getFirebaseConfigStream();

        if (serviceAccount == null) {
          log.warn(
              "Firebase service account file not found at {} or classpath. "
                  + "Push notifications will be disabled.",
              DOCKER_MOUNT_PATH);
          return null;
        }

        FirebaseOptions options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        log.info("Firebase application initialized successfully");
      } else {
        log.info("Firebase application already initialized");
      }

      return FirebaseMessaging.getInstance();
    } catch (IOException e) {
      log.warn(
          "Firebase config file not found or invalid. Push notifications will be disabled. "
              + "Error: {}",
          e.getMessage());
      return null;
    } catch (Exception e) {
      log.error("Failed to initialize Firebase: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Try to get Firebase config from multiple locations: 1. Docker volume mount path
   * (/app/firebase-service-account.json) 2. Classpath resource (inside JAR)
   */
  private InputStream getFirebaseConfigStream() {
    // Try Docker mount path first (for VPS deployment)
    File dockerMountFile = new File(DOCKER_MOUNT_PATH);
    if (dockerMountFile.exists() && dockerMountFile.canRead()) {
      try {
        log.info("Loading Firebase config from Docker mount: {}", DOCKER_MOUNT_PATH);
        return new FileInputStream(dockerMountFile);
      } catch (IOException e) {
        log.warn("Failed to read Docker mount file: {}", e.getMessage());
      }
    } else {
      log.info(
          "Firebase config not found at Docker mount path: {} (exists={}, canRead={})",
          DOCKER_MOUNT_PATH,
          dockerMountFile.exists(),
          dockerMountFile.canRead());
    }

    // Fall back to classpath (for local development / CI builds)
    try {
      ClassPathResource resource = new ClassPathResource(serviceAccountPath);
      if (resource.exists()) {
        log.info("Loading Firebase config from classpath: {}", serviceAccountPath);
        return resource.getInputStream();
      } else {
        log.info("Firebase config not found in classpath: {}", serviceAccountPath);
      }
    } catch (IOException e) {
      log.warn("Failed to read classpath resource: {}", e.getMessage());
    }

    return null;
  }
}
