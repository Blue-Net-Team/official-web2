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
- **THEN** description SHALL explain the template purpose

### Requirement: Template runtime state persistence
The system SHALL persist template content overrides and subject overrides to the database. On application startup, the system SHALL load any existing overrides from the database and apply them over the code-defined default templates. When no database override exists for a template, the system SHALL fall back to the code-defined default content and subject.

#### Scenario: Content override is persisted and restored
- **WHEN** administrator updates template content via management API
- **THEN** the new content SHALL be written to the database
- **THEN** the new content SHALL be returned on subsequent reads
- **THEN** after application restart, the overridden content SHALL still be returned

#### Scenario: Subject override is persisted and restored
- **WHEN** administrator updates template subject via management API
- **THEN** the new subject SHALL be written to the database
- **THEN** the new subject SHALL be returned on subsequent reads
- **THEN** after application restart, the overridden subject SHALL still be returned

#### Scenario: Fallback to default when no database override
- **GIVEN** a template has code-defined default content and subject
- **WHEN** no database record exists for that template code
- **THEN** the system SHALL return the code-defined default content
- **THEN** the system SHALL return the code-defined default subject

#### Scenario: Template metadata remains immutable
- **GIVEN** a template is registered with code-defined metadata (name, description, variable list, default content)
- **WHEN** administrator edits template content or subject
- **THEN** the code-defined metadata (name, description, variable list) SHALL remain unchanged
