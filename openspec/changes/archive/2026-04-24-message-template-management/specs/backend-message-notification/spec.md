## ADDED Requirements

### Requirement: Unified verification code template component
The system SHALL replace individual verification code template classes with a single `EmailVerificationCodeTemplate` component that supports multiple scenes via a `VerificationCodeScene` enum.

#### Scenario: Template component registration
- **WHEN** the application context loads
- **THEN** the system SHALL register exactly one `EmailVerificationCodeTemplate` bean
- **AND** the previous `LoginVerificationCodeTemplate`, `ResetPasswordVerificationCodeTemplate`, and `ChangeEmailVerificationCodeTemplate` beans SHALL no longer exist

#### Scenario: Scene-based template rendering
- **WHEN** `EmailVerificationCodeTemplate.buildHtml(scene, code)` is called with any supported scene
- **THEN** the system SHALL return HTML with the scene-appropriate title, description, and footer
- **AND** the `{{code}}` placeholder SHALL be replaced with the provided verification code

### Requirement: Message template metadata registry
The system SHALL maintain a registry of all message templates with their metadata (code, name, subject, description, variables, enabled status) for admin management purposes.

#### Scenario: Template metadata enumeration
- **WHEN** the system enumerates all message templates
- **THEN** it SHALL include: LOGIN_VERIFY_CODE, RESET_PASSWORD_VERIFY_CODE, CHANGE_EMAIL_VERIFY_CODE, ENROLL_APPROVAL_CREDENTIAL, ENROLL_REJECTION, ASSESSMENT_DECISION_NOTIFICATION
- **AND** each entry SHALL expose its code, name, subject, description, variable list, and enabled status

#### Scenario: Template enabled status check
- **WHEN** a template is disabled via the admin API
- **THEN** subsequent attempts to use that template for sending emails SHALL throw a clear exception
