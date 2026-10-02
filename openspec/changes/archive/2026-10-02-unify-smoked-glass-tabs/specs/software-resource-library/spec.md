# Delta: software-resource-library

## MODIFIED Requirements

### Requirement: Public resource list
The system SHALL expose a public page that lists all active software resources grouped by direction. Direction tabs SHALL be rendered by the unified `SmokedGlassTabs` component.

#### Scenario: Visitor views all resources
- **WHEN** a visitor opens `/resources`
- **THEN** the system displays tabs for "全部", "通用", "计算机视觉", "结构设计", and "嵌入式开发" via `SmokedGlassTabs`
- **AND** the "全部" tab shows every active resource sorted by `sort_order` ascending

#### Scenario: Visitor filters by direction
- **WHEN** a visitor clicks the "计算机视觉" tab
- **THEN** the system updates the URL to `/resources?tab=computer_vision` and shows only active resources whose `direction` equals `COMPUTER_VISION`

#### Scenario: Direction tab uses smoked glass style
- **WHEN** the resource page renders the direction tabs
- **THEN** the tabs appear as a smoked-glass capsule bar with a sliding capsule indicator and white highlighted active label
- **AND** no legacy self-styled pill buttons remain on the page

#### Scenario: Disabled resources are hidden from public
- **WHEN** a resource has `status` set to `DISABLED`
- **THEN** it MUST NOT appear in the public list
