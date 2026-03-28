# Team Vibe Component

## Purpose
团队氛围展示组件，用于在主页展示 BlueNet 团队的轻松融洽氛围和技术培养环境。

## Requirements

### Requirement: Component renders main title
The component SHALL display a main title "重新定义团队氛围" at the top of the section.

#### Scenario: Main title is visible
- **WHEN** the TeamVibe component is rendered
- **THEN** the main title "重新定义团队氛围" is displayed with 43px font size, white color, and bold weight

### Requirement: Component renders content card with text and image areas
The component SHALL display a content card containing a left text area and a right image area.

#### Scenario: Content card structure is correct
- **WHEN** the TeamVibe component is rendered
- **THEN** a content card is displayed with:
  - Border: 3px solid #1E3D9A
  - Border radius: 36px on top-left and bottom-left corners
  - Left side: text area with padding 36px 32px
  - Right side: image area with team photo and gradient overlay

### Requirement: Text area displays subtitle and descriptions
The text area SHALL display a subtitle and two description paragraphs.

#### Scenario: Text content is displayed correctly
- **WHEN** the TeamVibe component is rendered
- **THEN** the text area contains:
  - Subtitle: "队内氛围融洽，技术精湛" with 35px font size, white color, bold weight
  - First description: "团队氛围轻松融洽，弹性工作，无竞赛、论文等硬性指标。旨在培养学生学习更多新技术应用到工程实践" with 20px font size, white color
  - Second description: "进入团队后，可以跟学长和老师学习行业前沿技术，共同实现项目落地，丰富简历内容" with 20px font size, white color

### Requirement: Image area displays team photo with gradient overlay
The image area SHALL display the team photo with a left-to-right gradient overlay.

#### Scenario: Image area renders correctly
- **WHEN** the TeamVibe component is rendered
- **THEN** the image area displays:
  - Background image: team_vibe.jpg from src/assets/
  - Gradient overlay: linear gradient from black (left) to transparent (right)
  - Background size: cover
  - Background position: center

### Requirement: Component spans full width and viewport height
The component SHALL span 100% width and 110vh height of its parent container.

#### Scenario: Component dimensions are correct
- **WHEN** the TeamVibe component is rendered inside a container
- **THEN** the component width equals the container width
- **AND** the component height equals 110vh (110% of viewport height)
