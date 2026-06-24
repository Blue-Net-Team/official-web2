## ADDED Requirements

### Requirement: Public resource list
The system SHALL expose a public page that lists all active software resources grouped by direction.

#### Scenario: Visitor views all resources
- **WHEN** a visitor opens `/resources`
- **THEN** the system displays tabs for "全部", "通用", "计算机视觉", "结构设计", and "嵌入式开发"
- **AND** the "全部" tab shows every active resource sorted by `sort_order` ascending

#### Scenario: Visitor filters by direction
- **WHEN** a visitor clicks the "计算机视觉" tab
- **THEN** the system shows only active resources whose `direction` equals `COMPUTER_VISION`

#### Scenario: Disabled resources are hidden from public
- **WHEN** a resource has `status` set to `DISABLED`
- **THEN** it MUST NOT appear in the public list

### Requirement: Resource metadata
Each software resource SHALL contain a name, direction, category, description, external URL, sort order, and status.

#### Scenario: Resource has required fields
- **WHEN** an administrator creates a resource
- **THEN** the system validates that `name`, `direction`, `external_url`, and `status` are provided
- **AND** it rejects the request if any required field is missing

#### Scenario: Resource direction values
- **WHEN** a resource is created
- **THEN** `direction` MUST be one of `GENERAL`, `COMPUTER_VISION`, `STRUCTURAL_DESIGN`, or `EMBEDDED`

#### Scenario: External URL only
- **WHEN** a resource is created or updated
- **THEN** the system stores only the provided external URL
- **AND** it MUST NOT initiate any file upload or storage operation

### Requirement: Admin resource management
The system SHALL provide an admin interface for members and higher roles to create, read, update, delete, sort, and enable or disable software resources.

#### Scenario: Member creates a resource
- **WHEN** a user with role `MEMBER` or higher accesses `/admin/resources` and submits a valid resource form
- **THEN** the system persists the resource and returns success

#### Scenario: Non-member cannot manage resources
- **WHEN** a user with role below `MEMBER` attempts to create, update, or delete a resource
- **THEN** the system rejects the request with a 403 Forbidden response

#### Scenario: Member updates sort order
- **WHEN** a member edits a resource and changes `sort_order`
- **THEN** the public list reflects the new order on the next render

#### Scenario: Member disables a resource
- **WHEN** a member changes a resource `status` to `DISABLED`
- **THEN** the resource disappears from the public list immediately
- **AND** it remains visible in the admin list

### Requirement: Unique permissions
All new REST endpoints for resource management SHALL declare globally unique `@RequiresPermission` values.

#### Scenario: Permission scanner validation
- **WHEN** the application starts
- **THEN** `PermissionScanner` MUST successfully validate the new permission values without duplicate errors

### Requirement: No audit timestamps
The `tb_software_resource` table SHALL NOT include `created_at` or `updated_at` columns.

#### Scenario: Database schema inspection
- **WHEN** inspecting the `tb_software_resource` schema
- **THEN** no `created_at` or `updated_at` columns exist

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
