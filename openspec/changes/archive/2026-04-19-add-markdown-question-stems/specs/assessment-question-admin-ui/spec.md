## ADDED Requirements

### Requirement: Markdown question stem editing
The admin question drawer SHALL allow administrators to write question stem content as Markdown for every question type that has a stem content field.

#### Scenario: Edit file upload question stem as Markdown
- **WHEN** an administrator creates or edits a file upload question
- **THEN** the stem field accepts Markdown content and saves it to the existing `content.content` string field

#### Scenario: Edit choice question stem as Markdown
- **WHEN** an administrator creates or edits a single choice or multiple choice question
- **THEN** the stem field accepts Markdown content and saves it to the existing `content.content` string field without changing option or answer fields

#### Scenario: Edit algorithm question stem as Markdown
- **WHEN** an administrator creates or edits an algorithm question
- **THEN** the stem field accepts Markdown content and saves it to the existing `content.content` string field without changing test case, time limit, or memory limit fields

### Requirement: Markdown question stem preview
The admin question drawer SHALL provide a Markdown preview for the current question stem while creating or editing a question.

#### Scenario: Desktop preview layout
- **WHEN** an administrator edits a question on a desktop-size viewport
- **THEN** the drawer uses a wide responsive layout and displays the Markdown input and rendered preview side by side

#### Scenario: Mobile preview layout
- **WHEN** an administrator edits a question on a mobile-size viewport
- **THEN** the drawer uses a full-screen layout and provides edit and preview tabs for the question stem

#### Scenario: Preview updates from current form value
- **WHEN** an administrator changes the Markdown stem text
- **THEN** the preview reflects the current unsaved form value without requiring the question to be saved

#### Scenario: Empty preview state
- **WHEN** the current stem field is empty
- **THEN** the preview displays a clear empty state instead of rendering a blank or broken area

### Requirement: Markdown question stem read-only rendering
The admin question drawer SHALL render question stems as Markdown when displaying question details in read-only mode.

#### Scenario: View existing Markdown stem
- **WHEN** an administrator opens a question in view mode
- **THEN** the drawer renders the question stem as sanitized Markdown

#### Scenario: View existing plain text stem
- **WHEN** an administrator opens a question whose stem was previously stored as plain text
- **THEN** the drawer renders the stem legibly as Markdown-compatible text

### Requirement: Safe Markdown rendering
The admin question drawer SHALL sanitize rendered Markdown and MUST NOT execute raw HTML or scripts from question stem content.

#### Scenario: Raw HTML in stem content
- **WHEN** a question stem contains raw HTML or script-like content
- **THEN** the admin preview and read-only display do not execute that HTML or script
