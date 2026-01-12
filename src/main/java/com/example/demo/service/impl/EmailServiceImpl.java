package com.example.demo.service.impl;

import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender emailSender;

  @Override
  public void sendSimpleMessage(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    emailSender.send(message);
  }

  @Override
  public void sendLoanDisbursementEmail(String to, String userName, Long loanId, Double amount) {
    try {
      jakarta.mail.internet.MimeMessage message = emailSender.createMimeMessage();
      org.springframework.mail.javamail.MimeMessageHelper helper =
          new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(to);
      helper.setSubject("Your Loan Has Been Disbursed - Loan #" + loanId);

      String htmlContent =
          String.format(
              """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta charset="UTF-8">
                                <style>
                                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                                    .content { background-color: #f9f9f9; padding: 30px; border: 1px solid #ddd; }
                                    .loan-details { background-color: white; padding: 20px; margin: 20px 0; border-radius: 5px; border-left: 4px solid #4CAF50; }
                                    .loan-details h3 { margin-top: 0; color: #4CAF50; }
                                    .detail-row { margin: 10px 0; }
                                    .label { font-weight: bold; color: #555; }
                                    .value { color: #333; }
                                    .footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 5px 5px; }
                                    .button { display: inline-block; padding: 12px 30px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                                    .important { background-color: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 5px; }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="header">
                                        <h1>🎉 Loan Disbursed Successfully!</h1>
                                    </div>
                                    <div class="content">
                                        <p>Dear <strong>%s</strong>,</p>

                                        <p>Great news! Your loan application has been <strong>successfully disbursed</strong>. The funds should be available in your account shortly.</p>

                                        <div class="loan-details">
                                            <h3>Loan Details</h3>
                                            <div class="detail-row">
                                                <span class="label">Loan ID:</span>
                                                <span class="value">#%d</span>
                                            </div>
                                            <div class="detail-row">
                                                <span class="label">Disbursed Amount:</span>
                                                <span class="value">Rp %.2f</span>
                                            </div>
                                            <div class="detail-row">
                                                <span class="label">Status:</span>
                                                <span class="value" style="color: #4CAF50; font-weight: bold;">DISBURSED</span>
                                            </div>
                                        </div>

                                        <div class="important">
                                            <strong>📌 What's Next?</strong>
                                            <ul>
                                                <li>Check your bank account for the disbursed funds</li>
                                                <li>Review your repayment schedule</li>
                                                <li>Set up auto-debit for timely payments (recommended)</li>
                                                <li>Contact us if you have any questions</li>
                                            </ul>
                                        </div>

                                        <p style="margin-top: 30px;">You can view your complete loan details and repayment schedule by logging into your account.</p>

                                        <p>If you have any questions or concerns, please don't hesitate to contact our support team.</p>

                                        <p>Thank you for choosing our services!</p>

                                        <p style="margin-top: 30px;">
                                            Best regards,<br>
                                            <strong>Loan Management Team</strong>
                                        </p>
                                    </div>
                                    <div class="footer">
                                        <p>This is an automated message. Please do not reply to this email.</p>
                                        <p>&copy; 2026 Loan Management System. All rights reserved.</p>
                                    </div>
                                </div>
                            </body>
                            </html>
                            """,
              userName, loanId, amount);

      helper.setText(htmlContent, true);
      emailSender.send(message);
    } catch (jakarta.mail.MessagingException e) {
      throw new RuntimeException("Failed to send loan disbursement email", e);
    }
  }

  @Override
  public void sendPasswordResetEmail(String to, String userName, String resetLink) {
    try {
      jakarta.mail.internet.MimeMessage message = emailSender.createMimeMessage();
      org.springframework.mail.javamail.MimeMessageHelper helper =
          new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(to);
      helper.setSubject("Password Reset Request");

      String htmlContent =
          String.format(
              """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta charset="UTF-8">
                                <style>
                                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                    .header { background-color: #3b82f6; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                                    .content { background-color: #f9f9f9; padding: 30px; border: 1px solid #ddd; }
                                    .footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 5px 5px; }
                                    .button { display: inline-block; padding: 14px 30px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; font-weight: bold; }
                                    .button:hover { background-color: #2563eb; }
                                    .warning { background-color: #fef3c7; padding: 15px; border-left: 4px solid #f59e0b; margin: 20px 0; border-radius: 5px; }
                                </style>
                            </head>
                            <body>
                                <div class="container">
                                    <div class="header">
                                        <h1>🔐 Password Reset Request</h1>
                                    </div>
                                    <div class="content">
                                        <p>Dear <strong>%s</strong>,</p>

                                        <p>We received a request to reset your password. If you did not make this request, you can safely ignore this email.</p>

                                        <p>To reset your password, click the button below:</p>

                                        <div style="text-align: center;">
                                            <a href="%s" class="button">Reset Password</a>
                                        </div>

                                        <div class="warning">
                                            <strong>⚠️ Important:</strong>
                                            <ul>
                                                <li>This link will expire in 1 hour</li>
                                                <li>If you did not request a password reset, please ignore this email</li>
                                                <li>Never share this link with anyone</li>
                                            </ul>
                                        </div>

                                        <p>If the button above doesn't work, copy and paste the following link into your browser:</p>
                                        <p style="word-break: break-all; font-size: 12px; color: #666;">%s</p>

                                        <p style="margin-top: 30px;">
                                            Best regards,<br>
                                            <strong>Loan Management Team</strong>
                                        </p>
                                    </div>
                                    <div class="footer">
                                        <p>This is an automated message. Please do not reply to this email.</p>
                                        <p>&copy; 2026 Loan Management System. All rights reserved.</p>
                                    </div>
                                </div>
                            </body>
                            </html>
                            """,
              userName, resetLink, resetLink);

      helper.setText(htmlContent, true);
      emailSender.send(message);
    } catch (jakarta.mail.MessagingException e) {
      throw new RuntimeException("Failed to send password reset email", e);
    }
  }
}
