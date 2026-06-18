## ADDED Requirements

### Requirement: Global AI chat entry
The system SHALL provide a global floating action button entry for AI customer service on all public pages, grouped with the existing Bug feedback button.

#### Scenario: Entry visibility
- **WHEN** a visitor is on any public page
- **THEN** a floating AI chat button is visible at the bottom-right corner alongside the Bug feedback button

#### Scenario: Entry opens chat drawer
- **WHEN** the visitor clicks the AI chat button
- **THEN** a chat drawer slides out from the right side of the viewport

### Requirement: Chat drawer layout
The system SHALL render a chat drawer containing a header, a scrollable message list, and a message input area.

#### Scenario: Drawer sections
- **WHEN** the chat drawer is open
- **THEN** the drawer displays a title, a reset-conversation control, a close control, a message list, and a text input with a send button

### Requirement: Streaming chat integration
The system SHALL send user messages to the `POST /ai/v1/chat/stream` endpoint and render Server-Sent Events as a single assistant bubble.

#### Scenario: Send message
- **WHEN** the user enters text and clicks send
- **THEN** a user bubble appears immediately and the system opens an SSE connection to the AI service

#### Scenario: Aggregate SSE events into one bubble
- **WHEN** the backend emits multiple `reasoning`, `tool_call`, `tool_result`, and `content` events for one assistant response
- **THEN** all events are displayed inside a single assistant bubble and the bubble is finalized when a `done` event is received

### Requirement: Reasoning display
The system SHALL display the assistant reasoning process while it is being generated and collapse it after the reasoning phase ends.

#### Scenario: Reasoning expands during generation
- **WHEN** the assistant is emitting `reasoning` events
- **THEN** the reasoning block is expanded and appends each fragment in real time

#### Scenario: Reasoning collapses after completion
- **WHEN** the assistant response moves past the reasoning phase
- **THEN** the reasoning block collapses and shows only a summary indicator

### Requirement: Tool call display
The system SHALL display tool calls and tool results inside the assistant bubble, collapsed by default.

#### Scenario: Tool call collapsed
- **WHEN** a `tool_call` event is received
- **THEN** a tool call card is added to the bubble in a collapsed state showing the tool name

#### Scenario: Tool result attached
- **WHEN** a `tool_result` event follows a `tool_call`
- **THEN** the result is attached to the corresponding tool call card and remains collapsed by default

### Requirement: Markdown rendering
The system SHALL render the final `content` events as Markdown in real time.

#### Scenario: Markdown content streams
- **WHEN** `content` events are received
- **THEN** the accumulated content is rendered as Markdown, including support for headings, lists, links, code, and blockquotes

### Requirement: Conversation session management
The system SHALL keep the same `conversation_id` while the drawer remains open during a page session and start a new conversation after a page refresh.

#### Scenario: Continue conversation
- **WHEN** the user closes and reopens the drawer without refreshing the page
- **THEN** the previous messages and `conversation_id` are preserved

#### Scenario: New conversation on refresh
- **WHEN** the user refreshes the page
- **THEN** the chat state is reset and a new `conversation_id` is generated on the next message

#### Scenario: Manual reset
- **WHEN** the user clicks the reset control
- **THEN** the visible messages are cleared and a new `conversation_id` is generated

### Requirement: AI service configuration
The system SHALL read the AI service base URL from public environment variables separate from the main API service.

#### Scenario: Environment variables
- **WHEN** the application builds or runs
- **THEN** it uses `NEXT_PUBLIC_AI_SERVICE_HOST`, `NEXT_PUBLIC_AI_SERVICE_PORT`, `NEXT_PUBLIC_AI_SERVICE_SSL_ENABLED`, and `NEXT_PUBLIC_AI_SERVICE_PREFIX` to construct the AI service base URL

#### Scenario: Docker deployment
- **WHEN** the frontend container starts in Docker
- **THEN** the AI service host/port/prefix are injected from the compose environment

### Requirement: Mobile responsiveness
The system SHALL adapt the drawer width for mobile viewports.

#### Scenario: Mobile drawer
- **WHEN** the viewport width is below the tablet breakpoint
- **THEN** the drawer occupies most of the screen width instead of a fixed narrow panel
