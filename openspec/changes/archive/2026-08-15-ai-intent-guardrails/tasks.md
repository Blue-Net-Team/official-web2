## 1. Configuration

- [x] 1.1 Add intent-specific LLM settings to `src/ai-service/setting.py` (`INTENT_LLM_PROVIDER`, `INTENT_LLM_MODEL`, `INTENT_LLM_TEMPERATURE`, optional guard enable flag)
- [x] 1.2 Verify `LLMFactory.create()` accepts provider/model override parameters for the classifier
- [x] 1.3 Update `docker/.env.example` with new `TBD_RAG_INTENT_*` environment variables

## 2. Core Intent Classification

- [x] 2.1 Create `src/ai-service/agent/intent.py` with `IntentResult` Pydantic model (intent, confidence, reason, action)
- [x] 2.2 Implement `IntentClassifier` class using LangChain `with_structured_output`
- [x] 2.3 Define intent taxonomy constants and action mapping in `agent/intent.py`
- [x] 2.4 Add classification system prompt with role description and few-shot examples for boundary cases
- [x] 2.5 Implement parse-failure fallback returning a clarification message (`action=DIRECT`)
- [x] 2.6 Add logging for every classification result (intent, confidence, action, reason)

## 3. Reply Generators

- [x] 3.1 Add refusal message system prompt in `agent/prompts.py` (must not answer original question, mention service scope)
- [x] 3.2 Add direct-reply message system prompt in `agent/prompts.py` for greetings/chitchat
- [x] 3.3 Implement `_generate_refusal()` and `_generate_direct_reply()` helpers in `RagAgent`
- [x] 3.4 Ensure generated replies are streamed via `StreamChunk(type="content")` in `chat_stream()`

## 4. RagAgent Integration

- [x] 4.1 Initialize `IntentClassifier` in `RagAgent.__init__` using dedicated or default LLM config
- [x] 4.2 Add intent classification step at the start of `RagAgent.chat()` before invoking the graph
- [x] 4.3 Add intent classification step at the start of `RagAgent.chat_stream()` before invoking the graph
- [x] 4.4 For `RETRIEVE` action: keep existing graph flow unchanged
- [x] 4.5 For `REFUSE` action: skip graph, generate refusal, append user/assistant messages to conversation, emit `done`
- [x] 4.6 For `DIRECT` action: skip graph, generate direct reply, append user/assistant messages to conversation, emit `done`
- [x] 4.7 Handle classification exceptions with the experience-first fallback (clarification reply)

## 5. Prompt Engineering & Examples

- [x] 5.1 Draft few-shot examples distinguishing `ASSESSMENT_PROCESS` from `BLOCKED_ASSESSMENT_CONTENT`
- [x] 5.2 Draft few-shot examples distinguishing `SOFTWARE_DOWNLOAD` from `BLOCKED_TECH_SUPPORT`
- [x] 5.3 Draft few-shot examples for `BLOCKED_DEPLOYMENT`, `BLOCKED_SECURITY`, `BLOCKED_IRRELEVANT`, `GREETING`
- [x] 5.4 Review and refine classifier prompt to reduce false positives/negatives

## 6. Testing

- [x] 6.1 Write unit tests for `IntentClassifier` covering all allowed intents
- [x] 6.2 Write unit tests for `IntentClassifier` covering all blocked intents
- [x] 6.3 Write unit tests for `IntentClassifier` covering `GREETING` / `DIRECT`
- [x] 6.4 Write unit tests for parse-failure fallback behavior
- [x] 6.5 Write unit tests for `RagAgent` ensuring blocked requests do not call the graph
- [x] 6.6 Write unit tests for `RagAgent` ensuring allowed requests still call the graph
- [x] 6.7 Run `pytest` in `src/ai-service` and ensure all new tests pass

## 7. Verification & Deployment

- [x] 7.1 Run local API server and test `/ai/v1/chat/stream` with allowed / blocked / greeting inputs
- [x] 7.2 Verify SSE event sequence remains valid for refused and direct replies (`content` + `done`)
- [x] 7.3 Build `bluenet-ai-service` Docker image successfully
- [x] 7.4 Update deployment documentation with new environment variables
- [x] 7.5 Run `ruff check` and `ruff format` on modified Python files
