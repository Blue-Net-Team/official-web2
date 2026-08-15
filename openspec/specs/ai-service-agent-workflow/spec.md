# ai-service-agent-workflow Specification

## Purpose
TBD - created by archiving change refactor-ragagent-to-langgraph. Update Purpose after archive.
## Requirements
### Requirement: Intent guard precedes state graph invocation
The `RagAgent` SHALL classify user intent before invoking the LangGraph state graph and only allow `RETRIEVE` intents to enter the graph.

#### Scenario: Allowed intent reaches pre_disclose node
- **WHEN** the classifier returns `action=RETRIEVE`
- **THEN** the `RagAgent` appends the user message to the conversation
- **AND** invokes the state graph starting at the `pre_disclose` node
- **AND** the existing graph behavior remains unchanged

#### Scenario: Blocked intent bypasses the graph
- **WHEN** the classifier returns `action=REFUSE`
- **THEN** the `RagAgent` does NOT invoke the state graph
- **AND** does NOT call `tag_generate`, `tag_search_detailed`, embedding, or any retrieval tool
- **AND** returns the LLM-generated refusal as the final response

#### Scenario: Classification failure cannot bypass the graph
- **WHEN** intent classification raises an exception or produces no parseable result
- **THEN** the `RagAgent` does NOT invoke the state graph
- **AND** returns a fixed clarification reply asking the user to restate the question within the supported scope

#### Scenario: Direct intent bypasses the graph
- **WHEN** the classifier returns `action=DIRECT`
- **THEN** the `RagAgent` does NOT invoke the state graph
- **AND** returns the LLM-generated direct reply as the final response

#### Scenario: Sync and streaming paths both apply the guard
- **WHEN** a request is sent to either `POST /ai/v1/chat` or `POST /ai/v1/chat/stream`
- **THEN** both paths perform intent classification before any retrieval
- **AND** both paths produce equivalent refusal or direct-reply content for the same blocked/direct input

### Requirement: Agent workflow modeled as LangGraph StateGraph
The AI service SHALL implement the RAG multi-turn retrieval workflow as a `langgraph.StateGraph`, replacing the hand-written loop in `RagAgent`.

#### Scenario: State graph nodes
- **WHEN** the `RagAgent` is initialized
- **THEN** it builds a state graph containing `pre_disclose`, `agent`, `tool_executor` nodes and the `END` termination
- **AND** the graph transitions from `agent` to `tool_executor` when the LLM emits tool calls
- **AND** the graph transitions from `agent` to `END` when the LLM emits no tool calls

#### Scenario: Conditional stage routing
- **WHEN** `tool_executor` finishes and `tag_rounds` or `chunk_rounds` has not exceeded its limit
- **THEN** the graph routes back to `agent` for the next LLM call
- **AND** when the LLM no longer requests tools, the graph ends at `END`
- **AND** when a stage limit is exceeded the `tool_executor` returns a limit message and the graph routes back to `agent`

### Requirement: Pre-disclosure node injects initial retrieval context
The `pre_disclose` node SHALL call `tag_generate` and `tag_search_detailed`, then prepend the results to the first user message.

#### Scenario: Successful pre-disclosure
- **WHEN** a user sends a chat message
- **THEN** the `pre_disclose` node generates candidate tags
- **AND** retrieves detailed tag information
- **AND** the first user message in the graph state contains the original query plus the formatted tag results

#### Scenario: Pre-disclosure tag generation failure
- **WHEN** `tag_generate` raises an exception
- **THEN** the node logs a warning
- **AND** proceeds with an empty tag list
- **AND** the first user message informs the model that no related tags were found

### Requirement: Tool round limits enforced by state graph
The state graph SHALL enforce the same per-tool round limits as the existing `RagAgent`: tag search up to 4 rounds, chunk search by tags up to 3 rounds, and fallback chunk search up to 1 round.

#### Scenario: Tag search limit reached
- **WHEN** the LLM requests `tag_search_detailed` and `tag_rounds` is already 4
- **THEN** the `tool_executor` returns a limit message instead of executing the tool
- **AND** the graph routes back to `agent`

#### Scenario: Chunk search limit reached
- **WHEN** the LLM requests `chunk_search_by_tags` and `chunk_rounds` is already 3
- **THEN** the `tool_executor` returns a limit message instead of executing the tool
- **AND** the graph routes back to `agent`

#### Scenario: Fallback search limit reached
- **WHEN** the LLM requests `chunk_search` and `fallback_rounds` is already 1
- **THEN** the `tool_executor` returns a limit message instead of executing the tool
- **AND** the graph routes back to `agent`

### Requirement: SSE event protocol remains backward-compatible
The `/ai/v1/chat/stream` endpoint SHALL continue to emit the same event types and field names as before the refactor.

#### Scenario: Streaming reasoning events
- **WHEN** the configured LLM produces reasoning content during tool-calling
- **THEN** the server emits `data: {"type": "reasoning", "content": "..."}` events
- **AND** these events appear before the corresponding `tool_call` or final `content` events

#### Scenario: Streaming tool call and result events
- **WHEN** the LLM requests a tool
- **THEN** the server emits exactly one `data: {"type": "tool_call", "tool_name": "...", "tool_args": {...}}` event
- **AND** after the tool executes, emits exactly one `data: {"type": "tool_result", "tool_name": "...", "content": "..."}` event

#### Scenario: Streaming done event
- **WHEN** the graph reaches the end state
- **THEN** the server emits `data: {"type": "done"}`

### Requirement: Reasoning content preserved through the graph
The state graph SHALL capture and forward `reasoning_content` produced by the LLM provider, including during tool-calling rounds and when producing the final answer.

#### Scenario: Reasoning in tool-calling round
- **WHEN** `agent` node calls the LLM with tools
- **AND** the provider returns `reasoning_content`
- **THEN** the reasoning text is streamed to the client as `reasoning` events
- **AND** the reasoning is included in the assistant message stored in graph state

#### Scenario: Reasoning in final answer
- **WHEN** `agent` node produces no tool calls and the content becomes the final answer
- **AND** the provider returns `reasoning_content`
- **THEN** the reasoning text is streamed to the client as `reasoning` events
- **AND** the final non-streaming response includes the reasoning

### Requirement: Sync and streaming paths share the same state graph
The `RagAgent` SHALL use the same `langgraph.StateGraph` for both `chat()` and `chat_stream()`, differing only in how the final result is consumed.

#### Scenario: Sync invocation
- **WHEN** a client calls `POST /ai/v1/chat`
- **THEN** the server invokes the state graph with `graph.invoke()`
- **AND** returns the final content and reasoning as a single JSON response

#### Scenario: Streaming invocation
- **WHEN** a client calls `POST /ai/v1/chat/stream`
- **THEN** the server invokes the state graph with `graph.stream(stream_mode="custom")`
- **AND** yields the same reasoning, tool_call, tool_result, and content events as the synchronous path would have produced

#### Scenario: Equivalent behavior
- **WHEN** the same user input is sent to `/ai/v1/chat` and `/ai/v1/chat/stream`
- **THEN** both paths execute the same tool calls in the same order
- **AND** both paths apply the same round limits
- **AND** both paths produce the same final assistant content

### Requirement: Session state managed via LangGraph checkpointer
The `RagAgent` SHALL use a LangGraph checkpointer to manage conversation state per `conversation_id`.

#### Scenario: New conversation
- **WHEN** a request arrives without a `conversation_id`
- **THEN** the server generates a new thread id
- **AND** initializes a fresh graph state containing only the system prompt and pre-disclosed user message

#### Scenario: Continuing conversation
- **WHEN** a request arrives with an existing `conversation_id`
- **THEN** the graph resumes from the previously persisted state for that thread
- **AND** the new user message is appended to the existing message list

### Requirement: Non-streaming endpoint behavior unchanged
The `/ai/v1/chat` non-streaming endpoint SHALL return the same response shape and fields as before the refactor.

#### Scenario: Non-streaming final response
- **WHEN** a client calls `POST /ai/v1/chat` with a message
- **THEN** the server returns `{"reply": "...", "reasoning": "...", "conversation_id": "..."}`
- **AND** the `reply` field contains the final generated answer
- **AND** the `reasoning` field contains the aggregated reasoning if available

### Requirement: ToolRegistry integration preserved
The state graph SHALL continue to use the existing `ToolRegistry` for tool execution and result formatting.

#### Scenario: Tool execution through registry
- **WHEN** the `tool_executor` node runs
- **THEN** it invokes `ToolRegistry.execute(name, **args)`
- **AND** it returns the formatted string produced by the registry
- **AND** it does not bypass the registry's formatting logic

