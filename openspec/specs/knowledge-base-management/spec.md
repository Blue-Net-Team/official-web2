## ADDED Requirements

### Requirement: Admin can upload knowledge documents
The system SHALL allow administrators to upload markdown documents to the knowledge base.

#### Scenario: Successful document upload
- **WHEN** an administrator uploads a `.md` file via the management platform
- **THEN** the system saves the file to OSS with `FileType.KNOWLEDGE`
- **AND** creates a record in `tb_file` with status `ACTIVE`
- **AND** creates a record in `tb_rag_docs` with status `PENDING` and `file_id` referencing `tb_file.id`
- **AND** publishes a RabbitMQ message to `knowledge.parse` queue with `docId`, `fileId`, `downloadUrl`, and `reparse=false`

#### Scenario: Unsupported file type
- **WHEN** an administrator uploads a file that is not `.md`
- **THEN** the system rejects the upload with a `400 Bad Request` error

### Requirement: Admin can view knowledge documents
The system SHALL provide a paginated list of all knowledge documents with their parse status.

#### Scenario: View document list
- **WHEN** an administrator navigates to the knowledge document list page
- **THEN** the system returns paginated results including document id, title, parse status, chunk count, created time, and updated time

#### Scenario: View document parse status
- **WHEN** an administrator queries a document's detail
- **THEN** the system returns the document's current parse status directly from `tb_rag_docs` without calling the AI Service

### Requirement: Admin can reparse knowledge documents
The system SHALL allow administrators to trigger re-parsing of an existing document, replacing all its previous chunks.

#### Scenario: Successful reparse
- **WHEN** an administrator clicks "reparse" on a completed or failed document
- **THEN** the system updates `tb_rag_docs.status` to `PENDING`
- **AND** publishes a RabbitMQ message to `knowledge.parse` queue with `reparse=true`
- **AND** the AI Service deletes all existing chunks for this `doc_id` before inserting new ones

#### Scenario: Reparse triggers tag count recalculation
- **WHEN** a document is re-parsed
- **THEN** after new chunks are inserted, the system SHALL recalculate and update `chunks_count` for all affected tags in `tb_rag_tags`

### Requirement: Admin can delete knowledge documents
The system SHALL allow administrators to delete documents and all associated chunks.

#### Scenario: Successful deletion
- **WHEN** an administrator deletes a document
- **THEN** the system deletes the file from OSS
- **AND** deletes the record from `tb_file`
- **AND** deletes the record from `tb_rag_docs`
- **AND** deletes all associated chunks from `tb_rag_chunks`
- **AND** recalculates tag counts for affected tags

### Requirement: Admin can view document chunks
The system SHALL allow administrators to view all chunks belonging to a specific document.

#### Scenario: View chunks
- **WHEN** an administrator navigates to a document's chunk detail page
- **THEN** the system returns a paginated list of chunks including chunk id, content preview, tag ids, vector status, and source
- **AND** tag names are resolved by the client from the tag pool
### Requirement: Admin can manage knowledge tags
The system SHALL provide full CRUD tag management capabilities.

#### Scenario: View tag list
- **WHEN** an administrator navigates to the tag management page
- **THEN** the system returns a paginated list of all tags including tag name, description, and chunk count

#### Scenario: Update tag description
- **WHEN** an administrator updates a tag's description
- **THEN** the system updates `tb_rag_tags.tag_description` without re-vectorization
### Requirement: Admin can create knowledge tags
The system SHALL allow administrators to create a tag with a name and optional description. Tag vectorization SHALL be performed asynchronously by the ai-service.

#### Scenario: Successful tag creation
- **WHEN** an administrator creates a tag with a unique non-blank name
- **THEN** the system inserts the tag into `tb_rag_tags` (without `tag_vector`) in a transaction
- **AND** publishes a `tag-upsert` message with `tagId`
- **AND** the ai-service embeds the tag name and updates `tb_rag_tags.tag_vector`

#### Scenario: Duplicate tag name
- **WHEN** an administrator creates a tag whose name already exists in `tb_rag_tags`
- **THEN** the system rejects the request with `400 Bad Request`

### Requirement: Admin can rename a knowledge tag
The system SHALL allow administrators to rename a tag. Renaming SHALL update only the tag row; chunk associations SHALL remain untouched because associations are stored by tag id.

#### Scenario: Successful rename
- **WHEN** an administrator renames a tag
- **THEN** the system updates `tb_rag_tags.tag_name` and publishes a `tag-upsert` message
- **AND** the ai-service re-embeds the new name and updates `tag_vector`
- **AND** no `tb_rag_chunk_tags` rows are modified

### Requirement: Admin can delete a knowledge tag
The system SHALL allow administrators to delete a tag, automatically dissociating it from all chunks. Deletion is physical.

#### Scenario: Delete a tag in use
- **WHEN** an administrator deletes a tag referenced by `N` chunks
- **THEN** the system deletes the tag row from `tb_rag_tags`
- **AND** deletes all rows for that tag from `tb_rag_chunk_tags` in the same transaction
- **AND** the response includes the dissociated chunk count for confirmation display

#### Scenario: Delete a tag that does not exist
- **WHEN** an administrator deletes a non-existent tag
- **THEN** the system returns `404 Not Found`

### Requirement: Chunk-tag associations use a junction table
The system SHALL store chunk-tag associations in `tb_rag_chunk_tags(chunk_id, tag_id)` without physical foreign keys, maintained at the application layer.

#### Scenario: Association lookup
- **WHEN** the system resolves the tags of a chunk
- **THEN** it joins `tb_rag_chunk_tags` with `tb_rag_tags` on `tag_id`

### Requirement: Parse status enum values
The system SHALL use the following parse status values stored in lowercase as `ValueEnum`:
- `pending` — waiting to be parsed
- `parsing` — currently being parsed
- `completed` — parsing finished successfully
- `failed` — parsing failed
- `canceling` — cancel request issued, consumer will check and abort
- `canceled` — parsing was canceled
