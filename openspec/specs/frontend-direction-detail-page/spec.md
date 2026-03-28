# direction-detail-page Specification

## Purpose
TBD - created by archiving change integrate-direction-learning-path-api. Update Purpose after archive.
## Requirements
### Requirement: Learning Path display

The system SHALL display a 4-step learning path with numbered steps and video links fetched from backend.

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

---

### Requirement: Server-side data fetching

The system SHALL fetch learning path data on the server side using Next.js async server components.

#### Scenario: Server-side API call
- **WHEN** page component renders on server
- **THEN** system awaits learning path API response before rendering

#### Scenario: ISR caching
- **WHEN** page is cached
- **THEN** system revalidates cache every hour (3600 seconds)

#### Scenario: Cache revalidation
- **WHEN** cache expires
- **THEN** system fetches fresh data from backend on next request

