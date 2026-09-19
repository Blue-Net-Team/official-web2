## MODIFIED Requirements

### Requirement: Learning Path display

The system SHALL display the learning path with numbered steps and related links, with step data (title, relatedLink) fully driven by the backend API. The frontend MUST NOT hardcode learning steps; `data.ts` SHALL NOT contain learning path step data. The displayed step number SHALL be derived by the frontend from the step's position in the returned array (`index + 1`), and MUST NOT depend on any backend-provided number field. The array order returned by the backend SHALL be treated as the authoritative display order.

#### Scenario: Desktop learning path
- **WHEN** user views learning path section on desktop
- **THEN** system displays steps horizontally with arrow connectors, numbering each step from its position in the array and using title from backend

#### Scenario: Mobile learning path
- **WHEN** user views learning path section on mobile
- **THEN** system displays steps vertically in a column

#### Scenario: Step number derived from position
- **WHEN** the learning path section renders the returned steps
- **THEN** each card shows a two-digit number equal to its array index plus one (`01`, `02`, `03`…), with no backend number field involved

#### Scenario: Display numbering is always contiguous
- **WHEN** the backend returns steps whose stored order values have gaps (for example after a deletion)
- **THEN** the rendered numbers remain contiguous `01`, `02`, `03`… because they derive from array position

#### Scenario: Learning path with related links
- **WHEN** backend returns related links for learning steps
- **THEN** system displays clickable link entry for each step with a related link, with copy indicating "查看相关资料"

#### Scenario: Learning path without related links
- **WHEN** backend returns null related links
- **THEN** system displays steps without link elements

#### Scenario: Backend API failure renders empty section
- **WHEN** backend API is unavailable or returns no steps
- **THEN** system renders the learning path section with only its heading and no step cards, while the rest of the page renders normally
