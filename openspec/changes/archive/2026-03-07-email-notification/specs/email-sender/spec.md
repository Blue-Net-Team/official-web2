## Purpose

Email sending capability that encapsulates Spring Mail for sending emails with template variable substitution support.

## ADDED Requirements

### Requirement: Email sending service
The system SHALL provide an EmailSender interface for sending emails.

#### Scenario: Send simple text email
- **WHEN** sending an email with recipient, subject, and text content
- **THEN** the system SHALL use JavaMailSender to deliver the email
- **THEN** the system SHALL log the sending result

#### Scenario: Send HTML email
- **WHEN** sending an email with HTML content
- **THEN** the system SHALL set content type to text/html
- **THEN** the system SHALL send the email with proper encoding

### Requirement: Asynchronous email sending
The system SHALL support asynchronous email sending to avoid blocking the main thread.

#### Scenario: Async send email
- **WHEN** calling the async email send method
- **THEN** the system SHALL return immediately without waiting for email delivery
- **THEN** the email SHALL be sent in a background thread

### Requirement: Email sending error handling
The system SHALL handle email sending failures gracefully.

#### Scenario: SMTP connection failure
- **WHEN** SMTP server is unreachable
- **THEN** the system SHALL log the error with details
- **THEN** the system SHALL throw EmailSendException with descriptive message

#### Scenario: Invalid recipient address
- **WHEN** recipient email address is null or empty
- **THEN** the system SHALL throw IllegalArgumentException before attempting to send

### Requirement: Template variable substitution
The system SHALL support variable substitution in email content.

#### Scenario: Single variable substitution
- **WHEN** content contains {{code}} and variables map contains {"code": "123456"}
- **THEN** the system SHALL replace {{code}} with "123456" in the final content

#### Scenario: Multiple variable substitution
- **WHEN** content contains {{username}} and {{code}}
- **THEN** the system SHALL replace all occurrences with corresponding variable values

#### Scenario: Missing variable handling
- **WHEN** a template variable has no corresponding value in the variables map
- **THEN** the system SHALL leave the placeholder unchanged
- **THEN** the system SHALL log a warning about the missing variable
