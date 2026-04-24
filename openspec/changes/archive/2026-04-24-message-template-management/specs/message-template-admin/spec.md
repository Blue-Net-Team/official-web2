## ADDED Requirements

### Requirement: Admin can list message templates
The system SHALL provide a paginated list of all message templates with their code, name, subject, description, and enabled status.

#### Scenario: Admin queries template list
- **WHEN** an admin user calls the template list API
- **THEN** the system SHALL return a paginated list of all templates ordered by code

#### Scenario: Non-admin cannot access template list
- **WHEN** a non-admin user attempts to call the template list API
- **THEN** the system SHALL reject the request with a forbidden response

### Requirement: Admin can view template details
The system SHALL allow admins to view the full details of a specific template including its HTML content and available variables.

#### Scenario: Admin views existing template
- **WHEN** an admin user calls the template detail API with a valid template code
- **THEN** the system SHALL return the template's code, name, subject, content, description, enabled status, and a list of available variables with descriptions

#### Scenario: Admin views non-existent template
- **WHEN** an admin user calls the template detail API with an invalid template code
- **THEN** the system SHALL return a not found error

### Requirement: Admin can update template content
The system SHALL allow admins to edit a template's subject and HTML content.

#### Scenario: Admin updates template successfully
- **WHEN** an admin user calls the template update API with a valid template code, new subject, and new content
- **THEN** the system SHALL validate the content contains only supported variables
- **AND** the system SHALL persist the updated template
- **AND** subsequent email sends SHALL use the updated content

#### Scenario: Admin updates with invalid variables
- **WHEN** an admin user submits template content containing unsupported variables
- **THEN** the system SHALL reject the update with a validation error listing unsupported variables

### Requirement: Admin can toggle template enabled status
The system SHALL allow admins to enable or disable individual templates.

#### Scenario: Admin disables a template
- **WHEN** an admin user calls the toggle API to disable a template
- **THEN** the system SHALL mark the template as disabled
- **AND** subsequent attempts to send emails using this template SHALL fail with a clear error

#### Scenario: Admin enables a template
- **WHEN** an admin user calls the toggle API to enable a previously disabled template
- **THEN** the system SHALL mark the template as enabled
- **AND** emails using this template SHALL resume normal delivery

### Requirement: Admin can preview template rendering
The system SHALL provide a preview endpoint that renders a template with test variables without sending an email.

#### Scenario: Admin previews template with valid variables
- **WHEN** an admin user calls the preview API with a template code and a set of test variable values
- **THEN** the system SHALL substitute the variables into the template content
- **AND** return the rendered HTML string

#### Scenario: Admin previews template with missing variables
- **WHEN** an admin user calls the preview API with test variables missing some required placeholders
- **THEN** the system SHALL render the template with missing variables left as placeholders
- **AND** include a warning about the missing variables in the response
