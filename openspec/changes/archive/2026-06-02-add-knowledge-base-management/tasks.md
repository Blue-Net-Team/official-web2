## 1. Database & Enums

- [x] 1.1 Create Flyway migration `V18__update_rag_docs_for_knowledge_management.sql` (add status, chunk_count, error_message, created_at, updated_at to tb_rag_docs)
- [x] 1.2 Add `DocParseStatus` enum implementing `ValueEnum` (pending, parsing, completed, failed, canceling, canceled)
- [x] 1.3 Add `KNOWLEDGE` to `FileType` enum (value="knowledge")

## 2. API Service — RabbitMQ Infrastructure

- [x] 2.1 Create `KnowledgeQueueConfig` (DirectExchange, Queue, Binding for `knowledge.parse`)
- [x] 2.2 Create `KnowledgeParsePublisher` component with `publish(docId, fileId, downloadUrl, reparse)` method
- [x] 2.3 Add RabbitMQ environment variables to `application.yml` (reuse existing rabbitmq config)

## 3. API Service — Domain & Repository

- [x] 3.1 Create `KnowledgeDoc` domain entity (id, fileId, title, status, chunkCount, errorMessage, createdAt, updatedAt)
- [x] 3.2 Create `KnowledgeDocRepository` interface and `KnowledgeDocRepositoryImpl`
- [x] 3.3 Create `KnowledgeDocMapper` and `tb_rag_docs` CRUD XML

## 4. API Service — Application Layer

- [x] 4.1 Create `KnowledgeBaseAppService` with `uploadDocument()`, `reparse()`, `cancelParse()`, `deleteDocument()` methods
- [x] 4.2 Create `KnowledgeDocQueryService` for list/detail/chunk/tag queries
- [x] 4.3 Create DTOs: `KnowledgeDocDTO`, `KnowledgeChunkDTO`, `KnowledgeTagDTO`, `PageDTO` wrappers

## 5. API Service — Controller

- [x] 5.1 Create `KnowledgeDocController` with `@RequiresPermission` annotations for all endpoints
- [x] 5.2 Implement `POST /api/v1/admin/knowledge/docs` (upload, only .md allowed)
- [x] 5.3 Implement `POST /api/v1/admin/knowledge/docs/{id}/reparse`
- [x] 5.4 Implement `DELETE /api/v1/admin/knowledge/docs/{id}`
- [x] 5.5 Implement `GET /api/v1/admin/knowledge/docs` (paginated list)
- [x] 5.6 Implement `GET /api/v1/admin/knowledge/docs/{id}/chunks` (paginated chunks)
- [x] 5.7 Implement `GET /api/v1/admin/knowledge/tags` (paginated tags)
- [x] 5.8 Implement `PUT /api/v1/admin/knowledge/tags/{id}` (update description)

## 6. AI Service — Environment & Dependencies

- [x] 6.1 Add `aio-pika` to `pyproject.toml`
- [x] 6.2 Add RabbitMQ environment variables to `setting.py` (RABBITMQ_HOST, PORT, USERNAME, PASSWORD)
- [x] 6.3 `httpx` already exists in pyproject.toml for downloading files via pre-signed URL

## 7. AI Service — Parse Pipeline Refactor

- [x] 7.1 Extract `parse_single_document(doc_id, file_id, download_url, reparse)` to `pipeline/document_parser.py`
- [x] 7.2 Implement file download via HTTP GET to pre-signed URL
- [x] 7.3 Implement old chunk cleanup when `reparse=true`
- [x] 7.4 Implement status update logic (parsing → completed/failed/canceled)
- [x] 7.5 Implement `recalculate_tag_counts()` after parse completion
- [x] 7.6 Refactor `load2db_pipeline.py` to reuse `ingest_chunks()` from `document_parser.py`

## 8. AI Service — RabbitMQ Consumer

- [x] 8.1 Create `messaging/parse_consumer.py` with `start_parse_consumer()` coroutine
- [x] 8.2 Handle message parsing, status checking, cancellation logic
- [x] 8.3 Wire consumer into `main.py` lifespan (start on app startup, cancel on shutdown)

## 9. Frontend — Menu & Routing

- [x] 9.1 Add "知识库管理" menu group to `AdminNav/index.tsx` menuConfig
- [x] 9.2 Create `src/app/admin/knowledge/docs/page.tsx`
- [x] 9.3 Create `src/app/admin/knowledge/docs/[docId]/chunks/page.tsx`
- [x] 9.4 Create `src/app/admin/knowledge/tags/page.tsx`

## 10. Frontend — API Integration

- [x] 10.1 Create `src/apis/services/knowledge.service.ts` with all knowledge base API calls
- [x] 10.2 Implement document list page with upload, reparse, delete actions
- [x] 10.3 Implement chunk detail page (content preview, tags)
- [x] 10.4 Implement tag management page with editable descriptions
- [x] 10.5 Add parse status polling (every 3 seconds) on document list page

## 11. Integration & Testing

- [x] 11.1 Verify RabbitMQ queue creation and message flow end-to-end
- [x] 11.2 Test document upload → parse → chunks visible full flow
- [x] 11.3 Test reparse (old chunks deleted, new chunks inserted, tag counts updated)
- [x] 11.4 Test cancellation flow (canceling → consumer aborts → canceled)
- [x] 11.5 Test delete document (OSS file + tb_file + tb_rag_docs + chunks all cleaned)
- [x] 11.6 Verify `load2db_pipeline.py` offline import still works after refactor
