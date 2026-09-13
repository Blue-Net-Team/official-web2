## MODIFIED Requirements

### Requirement: Tool round limits enforced by state graph
The state graph SHALL enforce the same per-tool round limits as the existing `RagAgent`: tag search up to 4 rounds, chunk search by tags up to 3 rounds, fallback chunk search up to 1 round, software resource listing up to 1 round, and software resource lookup up to 2 rounds.

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

#### Scenario: Software resource list limit reached
- **WHEN** the LLM requests `software_resource_list` and `software_list_rounds` is already 1
- **THEN** the `tool_executor` returns a limit message instead of executing the tool
- **AND** the message instructs the model to proceed with the results already obtained
- **AND** the graph routes back to `agent`

#### Scenario: Software resource lookup limit reached
- **WHEN** the LLM requests `software_resource_lookup` and `software_lookup_rounds` is already 2
- **THEN** the `tool_executor` returns a limit message instead of executing the tool
- **AND** the message instructs the model to proceed with the results already obtained
- **AND** the graph routes back to `agent`

#### Scenario: Round counters are independent
- **WHEN** `software_resource_list` has been called once and the LLM requests `software_resource_lookup`
- **THEN** the lookup request SHALL be executed for as long as `software_lookup_rounds` is below 2
