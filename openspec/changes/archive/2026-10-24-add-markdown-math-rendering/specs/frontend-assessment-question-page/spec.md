## MODIFIED Requirements

### Requirement: Algorithm question editor experience for candidates
The assessment question detail page SHALL render an algorithm question workspace with statement, examples, language selector, starter code, code editor, run controls, submit controls, and result panels. The input description, output description, and constraints sections SHALL render as Markdown with LaTeX math support.

#### Scenario: Candidate opens algorithm question with one template
- **WHEN** the algorithm question provides only a Python starter template
- **THEN** the page SHALL select Python and prevent choosing unconfigured languages

#### Scenario: Candidate opens algorithm question with multiple templates
- **WHEN** the algorithm question provides multiple starter templates
- **THEN** the page SHALL allow the candidate to choose one of the configured languages

#### Scenario: Statement sections render Markdown with math
- **WHEN** a candidate opens an algorithm question whose input description, output description, or constraints contain Markdown and LaTeX math
- **THEN** the page SHALL render those sections as formatted content with typeset formulas instead of literal Markdown and formula source text

#### Scenario: Statement sections stay inside their narrow columns
- **WHEN** a statement section containing a display formula is rendered in the three-column statement grid on a desktop viewport
- **THEN** the section SHALL use the compact Markdown layout and SHALL keep the formula inside the column bounds via horizontal scrolling rather than overflowing or breaking the grid

#### Scenario: Statement sections stack on narrow viewports
- **WHEN** a candidate opens an algorithm question on a viewport below the medium breakpoint
- **THEN** the statement sections SHALL stack vertically at full width and remain legible with the compact Markdown layout

#### Scenario: Empty statement sections are omitted
- **WHEN** an algorithm question leaves any of the input description, output description, or constraints empty
- **THEN** the page SHALL omit that section and SHALL still render the sections that have content

#### Scenario: Malformed formula in a statement section
- **WHEN** a statement section contains an invalid LaTeX expression
- **THEN** the page SHALL show the invalid expression highlighted as an error and continue rendering the remaining statement, examples, and answer controls

#### Scenario: Default run testcases are not rendered
- **WHEN** an algorithm question is configured with default run testcases
- **THEN** the candidate-facing statement SHALL NOT display those testcases and they SHALL only be executed when the candidate triggers a default run
