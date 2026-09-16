## MODIFIED Requirements

### Requirement: Backward-compatible SSE protocol

The public SSE protocol for `/ai/v1/chat/stream` SHALL keep the same event types and field names; existing clients SHALL NOT need to change their event parsing logic.

New event types MAY be introduced over time. Clients MUST ignore event types they do not recognize rather than treating them as errors.

#### Scenario: Existing client consumes new stream
- **WHEN** an existing client parses the SSE stream and appends `content` fields for each event type
- **THEN** `reasoning` events are displayed incrementally and `tool_call` / `tool_result` / `content` / `done` events behave exactly as before

#### Scenario: Unknown event type is ignored
- **WHEN** a client receives an SSE event whose `type` it does not recognize
- **THEN** the client SHALL skip that event
- **AND** SHALL continue processing subsequent events normally
- **AND** SHALL NOT surface an error to the user

#### Scenario: Existing field names are unchanged
- **WHEN** the server emits `reasoning` / `tool_call` / `tool_result` / `content` / `done` events
- **THEN** the JSON field names within those events SHALL remain `type`, `content`, `tool_name`, `tool_args`

## ADDED Requirements

### Requirement: Conversation identifier is emitted as the first SSE frame

The streaming endpoint SHALL emit the authoritative `conversation_id` before any other event so clients can persist it for subsequent requests.

#### Scenario: New conversation identifier is delivered first
- **WHEN** a client calls `POST /ai/v1/chat/stream` without a `conversation_id`
- **THEN** the server SHALL generate a new identifier
- **AND** SHALL emit `data: {"type": "conversation_id", "conversation_id": "<id>"}` as the first SSE frame
- **AND** all subsequent events in that stream SHALL belong to that conversation

#### Scenario: Existing conversation identifier is echoed back
- **WHEN** a client calls `POST /ai/v1/chat/stream` with an existing `conversation_id`
- **THEN** the server SHALL emit that same identifier in the first SSE frame

#### Scenario: Identifier is emitted even for blocked requests
- **WHEN** the request is refused or answered directly without entering the state graph
- **THEN** the `conversation_id` frame SHALL still be emitted first
- **AND** clients SHALL be able to continue the same conversation afterwards

#### Scenario: Identifier frame is emitted for every path
- **WHEN** a client calls `POST /ai/v1/chat/stream`
- **THEN** every response SHALL begin with the `conversation_id` frame
- **AND** the frame SHALL be emitted regardless of intent classification outcome
