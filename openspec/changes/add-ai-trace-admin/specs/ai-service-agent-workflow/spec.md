## MODIFIED Requirements

### Requirement: Session state managed via LangGraph checkpointer

The `RagAgent` SHALL use a LangGraph checkpointer to manage conversation state per `conversation_id`.

The `conversation_id` SHALL be server-authoritative: the AI service is the sole generator of conversation identifiers, and it SHALL return the identifier to the client so the client can continue the same conversation.

#### Scenario: New conversation
- **WHEN** a request arrives without a `conversation_id`
- **THEN** the server generates a new thread id
- **AND** initializes a fresh graph state containing only the system prompt and pre-disclosed user message
- **AND** returns the generated identifier to the client

#### Scenario: Continuing conversation
- **WHEN** a request arrives with an existing `conversation_id`
- **THEN** the graph resumes from the previously persisted state for that thread
- **AND** the new user message is appended to the existing message list
- **AND** the same identifier is returned to the client unchanged

#### Scenario: Non-streaming path returns the identifier
- **WHEN** a client calls `POST /ai/v1/chat`
- **THEN** the response SHALL contain the authoritative `conversation_id`
- **AND** the identifier SHALL be the same value that the streaming path would have returned for the same conversation

#### Scenario: Client-supplied identifier is honored
- **WHEN** a client supplies a `conversation_id` it previously received from the server
- **THEN** the server SHALL treat it as authoritative
- **AND** SHALL NOT replace or regenerate it

#### Scenario: Client does not generate identifiers
- **WHEN** the frontend initiates a new conversation
- **THEN** it SHALL send no `conversation_id`
- **AND** SHALL adopt the identifier returned by the server
- **AND** SHALL reuse that identifier for every subsequent request in the same conversation
