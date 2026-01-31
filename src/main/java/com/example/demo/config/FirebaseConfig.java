package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
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

  @PostConstruct
  public void initialize() {
    try {
      if (FirebaseApp.getApps().isEmpty()) {
        InputStream serviceAccount = new ClassPathResource(serviceAccountPath).getInputStream();

        FirebaseOptions options =
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        log.info("Firebase application initialized successfully");
      } else {
        log.info("Firebase application already initialized");
      }
    } catch (IOException e) {
      log.warn(
          "Firebase config file not found. Push notifications will be disabled. Error: {}",
          e.getMessage());
      // Do not throw exception, allow app to start without Firebase
    } catch (Exception e) {
      log.error("Failed to initialize Firebase: {}", e.getMessage());
      // Keep running even if Firebase fails
    }
  }

  @Bean
  @ConditionalOnMissingBean(FirebaseMessaging.class)
  public FirebaseMessaging firebaseMessaging() {
    if (FirebaseApp.getApps().isEmpty()) {
      return null;
    }
    return FirebaseMessaging.getInstance();
  }
}
