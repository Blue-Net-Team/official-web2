## ADDED Requirements

### Requirement: Direction page routing

The system SHALL provide dynamic routing for direction detail pages at `/direction/[slug]` where slug is one of: `cv`, `embed`, `struct`.

#### Scenario: Valid direction slug - cv
- **WHEN** user navigates to `/direction/cv`
- **THEN** system renders the computer vision direction detail page with purple theme

#### Scenario: Valid direction slug - embed
- **WHEN** user navigates to `/direction/embed`
- **THEN** system renders the embedded development direction detail page with green theme

#### Scenario: Valid direction slug - struct
- **WHEN** user navigates to `/direction/struct`
- **THEN** system renders the structure design direction detail page with blue theme

#### Scenario: Invalid direction slug
- **WHEN** user navigates to `/direction/invalid-slug`
- **THEN** system returns 404 page

---

### Requirement: Hero Section display

The system SHALL display a hero section with direction title, description, and decorative background elements.

#### Scenario: Desktop hero section
- **WHEN** user views direction page on desktop (width >= 1024px)
- **THEN** system displays hero section with 64px title, 800px max-width description, and decorative elements

#### Scenario: Mobile hero section
- **WHEN** user views direction page on mobile (width < 768px)
- **THEN** system displays hero section with 32px title, 14px description, and scaled decorative elements

---

### Requirement: Tech Stack display

The system SHALL display 4 technology cards showing the core technologies for each direction.

#### Scenario: Desktop tech stack
- **WHEN** user views tech stack section on desktop
- **THEN** system displays 4 technology cards in horizontal row with name and description

#### Scenario: Mobile tech stack
- **WHEN** user views tech stack section on mobile
- **THEN** system displays technology cards in 2x2 grid layout

---

### Requirement: Learning Path display

The system SHALL display a 4-step learning path with numbered steps.

#### Scenario: Desktop learning path
- **WHEN** user views learning path section on desktop
- **THEN** system displays 4 steps horizontally with arrow connectors

#### Scenario: Mobile learning path
- **WHEN** user views learning path section on mobile
- **THEN** system displays steps vertically in a column

---

### Requirement: Career Section display

The system SHALL display career development information including positions and companies.

#### Scenario: Desktop career cards
- **WHEN** user views career section on desktop
- **THEN** system displays cards in horizontal layout with text on left, image on right

#### Scenario: Mobile career cards
- **WHEN** user views career section on mobile
- **THEN** system displays cards vertically with image on top

---

### Requirement: Recruitment Info display

The system SHALL display recruitment requirements and an "Apply Now" button.

#### Scenario: Recruitment card display
- **WHEN** user views recruitment section
- **THEN** system displays gradient background card with requirements list and "Apply Now" button

#### Scenario: Apply button click
- **WHEN** user clicks "Apply Now" button
- **THEN** system navigates to `/enroll` page

---

### Requirement: Theme color system

The system SHALL apply direction-specific theme colors throughout the page.

#### Scenario: Computer Vision theme
- **WHEN** user views `/direction/cv` page
- **THEN** system uses purple theme (#8B5CF6 primary)

#### Scenario: Embedded Development theme
- **WHEN** user views `/direction/embed` page
- **THEN** system uses green theme (#10B981 primary)

#### Scenario: Structure Design theme
- **WHEN** user views `/direction/struct` page
- **THEN** system uses blue theme (#3B82F6 primary)
