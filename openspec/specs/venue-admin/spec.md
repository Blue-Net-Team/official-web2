## Purpose

Admin-side venue management providing CRUD operations, image upload, and reordering through the `/admin/venue` page. Complements the public venue display by giving administrators a visual interface to maintain venue records without direct database access.

## Requirements

### Requirement: Admin can view venue list
The system SHALL provide a paginated list of venues in the admin interface, ordered by `sort_order` descending.

#### Scenario: Venue list loads successfully
- **WHEN** admin navigates to `/admin/venue`
- **THEN** the page displays a table with columns: name, subtitle, description (truncated), sort order
- **AND** the list is ordered by sort_order DESC

### Requirement: Admin can create a new venue
The system SHALL allow admin to create a new venue with name, subtitle, description, image, and sort order.

#### Scenario: Successfully create venue
- **WHEN** admin clicks "新建" button
- **AND** fills in the form with valid data
- **AND** uploads an image (optional)
- **AND** clicks "保存"
- **THEN** the venue is created
- **AND** the list refreshes

#### Scenario: Create venue with missing name
- **WHEN** admin submits the form without filling the name field
- **THEN** the form shows validation error "场地名称不能为空"
- **AND** the venue is not created

### Requirement: Admin can edit an existing venue
The system SHALL allow admin to update any field of an existing venue.

#### Scenario: Successfully update venue
- **WHEN** admin opens a venue in edit mode
- **AND** changes one or more fields
- **AND** clicks "保存"
- **THEN** the venue is updated
- **AND** the list refreshes

### Requirement: Admin can delete a venue
The system SHALL allow admin to delete a venue with confirmation.

#### Scenario: Successfully delete venue
- **WHEN** admin clicks "删除" on a venue
- **AND** confirms the deletion in the modal
- **THEN** the venue is removed
- **AND** the list refreshes

### Requirement: Admin can update venue image
The system SHALL allow admin to upload or replace the image of a venue.

#### Scenario: Successfully update image
- **WHEN** admin uploads a new image in the venue drawer
- **THEN** the image is uploaded via file service
- **AND** the venue's imageFileId is updated

### Requirement: Admin can reorder venues
The system SHALL allow admin to change the display order of venues via drag-and-drop or move buttons.

#### Scenario: Reorder via move up button
- **WHEN** admin clicks "上移" on a venue
- **THEN** the venue swaps sort_order with the venue above it
- **AND** the list refreshes with new order

#### Scenario: Reorder via drag-and-drop
- **WHEN** admin drags a venue row to a new position
- **THEN** the system recalculates sort_order for affected venues
- **AND** the list displays in the new order
