# question-statement-preview Specification

## Purpose
TBD - created by archiving change refactor-question-statement-preview. Update Purpose after archive.
## Requirements
### Requirement: Admin statement preview layout
The admin question drawer SHALL render the algorithm statement editing area as a two-pane layout: a left pane containing all editing controls and a right pane containing a continuous preview of the candidate-facing statement.

#### Scenario: Desktop two-pane layout
- **WHEN** an administrator edits a question on a viewport at or above the medium breakpoint
- **THEN** the drawer SHALL display the editing controls in the left pane and the statement preview in the right pane, both visible at the same time

#### Scenario: Left pane contains all editing controls
- **WHEN** an administrator edits an algorithm question
- **THEN** the left pane SHALL contain the stem, input description, output description, constraints, examples, default run testcases, and language template controls in that order

#### Scenario: Right pane is a continuous preview
- **WHEN** the right pane renders the preview
- **THEN** it SHALL render the stem, the statement sections, and the examples as one continuous document rather than as separate per-field preview boxes

#### Scenario: Panes scroll independently
- **WHEN** the combined height of the left pane controls exceeds the available height
- **THEN** the left pane SHALL scroll independently while the right pane keeps its own scroll position, and the drawer body itself SHALL not scroll

### Requirement: Preview mirrors the candidate-facing statement
The right pane preview SHALL reuse the same statement rendering components as the candidate-facing question page so that the preview and the candidate page cannot diverge.

#### Scenario: Preview matches candidate statement sections
- **WHEN** an algorithm question has a stem, input description, output description, constraints, and examples
- **THEN** the preview SHALL show the stem card followed by the three-column statement sections followed by the example blocks, in the same order and with the same narrow-column compact layout the candidate sees

#### Scenario: Preview matches candidate choice options
- **WHEN** an administrator edits a single choice or multiple choice question
- **THEN** the preview SHALL show the stem card followed by the option list rendered the same way the candidate sees it

#### Scenario: Preview shows options in the unselected presentation
- **WHEN** the preview renders the option list
- **THEN** every option SHALL appear in its unselected presentation, and no option SHALL appear interactive, selected, or highlighted

#### Scenario: Preview matches candidate file upload statement
- **WHEN** an administrator edits a file upload question
- **THEN** the preview SHALL show only the stem card, matching the candidate-facing statement for that question type

#### Scenario: Preview updates from unsaved form values
- **WHEN** an administrator changes any statement field
- **THEN** the preview SHALL reflect the current unsaved form value without requiring the question to be saved

### Requirement: Preview excludes candidate-invisible content
The right pane preview SHALL render only content the candidate can see and MUST NOT render answers, default run testcases, language templates, formal testcases, or judge configuration.

#### Scenario: Default run testcases stay out of the preview
- **WHEN** an administrator edits the default run testcases in the left pane
- **THEN** the preview SHALL NOT display any part of those testcases

#### Scenario: Correct answers stay out of the preview
- **WHEN** an administrator edits a choice question's correct answer
- **THEN** the preview SHALL NOT display or highlight the correct answer

#### Scenario: Language templates stay out of the preview
- **WHEN** an administrator edits the language templates in the left pane
- **THEN** the preview SHALL NOT display starter code or the language selector

### Requirement: Preview rendering is deferred
The right pane preview SHALL defer its rendering so that typing in the left pane stays responsive when the statement contains many formulas.

#### Scenario: Typing stays responsive with many formulas
- **WHEN** an administrator types continuously in a statement field of a question whose statement contains many formulas
- **THEN** the left pane input SHALL update on every keystroke and the preview SHALL catch up without blocking input

#### Scenario: Preview converges on the latest value
- **WHEN** an administrator stops typing
- **THEN** the preview SHALL settle on the latest form value without requiring any manual refresh action

### Requirement: Preview degrades for narrow viewports and read-only mode
The statement preview SHALL degrade to a single column when the available width is insufficient, and read-only mode SHALL render only the preview.

#### Scenario: Narrow editing viewport
- **WHEN** an administrator edits a question on a viewport below the medium breakpoint
- **THEN** the drawer SHALL present a single column with an edit and preview switch instead of two side-by-side panes

#### Scenario: Read-only detail view
- **WHEN** an administrator opens a question in view mode
- **THEN** the drawer SHALL render only the candidate-facing statement preview without any editing controls

#### Scenario: Wide formula in a narrow container
- **WHEN** the preview contains a display formula wider than its container
- **THEN** the formula SHALL stay inside its container and the overflow SHALL be horizontally scrollable instead of breaking the layout

