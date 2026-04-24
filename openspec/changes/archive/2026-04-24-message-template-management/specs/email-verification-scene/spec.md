## ADDED Requirements

### Requirement: Unified email verification code template
The system SHALL provide a single `EmailVerificationCodeTemplate` component that renders verification code emails for all scenes using a configurable scene enum.

#### Scenario: Login verification code
- **WHEN** the system sends a login verification code
- **THEN** the email SHALL display the title "蓝网登录验证码", description "您的验证码为：", and footer "验证码5分钟内有效。"

#### Scenario: Reset password verification code
- **WHEN** the system sends a password reset verification code
- **THEN** the email SHALL display the title "蓝网密码重置验证码", description "您正在重置密码，验证码为：", and footer "验证码5分钟内有效，如非本人操作请忽略此邮件"

#### Scenario: Change email verification code for original email
- **WHEN** the system sends a change-email verification code for the original email
- **THEN** the email SHALL display the title "蓝网修改邮箱 - 验证原邮箱", description "您的验证码为：", and footer "验证码5分钟内有效，请勿泄露给他人。"

#### Scenario: Change email verification code for new email
- **WHEN** the system sends a change-email verification code for the new email
- **THEN** the email SHALL display the title "蓝网修改邮箱 - 验证新邮箱", description "您的验证码为：", and footer "验证码5分钟内有效，请勿泄露给他人。"

### Requirement: Verification code scene enum
The system SHALL define a `VerificationCodeScene` enum containing all supported verification code scenes with their display texts.

#### Scenario: Scene enum covers all existing use cases
- **WHEN** inspecting the `VerificationCodeScene` enum
- **THEN** it SHALL contain at least: LOGIN, RESET_PASSWORD, CHANGE_EMAIL_ORIGINAL, CHANGE_EMAIL_NEW
- **AND** each scene SHALL have title, description, and footer properties

### Requirement: Backward compatibility of verification code emails
The system SHALL ensure that all existing verification code email sending behavior remains unchanged after the refactoring.

#### Scenario: Existing AuthAppServiceImpl login code email
- **WHEN** `AuthAppServiceImpl` sends a login verification code
- **THEN** the rendered email HTML SHALL be semantically equivalent to the previous `LoginVerificationCodeTemplate` output

#### Scenario: Existing ResetPasswordAppServiceImpl reset code email
- **WHEN** `ResetPasswordAppServiceImpl` sends a password reset verification code
- **THEN** the rendered email HTML SHALL be semantically equivalent to the previous `ResetPasswordVerificationCodeTemplate` output

#### Scenario: Existing UserInfoAppServiceImpl change email code email
- **WHEN** `UserInfoAppServiceImpl` sends a change-email verification code
- **THEN** the rendered email HTML SHALL be semantically equivalent to the previous `ChangeEmailVerificationCodeTemplate` output
