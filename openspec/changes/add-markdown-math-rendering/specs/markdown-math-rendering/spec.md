## ADDED Requirements

### Requirement: LaTeX math rendering in Markdown content
The frontend Markdown renderer SHALL render LaTeX math expressions inside Markdown content, supporting both inline `$...$` and block `$$...$$` delimiters.

#### Scenario: Inline math in statement text
- **WHEN** rendered Markdown content contains `$1 \le n \le 10^5$`
- **THEN** the renderer displays the expression as a typeset inline formula instead of the literal delimited text

#### Scenario: Block math in statement text
- **WHEN** rendered Markdown content contains a `$$...$$` block
- **THEN** the renderer displays the expression as a typeset display formula on its own line

#### Scenario: Markdown syntax inside math is preserved
- **WHEN** a formula contains characters that Markdown normally consumes, such as `*`, `\{`, `\}` or `\\`
- **THEN** those characters reach the formula renderer unchanged and are not converted into emphasis, escapes, or line breaks

#### Scenario: Content without math is unchanged
- **WHEN** rendered Markdown content contains no math delimiters
- **THEN** the rendered output is identical to the output before math support existed

#### Scenario: Math inside code is not rendered
- **WHEN** a math delimiter appears inside an inline code span or fenced code block
- **THEN** the renderer leaves it as literal code text and does not typeset it as a formula

### Requirement: Sanitization is preserved alongside math rendering
The frontend Markdown renderer SHALL continue to sanitize Markdown-derived HTML and MUST NOT execute raw HTML or scripts, while still allowing math to be typeset.

#### Scenario: Raw HTML is stripped from content with math
- **WHEN** rendered content contains both a formula and raw HTML such as `<script>` or an event-handler attribute
- **THEN** the formula renders as typeset math and the raw HTML is stripped without execution

#### Scenario: Formula output is not treated as untrusted HTML
- **WHEN** a formula is typeset
- **THEN** the renderer produces the formula markup from the formula source rather than passing formula output through the HTML sanitizer, so formula spacing and glyph classes remain intact

#### Scenario: Existing Markdown structures still render
- **WHEN** content contains a formula together with links, tables, lists, or fenced code blocks
- **THEN** all of those structures render normally in the same output as the formula

### Requirement: Math rendering errors are isolated
The frontend Markdown renderer SHALL isolate invalid math expressions so that a malformed formula does not prevent the rest of the content from rendering.

#### Scenario: Malformed formula
- **WHEN** content contains an invalid LaTeX expression such as an unclosed brace
- **THEN** the renderer displays that expression's source text highlighted as an error and continues rendering the remaining content

#### Scenario: Malformed formula in a candidate-facing statement
- **WHEN** a candidate opens a question whose statement contains an invalid LaTeX expression
- **THEN** the question page still renders the statement, examples, and answer controls instead of failing to render

### Requirement: Compact rendering variant for narrow containers
The frontend Markdown renderer SHALL provide a compact layout variant that tightens heading sizes and vertical spacing for rendering inside narrow containers, without changing text color or reducing readability.

#### Scenario: Narrow container uses compact variant
- **WHEN** Markdown content is rendered inside a narrow container such as one column of a three-column statement grid
- **THEN** the renderer uses tightened heading margins and spacing so the content stays inside the container bounds

#### Scenario: Compact variant keeps math legible
- **WHEN** a display formula is rendered with the compact variant in a narrow container
- **THEN** the formula remains fully typeset and the container provides horizontal scrolling instead of clipping or overflowing the layout

#### Scenario: Default variant is unaffected
- **WHEN** Markdown content is rendered without the compact variant
- **THEN** heading sizes and spacing match the existing full-width presentation
