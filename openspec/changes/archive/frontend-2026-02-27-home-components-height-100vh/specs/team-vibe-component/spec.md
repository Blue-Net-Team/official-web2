## MODIFIED Requirements

### Requirement: Component spans full width and viewport height

The component SHALL span 100% width and 110vh height of its parent container.

#### Scenario: Component dimensions are correct
- **WHEN** the TeamVibe component is rendered inside a container
- **THEN** the component width equals the container width
- **AND** the component height equals 110vh (110% of viewport height)

### Requirement: Component renders content card with text and image areas

The component SHALL display a content card containing a left text area and a right image area.

#### Scenario: Content card structure is correct
- **WHEN** the TeamVibe component is rendered
- **THEN** a content card is displayed with:
  - Border: 3px solid #1E3D9A
  - Border radius: 36px on top-left and bottom-left corners
  - Left side: text area with padding 36px 32px
  - Right side: image area with team photo and gradient overlay
