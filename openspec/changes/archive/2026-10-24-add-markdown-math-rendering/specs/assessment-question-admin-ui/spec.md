## MODIFIED Requirements

### Requirement: Markdown question stem editing
The admin question drawer SHALL allow administrators to write Markdown content for every question type that has a stem content field, and additionally for the algorithm question statement fields `inputDescription`, `outputDescription`, and `constraints`.

#### Scenario: Edit file upload question stem as Markdown
- **WHEN** an administrator creates or edits a file upload question
- **THEN** the stem field accepts Markdown content and saves it to the existing `content.content` string field

#### Scenario: Edit choice question stem as Markdown
- **WHEN** an administrator creates or edits a single choice or multiple choice question
- **THEN** the stem field accepts Markdown content and saves it to the existing `content.content` string field without changing option or answer fields

#### Scenario: Edit algorithm question stem as Markdown
- **WHEN** an administrator creates or edits an algorithm question
- **THEN** the stem field accepts Markdown content and saves it to the existing `content.content` string field without changing test case, time limit, or memory limit fields

#### Scenario: Edit algorithm statement fields as Markdown
- **WHEN** an administrator creates or edits an algorithm question
- **THEN** the input description, output description, and constraints fields accept Markdown content including LaTeX math, and save the raw Markdown source to the existing `content.inputDescription`, `content.outputDescription`, and `content.constraints` string fields without changing the request contract

#### Scenario: Statement field editor height stays aligned with the replaced text area
- **WHEN** an administrator edits an algorithm question on a desktop-size viewport
- **THEN** each of the three statement field editors SHALL be within one text line of the height of the three-row text area it replaces, so the drawer content height grows by at most a few dozen pixels
- **NOTE** 实测：题干编辑器 96px vs 被替换的 3 行文本域 75.6px，单字段差 20.4px（小于 22px 行高），三个字段合计 +61px

### Requirement: Markdown question stem preview
The admin question drawer SHALL provide a Markdown preview for the current question stem while creating or editing a question, and SHALL provide live preview for the algorithm question statement fields.

#### Scenario: Desktop preview layout
- **WHEN** an administrator edits a question on a desktop-size viewport
- **THEN** the drawer uses a wide responsive layout and displays the Markdown input and rendered preview side by side

#### Scenario: Mobile preview layout
- **WHEN** an administrator edits a question on a mobile-size viewport
- **THEN** the drawer uses a full-screen layout and provides edit and preview tabs for the Markdown fields

#### Scenario: Preview updates from current form value
- **WHEN** an administrator changes the Markdown stem text
- **THEN** the preview reflects the current unsaved form value without requiring the question to be saved

#### Scenario: Empty preview state
- **WHEN** the current stem field is empty
- **THEN** the preview displays a clear empty state instead of rendering a blank or broken area

#### Scenario: Statement field preview updates from current form value
- **WHEN** an administrator changes any of the three algorithm statement fields
- **THEN** that field's preview reflects the current unsaved form value, including typeset LaTeX math

#### Scenario: Statement field preview empty state
- **WHEN** one of the three algorithm statement fields is empty
- **THEN** only that field's preview shows an empty state and the other statement fields are unaffected

### Requirement: Markdown question stem read-only rendering
The admin question drawer SHALL render question stems and algorithm statement fields as Markdown when displaying question details in read-only mode.

#### Scenario: View existing Markdown stem
- **WHEN** an administrator opens a question in view mode
- **THEN** the drawer renders the question stem as sanitized Markdown

#### Scenario: View existing plain text stem
- **WHEN** an administrator opens a question whose stem was previously stored as plain text
- **THEN** the drawer renders the stem legibly as Markdown-compatible text

#### Scenario: View algorithm statement fields in read-only mode
- **WHEN** an administrator opens an algorithm question in view mode
- **THEN** the three statement fields render as sanitized Markdown with LaTeX math and do not appear as disabled input boxes

#### Scenario: Read-only formula overflow in the narrow view drawer
- **WHEN** a statement field contains a display formula wider than the view-mode drawer
- **THEN** the rendered formula stays inside its container and the horizontal overflow is scrollable instead of breaking the drawer layout

### Requirement: Safe Markdown rendering
The admin question drawer SHALL sanitize rendered Markdown and MUST NOT execute raw HTML or scripts from question stem or statement field content.

#### Scenario: Raw HTML in stem content
- **WHEN** a question stem contains raw HTML or script-like content
- **THEN** the admin preview and read-only display do not execute that HTML or script

#### Scenario: Raw HTML in statement fields
- **WHEN** a statement field contains raw HTML or script-like content
- **THEN** the admin preview and read-only display do not execute that HTML or script while still rendering any LaTeX math in the same content

#### Scenario: Malformed formula in statement field
- **WHEN** a statement field contains invalid LaTeX
- **THEN** the admin preview shows the invalid expression highlighted as an error and the remaining content still renders
