from __future__ import annotations

from loguru import logger

_log = logger.bind(module="Conversation")


class Conversation:
    def __init__(self, system_prompt: str = "", max_tool_rounds: int = 5):
        self.messages: list[dict] = [{"role": "system", "content": system_prompt}] if system_prompt else []
        self.max_tool_rounds = max_tool_rounds

    def add_user_message(self, content: str) -> None:
        self.messages.append({"role": "user", "content": content})

    def add_assistant_message(self, content: str) -> None:
        self.messages.append({"role": "assistant", "content": content})

    def add_tool_message(self, tool_call_id: str, content: str) -> None:
        self.messages.append({"role": "tool", "content": content, "tool_call_id": tool_call_id})

    def get_messages(self) -> list[dict]:
        return self.messages

    def replace_system_prompt(self, new_prompt: str) -> None:
        if self.messages and self.messages[0]["role"] == "system":
            self.messages[0]["content"] = new_prompt
        else:
            self.messages.insert(0, {"role": "system", "content": new_prompt})

    def count_tokens_estimate(self) -> int:
        total = 0
        for msg in self.messages:
            content = msg.get("content", "")
            total += len(content) // 2 + 10
        return total

    def trim(self, max_tokens: int = 16000) -> None:
        if self.count_tokens_estimate() <= max_tokens:
            return

        keep_indices = [0]
        for i in range(len(self.messages) - 1, 0, -1):
            keep_indices.append(i)
            trimmed = [self.messages[j] for j in sorted(keep_indices)]
            total = 0
            for msg in trimmed:
                total += len(msg.get("content", "")) // 2 + 10
            if total > max_tokens:
                keep_indices.pop()
                break

        keep_indices = sorted(set(keep_indices))
        dropped = len(self.messages) - len(keep_indices)
        self.messages = [self.messages[i] for i in keep_indices]
        if dropped:
            _log.warning(f"对话上下文超出限制，已丢弃 {dropped} 条消息")

    def clear(self) -> None:
        system = self.messages[0] if self.messages and self.messages[0]["role"] == "system" else None
        self.messages = [system] if system else []

    def copy_messages(self) -> list[dict]:
        return [dict(m) for m in self.messages]
