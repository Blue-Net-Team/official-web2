## MODIFIED Requirements

### Requirement: Learning Path display

The system SHALL display a 4-step learning path with numbered steps and video links fetched from backend. The page SHALL use `export const revalidate` imported from `@/config/isr` (`ISR.direction`) instead of hardcoded `3600`.

#### Scenario: Desktop learning path
- **WHEN** user views learning path section on desktop
- **THEN** system displays 4 steps horizontally with arrow connectors

#### Scenario: Mobile learning path
- **WHEN** user views learning path section on mobile
- **THEN** system displays steps vertically in a column

#### Scenario: Learning path with video links
- **WHEN** backend returns video links for learning steps
- **THEN** system displays clickable video link for each step with video

#### Scenario: Learning path without video links
- **WHEN** backend returns null video links
- **THEN** system displays steps without video link elements

#### Scenario: Backend API failure fallback
- **WHEN** backend API is unavailable
- **THEN** system displays learning path using static data from data.ts

#### Scenario: Direction page uses unified ISR config
- **WHEN** direction page is built
- **THEN** revalidate value is read from ISR.direction instead of hardcoded 3600
