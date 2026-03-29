## Purpose

Lab environment display page showing laboratory venues and equipment with responsive card layouts.

## Requirements

### Requirement: Lab environment page routing
The system SHALL provide a lab environment page at /lab-environment route.

#### Scenario: Access lab environment page
- **WHEN** user navigates to /lab-environment
- **THEN** system SHALL render the LabEnvironmentPage component

### Requirement: Hero section with fixed content
The page SHALL display a hero section with fixed title and subtitle.

#### Scenario: Hero section renders correctly
- **WHEN** page loads
- **THEN** hero section SHALL display title "实验室环境"
- **THEN** hero section SHALL display subtitle "专业的实验场地与先进的工程设备，为创新实践提供全方位支持"

### Requirement: Venue display section
The page SHALL display a venue section with 2x2 card grid layout.

#### Scenario: Venue cards render with data
- **WHEN** venue data is loaded successfully
- **THEN** system SHALL display venue cards in 2x2 grid layout
- **THEN** each card SHALL display venue image
- **THEN** each card SHALL display venue name as title
- **THEN** each card SHALL display venue subtitle
- **THEN** each card SHALL display venue description

#### Scenario: Venue cards loading state
- **WHEN** venue data is being fetched
- **THEN** system SHALL display loading skeleton or spinner

#### Scenario: Venue cards empty state
- **WHEN** no venue data exists
- **THEN** system SHALL display empty message or hide the section

### Requirement: Equipment display section
The page SHALL display an equipment section with 2x3 card grid layout.

#### Scenario: Equipment cards render with data
- **WHEN** equipment data is loaded successfully
- **THEN** system SHALL display equipment cards in 2x3 grid layout (2 rows, 3 columns)
- **THEN** each card SHALL display equipment image
- **THEN** each card SHALL display equipment name as title
- **THEN** each card SHALL display equipment brand as subtitle
- **THEN** each card SHALL display equipment description

#### Scenario: Equipment cards loading state
- **WHEN** equipment data is being fetched
- **THEN** system SHALL display loading skeleton or spinner

#### Scenario: Equipment cards empty state
- **WHEN** no equipment data exists
- **THEN** system SHALL display empty message or hide the section

### Requirement: Card styling consistency
All cards SHALL follow consistent styling patterns.

#### Scenario: Card container styling
- **WHEN** cards are rendered
- **THEN** card background SHALL be dark (#15151f)
- **THEN** card SHALL have 16px border radius
- **THEN** card SHALL have shadow effect
- **THEN** card content SHALL have 24px padding

#### Scenario: Card text styling
- **WHEN** cards are rendered
- **THEN** title SHALL be white (#ffffff) with 20px font size and 600 font weight
- **THEN** subtitle/brand SHALL be blue (#4a9eff) with 14px font size
- **THEN** description SHALL be gray (#a0a0b0) with 14px font size

### Requirement: Responsive layout
The page SHALL adapt to different screen sizes.

#### Scenario: Desktop layout
- **WHEN** viewport width >= 1200px
- **THEN** venue section SHALL display 2 cards per row
- **THEN** equipment section SHALL display 3 cards per row

#### Scenario: Tablet layout
- **WHEN** viewport width is between 768px and 1199px
- **THEN** cards SHALL adapt to fit available width

#### Scenario: Mobile layout
- **WHEN** viewport width < 768px
- **THEN** all cards SHALL display in single column
