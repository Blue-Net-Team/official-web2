## MODIFIED Requirements

### Requirement: Markdown question stem editing
The admin question drawer SHALL allow administrators to write Markdown content for every question type that has a stem content field, and additionally for the algorithm question statement fields `inputDescription`, `outputDescription`, and `constraints`, with all statement editing controls placed in the left pane of the statement editing area.

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

#### Scenario: Statement editing controls share one column
- **WHEN** an administrator edits an algorithm question on a viewport at or above the medium breakpoint
- **THEN** the stem, input description, output description, constraints, examples, default run testcases, and language template controls all appear in the left pane and no field renders its own nested edit-preview split

### Requirement: Markdown question stem preview
The admin question drawer SHALL provide a candidate-facing statement preview in the right pane of the statement editing area while creating or editing a question, replacing the per-field preview of the question stem.

#### Scenario: Desktop preview layout
- **WHEN** an administrator edits a question on a desktop-size viewport
- **THEN** the drawer uses a wide responsive layout and displays the statement editing controls and the rendered statement preview side by side

#### Scenario: Mobile preview layout
- **WHEN** an administrator edits a question on a mobile-size viewport
- **THEN** the drawer uses a full-screen layout and provides a single-column edit and preview switch

#### Scenario: Preview updates from current form value
- **WHEN** an administrator changes any statement text
- **THEN** the preview reflects the current unsaved form value without requiring the question to be saved

#### Scenario: Empty preview state
- **WHEN** every statement field is empty
- **THEN** the preview displays a clear empty state instead of rendering a blank or broken area

#### Scenario: Partial content preview
- **WHEN** only some statement fields have content
- **THEN** the preview renders only the sections that have content and omits the empty ones

#### Scenario: Preview shows candidate layout not field order
- **WHEN** the preview renders an algorithm question statement
- **THEN** it presents the sections in the candidate-facing three-column compact layout rather than as stacked full-width preview boxes

### Requirement: Markdown question stem read-only rendering
The admin question drawer SHALL render the candidate-facing statement preview as Markdown when displaying question details in read-only mode, without any editing controls.

#### Scenario: View existing Markdown stem
- **WHEN** an administrator opens a question in view mode
- **THEN** the drawer renders the statement preview as sanitized Markdown

#### Scenario: View existing plain text stem
- **WHEN** an administrator opens a question whose stem was previously stored as plain text
- **THEN** the drawer renders the stem legibly as Markdown-compatible text

#### Scenario: View algorithm statement fields in read-only mode
- **WHEN** an administrator opens an algorithm question in view mode
- **THEN** the drawer renders the statement preview with LaTeX math and does not display any statement editing controls or disabled input boxes

#### Scenario: Read-only formula overflow in the narrow view drawer
- **WHEN** a statement field contains a display formula wider than the view-mode drawer
- **THEN** the rendered formula stays inside its container and the horizontal overflow is scrollable instead of breaking the drawer layout
