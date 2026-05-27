# frontend-monaco-code-editor Specification

## Purpose
TBD - created by archiving change add-frontend-code-editor. Update Purpose after archive.
## Requirements
### Requirement: CodeEditor component provides syntax highlighting for supported languages
The system SHALL provide a reusable CodeEditor React component that supports syntax highlighting for Python, C, C++, Java, and JavaScript.

#### Scenario: Rendering Python code
- **WHEN** the CodeEditor component is rendered with language="python"
- **THEN** the editor SHALL display Python syntax highlighting

#### Scenario: Rendering C++ code
- **WHEN** the CodeEditor component is rendered with language="cpp"
- **THEN** the editor SHALL display C++ syntax highlighting

#### Scenario: Rendering Java code
- **WHEN** the CodeEditor component is rendered with language="java"
- **THEN** the editor SHALL display Java syntax highlighting

### Requirement: CodeEditor component displays line numbers and bracket matching
The system SHALL display line numbers and highlight matching brackets in the CodeEditor component.

#### Scenario: Viewing line numbers
- **WHEN** the CodeEditor component is rendered
- **THEN** line numbers SHALL be visible on the left side of the editor

#### Scenario: Matching brackets
- **WHEN** the user places cursor next to a bracket
- **THEN** the matching opening/closing bracket SHALL be highlighted

### Requirement: Algorithm question code writing area uses CodeEditor
The system SHALL replace the native textarea in the candidate's algorithm question page with the CodeEditor component.

#### Scenario: Candidate writes algorithm code
- **WHEN** a candidate opens an algorithm question
- **THEN** the code input area SHALL be the CodeEditor with syntax highlighting
- **AND** the language SHALL match the selected algorithm language

#### Scenario: Code change propagation
- **WHEN** the candidate types code in the CodeEditor
- **THEN** the code value SHALL be propagated to the parent component via onChange

### Requirement: Admin question editor uses CodeEditor for generator source
The system SHALL replace the Input.TextArea for generator source code in the admin question editor with the CodeEditor component.

#### Scenario: Admin edits generator source
- **WHEN** an admin opens the question editor and the question type is algorithm
- **THEN** the generator source input SHALL be the CodeEditor with syntax highlighting
- **AND** the language SHALL match the selected generator language

### Requirement: Admin question editor uses CodeEditor for standard solution source
The system SHALL replace the Input.TextArea for standard solution source code in the admin question editor with the CodeEditor component.

#### Scenario: Admin edits standard solution
- **WHEN** an admin edits standard solutions in the question editor
- **THEN** each standard solution source input SHALL be the CodeEditor with syntax highlighting
- **AND** the language SHALL match the solution's language

### Requirement: Admin question editor uses CodeEditor for starter code templates
The system SHALL replace the Input.TextArea for starter code templates in the admin question editor with the CodeEditor component.

#### Scenario: Admin edits starter template
- **WHEN** an admin edits starter code templates in the question editor
- **THEN** each starter code template input SHALL be the CodeEditor with syntax highlighting
- **AND** the language SHALL match the template's language

### Requirement: CodeEditor supports read-only mode
The system SHALL support a read-only mode in the CodeEditor component for view-only scenarios.

#### Scenario: View mode in admin drawer
- **WHEN** the admin question drawer is in view mode
- **THEN** all CodeEditor instances SHALL be read-only and non-editable

### Requirement: CodeEditor is compatible with Next.js SSR
The system SHALL ensure the CodeEditor component works correctly in Next.js 15 with SSR without causing hydration errors.

#### Scenario: Server-side rendering
- **WHEN** the page is server-side rendered
- **THEN** the CodeEditor SHALL not cause hydration mismatches
- **AND** the editor SHALL initialize correctly on the client side

