## ADDED Requirements

### Requirement: Drag-and-drop resource sorting

The system SHALL allow administrators (role `MEMBER` or higher) to reorder software resources on the `/admin/resources` page by dragging table rows, persisting the new order via a batch sort endpoint.

#### Scenario: Admin reorders resources by dragging

- **WHEN** an admin drags a resource row and drops it at a new position within the current page
- **THEN** the frontend optimistically re-renders the rows in the new order
- **AND** it sends `PUT /api/v1/admin/software-resources/sort` with `{ items: [{ id, sortOrder }] }` recalculated for every row on the current page (`sortOrder = currentPage * pageSize + rowIndex + 1`)
- **AND** the backend updates each resource's `sort_order` accordingly

#### Scenario: Persisted order survives reload

- **WHEN** the batch sort request succeeds and the admin reloads the list
- **THEN** the resources appear in the newly persisted order (`ORDER BY sort_order ASC, id ASC`)
- **AND** the public `/resources` list reflects the same order

#### Scenario: Failed sort reverts the optimistic order

- **WHEN** the batch sort request fails
- **THEN** the frontend restores the previous row order
- **AND** it shows an error message

#### Scenario: Non-admin cannot drag to sort

- **WHEN** a user with role below `MEMBER` views `/admin/resources`
- **THEN** the drag handle column is not shown
- **AND** any direct call to the batch sort endpoint is rejected with 403 Forbidden

### Requirement: Batch sort endpoint validation

The batch sort endpoint SHALL validate the request payload and the existence of every referenced resource before applying changes.

#### Scenario: Empty item list is rejected

- **WHEN** the batch sort request contains an empty `items` list
- **THEN** the system rejects the request with a 400 Bad Request response

#### Scenario: Unknown resource id is rejected

- **WHEN** the batch sort request references a resource id that does not exist
- **THEN** the system rejects the request and does not modify any `sort_order`

#### Scenario: Unique permission for sort endpoint

- **WHEN** the application starts
- **THEN** `PermissionScanner` MUST validate the new permission value `software-resource:sort` without duplicate errors
