## ADDED Requirements

### Requirement: Comprehensive audit logging
The Audit entity SHALL record all system operations for security auditing and troubleshooting.

#### Scenario: Audit record creation
- **WHEN** any operation is performed
- **THEN** action field SHALL store the application layer function name
- **THEN** action_arg SHALL store parameters as JSON (max 4000 characters)
- **THEN** action_user_id MAY reference the User (null for unauthenticated)
- **THEN** action_time SHALL record the operation timestamp
- **THEN** ip_address SHALL store the client IP
- **THEN** user_agent SHALL store the client User-Agent
- **THEN** success_state SHALL indicate success or failure
- **THEN** remarks MAY contain additional notes

### Requirement: Audit data retention
The system SHALL retain audit logs without soft deletion.

#### Scenario: Audit table constraints
- **WHEN** creating the Audit table
- **THEN** it SHALL NOT have deleted field
- **THEN** it SHALL NOT have create_time or update_time fields
- **THEN** action_time SHALL serve as the primary temporal reference
