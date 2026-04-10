## MODIFIED Requirements

### Requirement: Comprehensive audit logging
The Audit entity SHALL record all system operations for security auditing and troubleshooting.

#### Scenario: Audit record creation
- **WHEN** any operation is performed
- **THEN** action_arg SHALL store parameters as JSON (max 4000 characters), with sensitive fields masked
- **THEN** action_user_id MAY reference the User (null for unauthenticated)
- **THEN** action_time SHALL record the operation timestamp
- **THEN** ip_address SHALL store the client IP
- **THEN** user_agent SHALL store the client User-Agent
- **THEN** success_state SHALL indicate success or failure
- **THEN** request_method SHALL store the HTTP method
- **THEN** request_uri SHALL store the raw HTTP request URI
- **THEN** request_uri_pattern SHALL store the normalized URI pattern extracted from Spring's `HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE` (e.g., `/api/v1/file/download/{fileId}`)
- **THEN** http_status SHALL store the HTTP response status code
- **THEN** response_message SHALL store the response message
- **THEN** stack_trace MAY store the exception stack trace (if failed)
- **THEN** duration_ms SHALL store the request processing time in milliseconds

#### Scenario: URI pattern extraction fallback
- **WHEN** `BEST_MATCHING_PATTERN_ATTRIBUTE` is null
- **THEN** request_uri_pattern SHALL fall back to the raw request_uri value
