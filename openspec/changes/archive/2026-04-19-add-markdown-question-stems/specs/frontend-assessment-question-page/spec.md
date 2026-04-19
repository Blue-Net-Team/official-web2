## ADDED Requirements

### Requirement: Markdown question stem rendering
The assessment question detail page SHALL render question stem content as sanitized Markdown.

#### Scenario: Render file upload question stem as Markdown
- **WHEN** a candidate opens a file upload question detail page
- **THEN** the page renders `content.content` as sanitized Markdown in the question description area

#### Scenario: Preserve Markdown structure
- **WHEN** the question stem contains Markdown headings, lists, links, tables, inline code, or fenced code blocks
- **THEN** the page renders those structures as formatted content instead of showing raw Markdown syntax where supported by the renderer

#### Scenario: Existing plain text stem remains readable
- **WHEN** the question stem contains existing plain text without Markdown syntax
- **THEN** the page displays the text legibly in the question description area

### Requirement: Do not split Markdown stem into artificial sections
The assessment question detail page SHALL treat the question stem as one Markdown document and MUST NOT split it into a first-line description and generated numbered requirement rows.

#### Scenario: Markdown list content
- **WHEN** the question stem contains a Markdown list
- **THEN** the page renders the list from the Markdown document instead of converting lines into separate custom requirement rows

#### Scenario: Markdown code block content
- **WHEN** the question stem contains a fenced code block with newlines
- **THEN** the page preserves the code block structure during rendering

### Requirement: Safe candidate-facing Markdown rendering
The assessment question detail page SHALL sanitize rendered Markdown and MUST NOT execute raw HTML or scripts from question stem content.

#### Scenario: Unsafe HTML in stem content
- **WHEN** a question stem contains raw HTML or script-like content
- **THEN** the candidate-facing page does not execute that HTML or script
