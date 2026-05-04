## Purpose

Admin-side equipment management providing CRUD operations, image upload, and reordering through the `/admin/equipment` page. Complements the public equipment display by giving administrators a visual interface to maintain equipment records without direct database access.

## Requirements

### Requirement: Admin can view equipment list
The system SHALL provide a paginated list of equipment in the admin interface, ordered by `sort_order` descending.

#### Scenario: Equipment list loads successfully
- **WHEN** admin navigates to `/admin/equipment`
- **THEN** the page displays a table with columns: name, brand, description (truncated), sort order
- **AND** the list is ordered by sort_order DESC

### Requirement: Admin can create a new equipment
The system SHALL allow admin to create a new equipment with name, brand, description, image, and sort order.

#### Scenario: Successfully create equipment
- **WHEN** admin clicks "新建" button
- **AND** fills in the form with valid data
- **AND** uploads an image (optional)
- **AND** clicks "保存"
- **THEN** the equipment is created
- **AND** the list refreshes

#### Scenario: Create equipment with missing name
- **WHEN** admin submits the form without filling the name field
- **THEN** the form shows validation error "设备名称不能为空"
- **AND** the equipment is not created

### Requirement: Admin can edit an existing equipment
The system SHALL allow admin to update any field of an existing equipment.

#### Scenario: Successfully update equipment
- **WHEN** admin opens an equipment in edit mode
- **AND** changes one or more fields
- **AND** clicks "保存"
- **THEN** the equipment is updated
- **AND** the list refreshes

### Requirement: Admin can delete an equipment
The system SHALL allow admin to delete an equipment with confirmation.

#### Scenario: Successfully delete equipment
- **WHEN** admin clicks "删除" on an equipment
- **AND** confirms the deletion in the modal
- **THEN** the equipment is removed
- **AND** the list refreshes

### Requirement: Admin can update equipment image
The system SHALL allow admin to upload or replace the image of an equipment.

#### Scenario: Successfully update image
- **WHEN** admin uploads a new image in the equipment drawer
- **THEN** the image is uploaded via file service
- **AND** the equipment's imageFileId is updated

### Requirement: Admin can reorder equipment
The system SHALL allow admin to change the display order of equipment via drag-and-drop or move buttons.

#### Scenario: Reorder via move up button
- **WHEN** admin clicks "上移" on an equipment
- **THEN** the equipment swaps sort_order with the equipment above it
- **AND** the list refreshes with new order

#### Scenario: Reorder via drag-and-drop
- **WHEN** admin drags an equipment row to a new position
- **THEN** the system recalculates sort_order for affected equipment
- **AND** the list displays in the new order
