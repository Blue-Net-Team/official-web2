## ADDED Requirements

### Requirement: Streaming reasoning chunks
The AI service SHALL emit `reasoning` events as incremental text fragments during the tool-calling phase of a streaming chat response, matching the streaming behavior of `content` events.

#### Scenario: DeepSeek streaming reasoning
- **WHEN** a client calls `POST /ai/v1/chat/stream` and the configured LLM provider is DeepSeek
- **THEN** the server emits multiple `data: {"type": "reasoning", "content": "..."}` SSE events, each containing a fragment of the model's reasoning, before any `tool_call` or final `content` events

### Requirement: Tool calls remain atomic
The AI service SHALL continue to emit `tool_call` and `tool_result` events only after the complete tool-call arguments have been assembled; it MUST NOT emit partial `tool_call` events.

#### Scenario: Tool call aggregation
- **WHEN** the model produces a streaming function-call across multiple SSE deltas
- **THEN** the server aggregates all fragments, validates the JSON arguments, and emits exactly one `data: {"type": "tool_call", "tool_name": "...", "tool_args": {...}}` event per tool call

### Requirement: Provider interface uniformity
The `LLMProvider.stream_with_tools()` interface SHALL yield a uniform event stream for all providers, even if the underlying provider cannot natively stream reasoning.

#### Scenario: SiliconFlow and Ollama compatibility
- **WHEN** the configured provider is SiliconFlow or Ollama
- **THEN** `stream_with_tools()` still yields `StreamEvent` objects of types `reasoning`, `content`, `tool_call`, and `done`, preserving the existing non-streaming aggregation behavior internally

### Requirement: Backward-compatible SSE protocol
The public SSE protocol for `/ai/v1/chat/stream` SHALL keep the same event types and field names; existing clients SHALL NOT need to change their event parsing logic.

#### Scenario: Existing client consumes new stream
- **WHEN** an existing client parses the SSE stream and appends `content` fields for each event type
- **THEN** `reasoning` events are displayed incrementally and `tool_call` / `tool_result` / `content` / `done` events behave exactly as before
