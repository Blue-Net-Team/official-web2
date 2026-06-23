"""基于 LangGraph StateGraph 的 RAG Agent 工作流。

本模块将原来 ``RagAgent._run_two_stage_loop`` 中的手写循环替换为显式状态图，
包含预披露、LLM 决策、工具执行、最终生成等节点，以及阶段跳转的条件边。
同步调用（``graph.invoke``）与流式调用（``graph.stream``）复用同一套节点定义。

注意：本文件不使用 ``from __future__ import annotations``，
因为 LangGraph 需要通过参数注解判断是否为节点注入 ``StreamWriter``。
"""

from typing import TypedDict

from langchain_core.runnables import RunnableConfig
from langgraph.graph import END, StateGraph
from langgraph.types import StreamWriter
from loguru import logger

from llm_providers.base import LLMProvider
from tools import ToolRegistry
from tools.tag_search import tag_generate
from tools.tag_search_detailed import tag_search_detailed

_log = logger.bind(module="RagGraph")

_MAX_TAG_ROUNDS = 4
_MAX_CHUNK_ROUNDS = 3
_MAX_FALLBACK_ROUNDS = 1


class AgentState(TypedDict):
    """LangGraph 状态图状态。"""

    messages: list[dict]
    tag_rounds: int
    chunk_rounds: int
    fallback_rounds: int
    final_content: str
    final_reasoning: str


# ---------------------------------------------------------------------------
# 节点
# ---------------------------------------------------------------------------


def _build_pre_disclosure(user_input: str) -> str:
    """生成预披露文本：自动标签生成 + 标签详细检索。"""
    _log.info("预披露阶段: 自动生成初始标签并检索")
    try:
        pre_tags = tag_generate(user_input)
        _log.info(f"初始标签: {pre_tags}")
    except Exception as e:
        _log.warning(f"标签生成失败: {e}")
        pre_tags = []

    pre_tag_results = []
    if pre_tags:
        try:
            pre_tag_results = tag_search_detailed(" ".join(pre_tags), top_k=10)
            _log.info(f"初始标签检索结果: {len(pre_tag_results)} 项")
        except Exception as e:
            _log.warning(f"标签检索失败: {e}")

    lines = [f"用户问题: {user_input}", ""]
    if pre_tag_results:
        lines.append("=== 初始标签检索结果 ===")
        if pre_tags:
            lines.append(f"自动生成的标签: {', '.join(pre_tags)}")
            lines.append("")
        for i, r in enumerate(pre_tag_results, 1):
            desc = f" - {r.tag_description[:60]}" if r.tag_description else ""
            lines.append(
                f"  [{i}] {r.tag_name:<12} score={r.relevance_score:.4f}  docs={r.chunks_count}{desc}"
            )
    else:
        lines.append("未找到相关标签，请尝试搜索。")

    lines.append("")
    lines.append("请按两阶段工作流执行：")
    lines.append("  1. 如果标签不充分，调用 tag_search_detailed 扩展（最多3轮）")
    lines.append("  2. 标签充分后，选择标签子集")
    lines.append("  3. 调用 chunk_search_by_tags 检索分片（最多3轮）")
    lines.append(
        "  4. 如果 chunk_search_by_tags 诊断显示'标签均不在库中'，调用 chunk_search(query) 进行兜底语义搜索（最多1轮）"
    )
    lines.append("  5. 基于结果生成答案")
    return "\n".join(lines)


def pre_disclose_node(state: AgentState) -> dict:
    """预披露节点：将最后一条用户消息替换为包含初始检索结果的 enriched 消息。"""
    messages = list(state["messages"])
    last_user_idx = -1
    for i in range(len(messages) - 1, -1, -1):
        if messages[i].get("role") == "user":
            last_user_idx = i
            break

    if last_user_idx == -1:
        _log.warning("预披露节点未找到用户消息")
        return {"messages": messages}

    raw_input = messages[last_user_idx].get("content", "")
    enriched = _build_pre_disclosure(raw_input)
    messages[last_user_idx] = {"role": "user", "content": enriched}
    return {"messages": messages}


def agent_node(
    state: AgentState,
    config: RunnableConfig,
    writer: StreamWriter = None,
) -> dict:
    """Agent 决策节点：调用支持工具选择的 LLM，将 assistant 消息写回状态。

    同步与流式复用同一处理逻辑：内部统一读取 ``stream_with_tools`` 事件流，
    仅在提供 ``writer`` 时把事件外发。

    如果本轮没有产生工具调用，说明 LLM 已经给出了最终答案，直接把
    ``full_content`` 作为 ``final_content`` 返回，避免再调一次生成节点导致
    模型看到空内容 assistant 消息而输出元话语。
    """
    configurable = config.get("configurable", {})
    llm: LLMProvider = configurable.get("llm")
    tool_specs: list[dict] = configurable.get("tool_specs", [])
    if llm is None:
        raise ValueError(f"Missing 'llm' in configurable. Keys: {list(configurable.keys())}")
    messages = list(state["messages"])

    full_reasoning = ""
    full_content = ""
    pending_tool_calls: list[dict] = []

    for event in llm.stream_with_tools(messages, tool_specs):
        if event.type == "reasoning":
            _log.info(f"Agent 思考片段: {event.delta[:50]}...")
            if writer is not None:
                writer({"type": "reasoning", "content": event.delta})
            full_reasoning += event.delta
        elif event.type == "content":
            full_content += event.delta
        elif event.type == "tool_call":
            tool_call = {
                "id": event.tool_call_id or "",
                "name": event.tool_name,
                "args": event.tool_args or {},
                "type": "function",
            }
            pending_tool_calls.append(tool_call)
            # 为了实现 "reasoning → tool_call → reasoning → tool_call" 的流式结构，
            # 每轮 LLM 调用只保留并执行第一个 tool_call，剩余调用在下一轮重新决策。
            if len(pending_tool_calls) == 1 and writer is not None:
                writer({
                    "type": "tool_call",
                    "tool_name": event.tool_name,
                    "tool_args": event.tool_args or {},
                })
        elif event.type == "done":
            break

    if len(pending_tool_calls) > 1:
        _log.warning(
            f"LLM 一次返回 {len(pending_tool_calls)} 个 tool_calls，"
            "仅保留第一个以支持顺序推理，其余将在下一轮重新决策"
        )
        pending_tool_calls = [pending_tool_calls[0]]

    assistant_msg: dict = {
        "role": "assistant",
        "content": full_content,
    }
    if pending_tool_calls:
        assistant_msg["tool_calls"] = pending_tool_calls
    if full_reasoning:
        assistant_msg["reasoning_content"] = full_reasoning

    messages.append(assistant_msg)
    _log.info(
        f"Agent 决策完成, tool_calls={len(pending_tool_calls)}, reasoning_len={len(full_reasoning)}"
    )

    # 无工具调用时，当前 content 即为最终答案，直接结束并外发 content 事件
    if not pending_tool_calls:
        if writer is not None and full_content:
            writer({"type": "content", "content": full_content})
        return {
            "messages": messages,
            "final_content": full_content,
            "final_reasoning": full_reasoning,
        }

    return {"messages": messages}


def tool_executor_node(
    state: AgentState,
    writer: StreamWriter = None,
) -> dict:
    """工具执行节点：执行最后一条 assistant 消息中的 tool_calls，并将结果写回。

    若提供 ``writer``（流式调用），则为每个执行结果外发 ``tool_result`` 事件。
    """
    messages = list(state["messages"])
    assistant_msg = messages[-1]
    if assistant_msg.get("role") != "assistant":
        _log.warning("工具执行节点未找到 assistant 消息")
        return {"messages": messages}

    tool_calls = assistant_msg.get("tool_calls", [])
    if not tool_calls:
        return {"messages": messages}

    updates: dict[str, int] = {}
    new_messages: list[dict] = []

    for tc in tool_calls:
        tool_name = tc["name"]
        tool_args = tc.get("args", {})
        tool_call_id = tc.get("id", "")

        tag_rounds = state["tag_rounds"]
        chunk_rounds = state["chunk_rounds"]
        fallback_rounds = state["fallback_rounds"]

        result: str | None = None

        if tool_name == "tag_search_detailed":
            tag_rounds += 1
            if tag_rounds > _MAX_TAG_ROUNDS:
                result = f"标签搜索已达上限 {_MAX_TAG_ROUNDS} 轮，请直接基于已有标签进入选择阶段"
                _log.warning(result)
            else:
                if tag_rounds == _MAX_TAG_ROUNDS:
                    _log.info("标签搜索剩余 1 次机会")
                result = ToolRegistry.execute(tool_name, **tool_args)
            updates["tag_rounds"] = tag_rounds

        elif tool_name == "chunk_search_by_tags":
            chunk_rounds += 1
            if chunk_rounds > _MAX_CHUNK_ROUNDS:
                result = f"分片检索已达上限 {_MAX_CHUNK_ROUNDS} 轮，请基于已有检索结果生成答案"
                _log.warning(result)
            else:
                if chunk_rounds == _MAX_CHUNK_ROUNDS:
                    _log.info("分片检索剩余 1 次机会")
                result = ToolRegistry.execute(tool_name, **tool_args)
            updates["chunk_rounds"] = chunk_rounds

        elif tool_name == "chunk_search":
            fallback_rounds += 1
            if fallback_rounds > _MAX_FALLBACK_ROUNDS:
                result = f"兜底语义搜索已达上限 {_MAX_FALLBACK_ROUNDS} 轮，请基于已有检索结果生成答案"
                _log.warning(result)
            else:
                result = ToolRegistry.execute(tool_name, **tool_args)
            updates["fallback_rounds"] = fallback_rounds

        else:
            result = ToolRegistry.execute(tool_name, **tool_args)

        if result is not None:
            if writer is not None:
                writer({
                    "type": "tool_result",
                    "tool_name": tool_name,
                    "content": result,
                })
            new_messages.append({
                "role": "tool",
                "content": result,
                "tool_call_id": tool_call_id,
            })

    messages.extend(new_messages)
    _log.info(f"工具执行完成, {len(new_messages)} 个结果已写回状态")
    return {"messages": messages, **updates}


# ---------------------------------------------------------------------------
# 条件边
# ---------------------------------------------------------------------------


def should_continue(state: AgentState) -> str:
    """决定是从 agent 节点继续工具循环，还是结束工作流。

    关键约束：只要 assistant 消息包含 tool_calls，就必须先经过
    ``tool_executor_node`` 为其补充对应的 tool 消息，否则后续 LLM 调用会报
    "insufficient tool messages following tool_calls message" 错误。

    当 assistant 没有 tool_calls（或所有 tool_calls 都已补充 tool 消息）时，
    工作流结束；最终答案已在 ``agent_node`` 中生成并写入
    ``state["final_content"]``。
    """
    messages = state["messages"]
    assistant_msg = messages[-1] if messages else {}
    if assistant_msg.get("role") != "assistant":
        return "continue"

    tool_calls = assistant_msg.get("tool_calls", [])
    if not tool_calls:
        return "end"

    # 检查当前 assistant 的每个 tool_call 是否都已有对应的 tool 消息
    tool_call_ids = {tc.get("id") for tc in tool_calls}
    found_tool_ids: set[str] = set()
    for msg in reversed(messages[:-1]):
        if msg.get("role") == "tool":
            found_tool_ids.add(msg.get("tool_call_id"))
        else:
            break

    if tool_call_ids.issubset(found_tool_ids):
        return "end"

    return "continue"


# ---------------------------------------------------------------------------
# 图构建
# ---------------------------------------------------------------------------


def build_rag_graph(llm: LLMProvider, tool_specs: list[dict]) -> StateGraph:
    """构建 RAG Agent 状态图（未编译）。"""
    builder = StateGraph(AgentState)

    builder.add_node("pre_disclose", pre_disclose_node)
    builder.add_node("agent", agent_node)
    builder.add_node("tool_executor", tool_executor_node)

    builder.set_entry_point("pre_disclose")
    builder.add_edge("pre_disclose", "agent")
    builder.add_conditional_edges(
        "agent",
        should_continue,
        {"continue": "tool_executor", "end": END},
    )
    builder.add_edge("tool_executor", "agent")

    return builder
