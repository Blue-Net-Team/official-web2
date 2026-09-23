## MODIFIED Requirements

### Requirement: Admin can view tag list
The system SHALL provide full CRUD tag management capabilities.

#### Scenario: View tag list
- **WHEN** an administrator navigates to the tag management page
- **THEN** the system returns a paginated list of all tags including tag name, description, chunk count, and vector status (`synced` or `embedding`)

#### Scenario: Update tag description
- **WHEN** an administrator updates a tag's description
- **THEN** the system updates `tb_rag_tags.tag_description` without re-vectorization
- **AND** the tag's `vector_status` remains unchanged

### Requirement: Admin can create knowledge tags
The system SHALL allow administrators to create a tag with a name and optional description. Tag vectorization SHALL be performed asynchronously by the ai-service.

#### Scenario: Successful tag creation
- **WHEN** an administrator creates a tag with a unique non-blank name
- **THEN** the system inserts the tag into `tb_rag_tags` (without `tag_vector`, with `vector_status = 'embedding'`) in a transaction
- **AND** publishes a `tag-upsert` message with `tagId`
- **AND** the ai-service embeds the tag name, updates `tb_rag_tags.tag_vector`, and sets `vector_status` to `synced`

#### Scenario: Duplicate tag name
- **WHEN** an administrator creates a tag whose name already exists in `tb_rag_tags`
- **THEN** the system rejects the request with `400 Bad Request`

### Requirement: Admin can rename a knowledge tag
The system SHALL allow administrators to rename a tag. Renaming SHALL update only the tag row; chunk associations SHALL remain untouched because associations are stored by tag id.

#### Scenario: Successful rename
- **WHEN** an administrator renames a tag
- **THEN** the system updates `tb_rag_tags.tag_name`, sets `vector_status = 'embedding'` in the same transaction, and publishes a `tag-upsert` message
- **AND** the ai-service re-embeds the new name, updates `tag_vector`, and sets `vector_status` to `synced`
- **AND** no `tb_rag_chunk_tags` rows are modified

## ADDED Requirements

### Requirement: Tag exposes vectorization status
The system SHALL track per-tag vector synchronization status so the management UI can indicate when a newly created or renamed tag is not yet searchable.

#### Scenario: Create or rename marks tag as embedding
- **WHEN** an administrator successfully creates or renames a tag
- **THEN** the system sets `tb_rag_tags.vector_status` to `embedding` in the same transaction

#### Scenario: Tag-upsert completion marks tag as synced
- **WHEN** the ai-service finishes embedding a tag
- **THEN** it sets `tb_rag_tags.vector_status` to `synced`

#### Scenario: Management UI indicates vectorization in progress
- **WHEN** the tag list contains any tag with `vector_status = 'embedding'`
- **THEN** the client SHALL render a spinning "向量化中" indicator on those tags
- **AND** the client SHALL poll the tag list every 3 seconds until no tag remains in `embedding` status
