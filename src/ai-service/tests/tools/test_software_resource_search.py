"""software_resource_search 工具单元测试。"""

from __future__ import annotations

import httpx
import pytest
from tools.software_resource_search import (
    _resolve_direction,
    software_resource_search,
)


class TestResolveDirection:
    """方向映射测试。"""

    def test_resolve_english_enum_value(self):
        assert _resolve_direction("COMPUTER_VISION") == "COMPUTER_VISION"
        assert _resolve_direction("STRUCTURAL_DESIGN") == "STRUCTURAL_DESIGN"
        assert _resolve_direction("EMBEDDED") == "EMBEDDED"
        assert _resolve_direction("GENERAL") == "GENERAL"

    def test_resolve_chinese_label(self):
        assert _resolve_direction("视觉方向") == "COMPUTER_VISION"
        assert _resolve_direction("计算机视觉") == "COMPUTER_VISION"
        assert _resolve_direction("视觉") == "COMPUTER_VISION"
        assert _resolve_direction("结构方向") == "STRUCTURAL_DESIGN"
        assert _resolve_direction("结构设计") == "STRUCTURAL_DESIGN"
        assert _resolve_direction("结构") == "STRUCTURAL_DESIGN"
        assert _resolve_direction("电控方向") == "EMBEDDED"
        assert _resolve_direction("嵌入式开发") == "EMBEDDED"
        assert _resolve_direction("嵌入式") == "EMBEDDED"
        assert _resolve_direction("电控") == "EMBEDDED"
        assert _resolve_direction("通用") == "GENERAL"
        assert _resolve_direction("通用方向") == "GENERAL"

    def test_resolve_unknown_direction_returns_none(self):
        assert _resolve_direction("不存在") is None
        assert _resolve_direction("") is None

    def test_resolve_none_returns_none(self):
        assert _resolve_direction(None) is None

    def test_resolve_strips_whitespace(self):
        assert _resolve_direction("  视觉方向  ") == "COMPUTER_VISION"


class TestSoftwareResourceSearch:
    """工具端到端测试，使用 monkeypatch 模拟 HTTP 客户端。"""

    @pytest.fixture(autouse=True)
    def stub_backend_url(self, monkeypatch):
        """固定后端地址，避免读取真实配置。"""
        from setting import settings

        monkeypatch.setattr(settings, "BACKEND_API_URL", "http://test-backend:8080")

    def _make_fake_response(self, status_code: int, json_payload: dict | None = None):
        class FakeResponse:
            def __init__(self):
                self.status_code = status_code
                self._json = json_payload

            def raise_for_status(self):
                if self.status_code >= 400:
                    raise Exception(f"HTTP {self.status_code}")

            def json(self):
                return self._json

        return FakeResponse()

    def test_success_returns_formatted_resources(self, monkeypatch):
        captured_url = None

        class FakeClient:
            def __init__(self, timeout):
                self.timeout = timeout

            def __enter__(self):
                return self

            def __exit__(self, *args):
                return False

            def get(self, url):
                nonlocal captured_url
                captured_url = url
                return self._make_fake_response(
                    200,
                    {
                        "data": {
                            "totalElements": 1,
                            "content": [
                                {
                                    "name": "Git",
                                    "direction": "GENERAL",
                                    "category": "版本控制",
                                    "description": "分布式版本控制工具",
                                    "externalUrl": "https://git-scm.com",
                                }
                            ],
                        }
                    },
                )

            def _make_fake_response(self, status_code, json_payload):
                return TestSoftwareResourceSearch._make_fake_response(self, status_code, json_payload)

        monkeypatch.setattr(
            "tools.software_resource_search._create_client",
            lambda: FakeClient(timeout=10.0),
        )

        result = software_resource_search("git", "通用")

        assert "Git" in result
        assert "通用" in result
        assert "https://git-scm.com" in result
        assert captured_url is not None
        assert "keyword=git" in captured_url
        assert "direction=GENERAL" in captured_url

    def test_no_results_returns_friendly_message(self, monkeypatch):
        class FakeClient:
            def __enter__(self):
                return self

            def __exit__(self, *args):
                return False

            def get(self, url):
                return TestSoftwareResourceSearch._make_fake_response(
                    self, 200, {"data": {"totalElements": 0, "content": []}}
                )

        monkeypatch.setattr(
            "tools.software_resource_search._create_client",
            lambda: FakeClient(),
        )

        result = software_resource_search("不存在的软件")
        assert "未找到" in result

    def test_timeout_returns_unavailable_message(self, monkeypatch):
        class FakeClient:
            def __enter__(self):
                return self

            def __exit__(self, *args):
                return False

            def get(self, url):
                raise httpx.TimeoutException("timeout")

        monkeypatch.setattr(
            "tools.software_resource_search._create_client",
            lambda: FakeClient(),
        )

        result = software_resource_search("git")
        assert "暂不可用" in result

    def test_http_error_returns_unavailable_message(self, monkeypatch):
        class FakeClient:
            def __enter__(self):
                return self

            def __exit__(self, *args):
                return False

            def get(self, url):
                raise httpx.HTTPError("server error")

        monkeypatch.setattr(
            "tools.software_resource_search._create_client",
            lambda: FakeClient(),
        )

        result = software_resource_search("git")
        assert "暂不可用" in result

    def test_unknown_error_returns_retry_message(self, monkeypatch):
        class FakeClient:
            def __enter__(self):
                return self

            def __exit__(self, *args):
                return False

            def get(self, url):
                raise ValueError("unexpected")

        monkeypatch.setattr(
            "tools.software_resource_search._create_client",
            lambda: FakeClient(),
        )

        result = software_resource_search("git")
        assert "异常" in result
