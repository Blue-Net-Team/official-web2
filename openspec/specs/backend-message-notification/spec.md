## Purpose

Message notification system for managing email verification codes and configurable message templates.

## Requirements

### Requirement: Email verification code management
The VerifyCode entity SHALL manage email verification codes with expiration and usage tracking.

#### Scenario: Verification code lifecycle
- **WHEN** creating a VerifyCode
- **THEN** target field SHALL store the email address
- **THEN** code SHALL be a random verification string
- **THEN** expire_at SHALL be set to 5 minutes from creation
- **THEN** used_at SHALL be null until consumed
- **THEN** ip_address SHALL store request IP for rate limiting

### Requirement: Message template management
The MessageTemplate entity SHALL support configurable email templates with variable substitution.

#### Scenario: Template configuration
- **WHEN** creating a MessageTemplate
- **THEN** code SHALL be unique identifier (e.g., EMAIL_VERIFY_CODE, EVALUATION_RESULT)
- **THEN** subject SHALL define the email subject line
- **THEN** content SHALL support variables like {{username}}, {{code}}
- **THEN** enabled boolean controls template availability
- **THEN** description SHALL explain the template purpose
