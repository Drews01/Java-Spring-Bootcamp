package com.example.demo.service;

public interface EmailService {
  void sendSimpleMessage(String to, String subject, String text);

  void sendLoanDisbursementEmail(String to, String userName, Long loanId, Double amount);

  void sendPasswordResetEmail(String to, String userName, String resetLink);

  void sendWelcomeEmail(String to, String userName, String loginLink);
}
