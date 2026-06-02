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
- **THEN** the system returns a paginated list of chunks including chunk id, content preview, tags, and source

### Requirement: Admin can manage knowledge tags
The system SHALL provide tag management capabilities.

#### Scenario: View tag list
- **WHEN** an administrator navigates to the tag management page
- **THEN** the system returns a paginated list of all tags including tag name, description, and chunk count

#### Scenario: Update tag description
- **WHEN** an administrator updates a tag's description
- **THEN** the system updates `tb_rag_tags.tag_description`

### Requirement: Parse status enum values
The system SHALL use the following parse status values stored in lowercase as `ValueEnum`:
- `pending` — waiting to be parsed
- `parsing` — currently being parsed
- `completed` — parsing finished successfully
- `failed` — parsing failed
- `canceling` — cancel request issued, consumer will check and abort
- `canceled` — parsing was canceled
