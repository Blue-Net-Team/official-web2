### Requirement: Admin can edit a chunk's content and tags
The system SHALL allow administrators to edit a single chunk's Markdown content and re-assign its tags from the existing tag pool. Creating new tags from the chunk edit entry SHALL be rejected.

#### Scenario: Successful chunk edit
- **WHEN** an administrator submits an edit for chunk `{id}` with new content and a set of existing tag ids
- **THEN** the system validates the document is not in `PENDING`, `PARSING`, or `CANCELING` status (otherwise `409 Conflict`)
- **AND** validates every tag id exists in `tb_rag_tags` (otherwise `400 Bad Request`)
- **AND** updates `tb_rag_chunks.content` and rebuilds the chunk's associations in `tb_rag_chunk_tags` within one transaction
- **AND** publishes a `re-embed` message to the `knowledge` exchange with `chunkId`
- **AND** returns the updated chunk immediately (vector update happens asynchronously)

#### Scenario: Edit with non-existent tag
- **WHEN** an administrator submits a chunk edit containing a tag id not present in `tb_rag_tags`
- **THEN** the system rejects the request with `400 Bad Request` and the chunk remains unchanged

#### Scenario: Edit while document is parsing
- **WHEN** an administrator edits a chunk whose document status is `PARSING`
- **THEN** the system rejects the request with `409 Conflict`

### Requirement: Re-embed consumer updates the chunk vector
The ai-service SHALL consume `re-embed` messages and update the chunk's embedding.

#### Scenario: Successful re-embed
- **WHEN** the ai-service consumes a `re-embed` message for an existing chunk
- **THEN** it embeds the chunk's current content
- **AND** updates `tb_rag_chunks.chunk_vector` and sets `vector_status` to `synced`
- **AND** recalculates `chunks_count` for all tags from `tb_rag_chunk_tags`

#### Scenario: Chunk no longer exists
- **WHEN** the ai-service consumes a `re-embed` message but the chunk row is missing
- **THEN** it retries with backoff for up to ~10 seconds (producer transaction may not have committed)
- **AND** if still missing, logs and discards the message without requeueing

### Requirement: Chunk exposes vectorization status
The system SHALL track per-chunk vector synchronization status so the management UI can indicate when an edited chunk is not yet searchable.

#### Scenario: Edit marks chunk as embedding
- **WHEN** an administrator successfully edits a chunk
- **THEN** the system sets `tb_rag_chunks.vector_status` to `embedding` in the same transaction

#### Scenario: Re-embed completion marks chunk as synced
- **WHEN** the ai-service finishes re-embedding a chunk
- **THEN** it sets `vector_status` to `synced`

#### Scenario: Parse pipeline inserts synced chunks
- **WHEN** the parse pipeline inserts chunks after embedding
- **THEN** each new chunk is stored with `vector_status` `synced`

### Requirement: Admin can re-upload a document's attachment
The system SHALL allow administrators to replace a document's source `.md` attachment while keeping the same document id, triggering a full re-parse.

#### Scenario: Successful attachment re-upload
- **WHEN** an administrator uploads a new `.md` file to an existing document whose status is not `PENDING`/`PARSING`/`CANCELING`
- **THEN** the system saves the new file to OSS and creates a `tb_files` record
- **AND** updates `tb_rag_docs.file_id` to the new file and sets status to `PENDING` in one transaction
- **AND** publishes a `parse` message with `reparse=true`
- **AND** the previous OSS file becomes an orphan for the nightly cleanup job

#### Scenario: Re-upload while parsing
- **WHEN** an administrator attempts to re-upload an attachment while the document is `PARSING`
- **THEN** the system rejects the request with `409 Conflict`

#### Scenario: Non-md re-upload
- **WHEN** an administrator uploads a file that is not `.md`
- **THEN** the system rejects the upload with `400 Bad Request`

### Requirement: Consumer tolerates uncommitted producer transactions
The ai-service SHALL retry target-row lookups that miss due to producer transactions not yet committed, for all knowledge queue message types.

#### Scenario: Document row not yet visible
- **WHEN** the ai-service processes a `parse` message whose `doc_id` is not yet committed to `tb_rag_docs`
- **THEN** it retries the status lookup with a ~2 second interval up to 5 times
- **AND** aborts processing only if the row is still missing after all retries
