## ADDED Requirements

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

## MODIFIED Requirements

None.

## REMOVED Requirements

None.
