## ADDED Requirements

### Requirement: AI Service consumes parse tasks from RabbitMQ
The AI Service SHALL consume messages from the `knowledge.parse` queue and execute document parsing asynchronously.

#### Scenario: Consumer receives parse message
- **WHEN** the AI Service receives a message from `knowledge.parse` queue
- **THEN** it SHALL extract `docId`, `fileId`, `downloadUrl`, and `reparse` from the message body
- **AND** it SHALL NOT query the `tb_file` table directly
- **AND** it SHALL NOT access OSS or any object storage directly

#### Scenario: Consumer checks cancellation before parsing
- **WHEN** the consumer starts processing a message
- **WHEN** it first checks `tb_rag_docs.status` for the given `doc_id`
- **THEN** if the status is `canceled`, it SHALL skip processing and acknowledge the message
- **AND** if the status is `canceling`, it SHALL abort the parse process and update status to `canceled`

### Requirement: AI Service downloads document via pre-signed URL
The AI Service SHALL download document content using the `downloadUrl` provided in the message.

#### Scenario: Download file content
- **WHEN** the consumer begins parsing a document
- **THEN** it SHALL perform an HTTP GET request to the `downloadUrl`
- **AND** it SHALL extract the raw text content from the downloaded file
- **AND** it SHALL handle only `.md` files (plain text)

### Requirement: AI Service performs semantic chunking
The AI Service SHALL split the document into semantic chunks using the existing `SemanticChunker`.

#### Scenario: Semantic chunking
- **WHEN** the document content is downloaded
- **THEN** the `SemanticChunker.split()` method SHALL be invoked
- **AND** the result SHALL be a list of text segments

### Requirement: AI Service handles reparse by cleaning old data
The AI Service SHALL delete existing chunks before re-parsing when `reparse=true`.

#### Scenario: Clean old chunks on reparse
- **WHEN** the message contains `reparse=true`
- **THEN** before inserting new chunks, the system SHALL execute `DELETE FROM tb_rag_chunks WHERE doc_id = ?`
- **AND** it SHALL NOT delete tags (tags may be reused by other documents)

### Requirement: AI Service updates document status throughout parsing
The AI Service SHALL update `tb_rag_docs.status` at key milestones.

#### Scenario: Status transitions during parse
- **WHEN** parsing begins, the system SHALL update status to `parsing`
- **WHEN** parsing completes successfully, the system SHALL update status to `completed` and set `chunk_count`
- **WHEN** parsing fails, the system SHALL update status to `failed` and set `error_message`
- **WHEN** parsing is canceled mid-way, the system SHALL update status to `canceled`

### Requirement: AI Service recalculates tag counts after parse
After chunks are inserted, the AI Service SHALL recalculate and update `tb_rag_tags.chunks_count`.

#### Scenario: Tag count recalculation
- **WHEN** all chunks for a document are inserted
- **THEN** the system SHALL count tag occurrences across all `tb_rag_chunks` rows
- **AND** update `tb_rag_tags.chunks_count` for each tag
- **AND** set `chunks_count = 0` for tags that are no longer referenced

### Requirement: Parse pipeline functions are reusable
The core parse logic SHALL be extracted into a reusable function that can be called both by the RabbitMQ consumer and by offline scripts.

#### Scenario: Offline batch import reuse
- **WHEN** the existing `load2db_pipeline.py` runs
- **THEN** it SHALL reuse the same `parse_single_document()` function that the consumer uses
- **AND** both paths SHALL produce identical chunking and tagging results
