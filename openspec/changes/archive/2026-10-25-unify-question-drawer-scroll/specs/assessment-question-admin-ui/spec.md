## MODIFIED Requirements

### Requirement: Markdown question stem preview
The admin question drawer SHALL provide a candidate-facing statement preview in the right pane of the statement editing area while creating or editing a question, replacing the per-field preview of the question stem.

#### Scenario: Desktop preview layout
- **WHEN** an administrator edits a question on a desktop-size viewport
- **THEN** the drawer uses a wide responsive layout and displays the statement editing controls and the rendered statement preview side by side

#### Scenario: Drawer and panes scroll independently
- **WHEN** an administrator edits a question on a viewport too short to show the whole statement area
- **THEN** the drawer body scrolls vertically as the single scroll container while the preview pane keeps its own vertical scrollbar bounded by the viewport height, and the edit pane grows with its content instead of being squeezed into a short strip

#### Scenario: Markdown editors grow with their content
- **WHEN** an administrator writes a longer stem, statement description, or example explanation
- **THEN** that Markdown editor grows in height up to a per-field cap instead of staying fixed at a few visible lines

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

## ADDED Requirements

### Requirement: Single scroll container in the question drawer
The admin question drawer SHALL use its body as the only vertical scroll container for all statement editing tabs, and MUST NOT nest a second vertical scroll container inside a tab panel.

#### Scenario: Judge configuration tab scrolls with the drawer
- **WHEN** an administrator opens the judge configuration tab on a viewport too short for its content
- **THEN** the judge configuration fields scroll as part of the drawer body and no inner panel renders its own vertical scrollbar

#### Scenario: Language limit tab scrolls with the drawer
- **WHEN** an administrator opens the language limit tab on a viewport too short for its content
- **THEN** the language limit content scrolls as part of the drawer body and no inner panel renders its own vertical scrollbar

#### Scenario: Embedded editors are not clipped by a nested scroller
- **WHEN** a tab panel contains code editors or select dropdowns
- **THEN** those controls render at their configured heights and their popups are not clipped by an inner scroll container

#### Scenario: Tab navigation scrolls away with the content
- **WHEN** the drawer body is scrolled down inside any statement tab
- **THEN** the tab navigation bar scrolls up together with the metadata and MUST NOT stay pinned over the content above it

#### Scenario: Switching tabs resets the scroll position
- **WHEN** an administrator switches from one statement tab to another while the drawer body is scrolled
- **THEN** the drawer body scroll position returns to the top of the newly selected tab

#### Scenario: Drawer keeps a fixed header and footer
- **WHEN** the drawer body scrolls
- **THEN** the drawer title and the footer action buttons remain fixed and do not scroll out of view
