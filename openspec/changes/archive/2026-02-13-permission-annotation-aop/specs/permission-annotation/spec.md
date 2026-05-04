## ADDED Requirements

### Requirement: Permission annotation definition
The system MUST provide a `@Permission` annotation with attributes `value`, `name`, and `access` to declare endpoint permissions.

#### Scenario: Method-level permission annotation
- **WHEN** a controller method is annotated with `@Permission`
- **THEN** the permission metadata SHALL be associated with that endpoint for scanning and enforcement

### Requirement: Permission value format validation
The system MUST validate `@Permission.value` against the pattern `^[a-z]+:[a-z]+$` during application startup.

#### Scenario: Invalid permission value
- **WHEN** a permission value does not match `resource:action` with lowercase letters
- **THEN** the application SHALL fail to start and report the invalid permission value

### Requirement: Access level semantics
The system MUST support three access levels: PUBLIC, AUTHENTICATED, and PROTECTED.

#### Scenario: Access level enumeration
- **WHEN** `@Permission.access` is omitted
- **THEN** the access level SHALL default to PROTECTED

### Requirement: Annotation precedence
The system MUST prefer method-level `@Permission` over class-level `@Permission`.

#### Scenario: Method-level override
- **WHEN** both class and method are annotated with `@Permission`
- **THEN** the method-level `value` and `access` SHALL be applied

### Requirement: Annotation usage on controllers
The system MUST allow `@Permission` on controller classes and methods.

#### Scenario: Class-level permission definition
- **WHEN** a controller class is annotated with `@Permission`
- **THEN** its methods SHALL inherit the class-level permission unless overridden
