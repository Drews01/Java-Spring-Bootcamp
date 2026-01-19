# Loan Disbursement Email Notification

This document describes the email notification feature that automatically sends an email to users when their loan is disbursed.

## Overview

When a loan application is approved and disbursed by the Back Office team, the system automatically sends:
1. **In-App Notification** - Stored in the database for the user to view
2. **Email Notification** - Professional HTML email sent to the user's registered email address

## Features

| Feature | Description |
|---------|-------------|
| HTML Template | Professional, responsive email design |
| Auto-Trigger | Sends when loan status changes to `DISBURSED` |
| Error Resilient | Email failures don't block the workflow |
| Dual Notification | Both in-app and email notifications sent |

## Configuration

### SMTP Settings

Add the following to `application.yml`:

```yaml
spring:
  mail:
    host: sandbox.smtp.mailtrap.io    # Your SMTP host
    port: 2525                         # SMTP port
    username: your-username            # SMTP username
    password: your-password            # SMTP password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### Using Mailtrap (Development)

1. Create an account at [mailtrap.io](https://mailtrap.io)
2. Get your SMTP credentials from the inbox settings
3. Update `application.yml` with the credentials
4. All emails will appear in your Mailtrap inbox

## Implementation

### Files Modified

| File | Purpose |
|------|---------|
| `EmailService.java` | Interface with `sendLoanDisbursementEmail()` method |
| `EmailServiceImpl.java` | Implementation with HTML email template |
| `LoanWorkflowService.java` | Integration point, triggers email on DISBURSED |

### Email Service Interface

```java
public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
    void sendLoanDisbursementEmail(String to, String userName, Long loanId, Double amount);
}
```

### Workflow Integration

The email is sent from `LoanWorkflowService.sendNotifications()`:

```java
// APPROVED_WAITING_DISBURSEMENT -> DISBURSED: Notify customer
if ("APPROVED_WAITING_DISBURSEMENT".equals(fromStatus) && "DISBURSED".equals(toStatus)) {
    // Create in-app notification
    notificationService.createNotification(...);
    
    // Send email notification
    try {
        emailService.sendLoanDisbursementEmail(
            user.getEmail(),
            user.getUsername(),
            loanApplication.getLoanApplicationId(),
            loanApplication.getAmount());
    } catch (Exception emailError) {
        // Log error but don't fail the workflow
        log.error("Failed to send disbursement email: {}", emailError.getMessage());
    }
}
```

## Email Content

### Subject
```
Your Loan Has Been Disbursed - Loan #[ID]
```

### Body Content
The HTML email includes:
- User's name (personalized greeting)
- Loan ID for reference
- Disbursed amount
- Confirmation message
- Next steps instructions
- Professional styling with gradient header

## Error Handling

The implementation uses a **fail-safe** approach:

| Scenario | Behavior |
|----------|----------|
| SMTP unavailable | Error logged, workflow continues |
| Invalid email address | Error logged, workflow continues |
| Network timeout | Error logged, workflow continues |
| Email sent successfully | Success logged |

**Key Point**: Email failures never prevent loan disbursement from proceeding.

## Testing

### Step 1: Configure SMTP
Ensure `application.yml` has valid SMTP settings (use Mailtrap for development).

### Step 2: Run Full Loan Workflow

```http
# 1. Submit a loan (as customer)
POST /api/loan-workflow/submit
Authorization: Bearer <customer_token>
Content-Type: application/json

{
  "productId": 1,
  "amount": 5000000,
  "tenureMonths": 12
}

# 2. Add comment (as marketing)
POST /api/loan-workflow/action
Authorization: Bearer <marketing_token>
Content-Type: application/json

{
  "loanApplicationId": 1,
  "action": "COMMENT",
  "comment": "Documents verified"
}

# 3. Forward to manager (as marketing)
POST /api/loan-workflow/action
Authorization: Bearer <marketing_token>
Content-Type: application/json

{
  "loanApplicationId": 1,
  "action": "FORWARD_TO_MANAGER"
}

# 4. Approve loan (as branch_manager)
POST /api/loan-workflow/action
Authorization: Bearer <manager_token>
Content-Type: application/json

{
  "loanApplicationId": 1,
  "action": "APPROVE"
}

# 5. Disburse loan (as back_office) - THIS TRIGGERS THE EMAIL
POST /api/loan-workflow/action
Authorization: Bearer <backoffice_token>
Content-Type: application/json

{
  "loanApplicationId": 1,
  "action": "DISBURSE"
}
```

### Step 3: Verify Email
Check your Mailtrap inbox for the disbursement email.

### Expected Log Output

**Success:**
```
INFO: Disbursement email sent to user@example.com for loan 1
```

**Failure (graceful):**
```
ERROR: Failed to send disbursement email for loan 1: Connection refused
```

## Notification Comparison

| Type | Guaranteed | Storage | User Action |
|------|-----------|---------|-------------|
| **In-App** | ✅ Yes | Database | View via API |
| **Email** | ⚠️ Best effort | Not stored | Check inbox |

## Troubleshooting

| Issue | Solution |
|-------|----------|
| No email received | Check SMTP configuration in `application.yml` |
| Authentication error | Verify SMTP username/password |
| Connection refused | Check host/port, ensure service is reachable |
| Email in spam | Check sender address, add to safe senders |

## Related Documentation

- [Forgot Password](forgot_password.md) - Email configuration reference
- [Loan Workflow](LOAN_WORKFLOW_DOCUMENTATION.md) - Full workflow documentation
- [Tier System](TIER_IMPLEMENTATION.md) - Credit limit and tier upgrades
