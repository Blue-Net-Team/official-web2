## ADDED Requirements

### Requirement: Scan controllers for permissions
The system MUST scan all controller methods for `@Permission` annotations at startup.

#### Scenario: Controller scanning
- **WHEN** the application finishes initializing
- **THEN** the permission scanner SHALL iterate over all controller mappings to discover permissions

### Requirement: Extract permission metadata
The system MUST extract permission value, name, URL, and HTTP method for each annotated endpoint.

#### Scenario: Permission metadata extraction
- **WHEN** an annotated endpoint is discovered
- **THEN** its permission value, name, URL path, and HTTP method SHALL be collected for synchronization

### Requirement: Batch synchronization with database
The system MUST synchronize permissions to the database in batch (insert/update/delete).

#### Scenario: Batch permission sync
- **WHEN** the permission scan completes
- **THEN** the system SHALL perform batch database operations to insert new permissions, update existing ones, and delete obsolete ones

### Requirement: Ghost permission deletion
The system MUST physically delete permissions that are no longer present in code.

#### Scenario: Delete obsolete permissions
- **WHEN** a permission exists in the database but is not found during the current scan
- **THEN** the system SHALL delete the permission record and its role-permission associations

### Requirement: Single URL constraint
The system MUST treat each endpoint as having a single URL mapping.

#### Scenario: URL selection
- **WHEN** a request mapping has multiple URL patterns
- **THEN** the scanner SHALL use the first URL pattern for permission mapping

### Requirement: Startup failure on invalid permissions
The system MUST fail startup if permission format validation fails.

#### Scenario: Invalid permission detected
- **WHEN** a permission value does not pass format validation
- **THEN** the scanner SHALL raise an exception and prevent application startup
