## ADDED Requirements

### Requirement: Admin can delete a chunk
The system SHALL allow administrators to delete a single chunk of a knowledge document. Deletion is physical and SHALL remove the chunk row, its tag associations, and decrement the document's `chunk_count`, all within one transaction.

#### Scenario: Successful chunk deletion
- **WHEN** an administrator deletes a chunk whose document status is `COMPLETED`, `FAILED`, or `CANCELED`
- **THEN** the system deletes the row from `tb_rag_chunks` (the chunk vector is stored on the row and is removed with it)
- **AND** deletes all rows for that chunk from `tb_rag_chunk_tags` in the same transaction
- **AND** decrements `tb_rag_docs.chunk_count` by 1 for the chunk's document in the same transaction
- **AND** returns success without publishing any re-embed message

#### Scenario: Delete while document is parsing
- **WHEN** an administrator attempts to delete a chunk whose document status is `PENDING`, `PARSING`, or `CANCELING`
- **THEN** the system rejects the request with `409 Conflict` and no data is modified

#### Scenario: Delete non-existent chunk
- **WHEN** an administrator deletes a chunk id that does not exist (including a chunk whose document was just deleted, cascading its chunks)
- **THEN** the system returns `404 Not Found`

#### Scenario: Management UI confirms and indicates progress
- **WHEN** an administrator clicks "delete" on a chunk in the chunk detail page
- **THEN** the client SHALL show a Popconfirm confirmation before issuing the request
- **AND** the delete button SHALL display a loading spinner while the request is in flight
- **AND** the chunk list refreshes after successful deletion
