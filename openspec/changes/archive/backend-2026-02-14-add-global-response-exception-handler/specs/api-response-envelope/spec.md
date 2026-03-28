## ADDED Requirements

### Requirement: Unified response envelope
The system SHALL return a unified response envelope with fields `code` (Integer), `msg` (String), and `data` (generic payload) for all application API responses unless explicitly exempted.

#### Scenario: Successful response
- **WHEN** an API request is processed successfully
- **THEN** the response body SHALL include `code`, `msg`, and `data` fields

### Requirement: Default success semantics
The system SHALL use a consistent success code and default message when an operation succeeds.

#### Scenario: Default success message
- **WHEN** an API operation succeeds without a custom message
- **THEN** the response SHALL use the default success `code` and `msg`

### Requirement: Exception mapping
The system SHALL map handled exceptions to the unified response envelope with appropriate error `code` and `msg`.

#### Scenario: Business exception mapping
- **WHEN** a business exception is thrown
- **THEN** the response SHALL contain the mapped error `code` and `msg` with `data` set to null

#### Scenario: Validation error mapping
- **WHEN** request validation fails
- **THEN** the response SHALL contain the validation error `code` and a message describing the first validation error

#### Scenario: Unhandled exception mapping
- **WHEN** an unhandled exception occurs
- **THEN** the response SHALL contain the default system error `code` and a generic error `msg`

### Requirement: Exemption handling
The system SHALL allow specific endpoints or response types (e.g., file/stream responses) to bypass the response envelope.

#### Scenario: File download endpoint
- **WHEN** an endpoint is configured as a file/stream response
- **THEN** the response SHALL NOT be wrapped in the unified envelope
