package com.example.demo.config;

import static org.mockito.Mockito.mock;

import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

@TestConfiguration
public class TestConfig {

  @Bean
  @Primary
  public FirebaseMessaging firebaseMessaging() {
    return mock(FirebaseMessaging.class);
  }

  @Bean
  @Primary
  public JavaMailSender javaMailSender() {
    return mock(JavaMailSender.class);
  }
}
