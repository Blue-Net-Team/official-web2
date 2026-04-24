## ADDED Requirements

### Requirement: Template runtime state persistence
The system SHALL persist template content overrides, subject overrides, and enabled status to the database. On application startup, the system SHALL load any existing overrides from the database and apply them over the code-defined default templates. When no database override exists for a template, the system SHALL fall back to the code-defined default content and subject.

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
- **THEN** the system SHALL report the template as enabled

#### Scenario: Template metadata remains immutable
- **GIVEN** a template is registered with code-defined metadata (name, description, variable list, default content)
- **WHEN** administrator edits template content or subject
- **THEN** the code-defined metadata (name, description, variable list) SHALL remain unchanged
