## MODIFIED Requirements

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
- **THEN** the left pane SHALL grow with its content and scroll together with the drawer body, while the right pane SHALL stay pinned in view and keep its own vertical scroll bounded by the viewport height

#### Scenario: Right pane stays visible for a long statement
- **WHEN** the left pane content is taller than the viewport
- **THEN** the right pane SHALL remain within the viewport while the drawer body scrolls, so the preview does not scroll out of view
