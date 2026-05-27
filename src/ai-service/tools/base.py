from dataclasses import dataclass, field
from typing import Any, Callable


@dataclass
class ToolDefinition:
    name: str
    description: str
    parameters: dict
    handler: Callable[..., Any]
    required_params: list[str] = field(default_factory=lambda: ["query"])


@dataclass
class TagSearchResult:
    tag_name: str
    relevance_score: float
    chunks_count: int
    tag_description: str = ""
