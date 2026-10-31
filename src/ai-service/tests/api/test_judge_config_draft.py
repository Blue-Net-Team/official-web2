"""AI 判题配置草稿的模型、生成服务和独立路由测试。"""

from __future__ import annotations

import json
from unittest.mock import MagicMock

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient
from pydantic import ValidationError

from api.judge_config_draft import (
    JudgeConfigDraft,
    JudgeConfigDraftGenerationError,
    JudgeConfigDraftGenerator,
    JudgeConfigDraftRequest,
    get_judge_config_draft_generator,
    router,
)


def valid_request_data() -> dict:
    return {
        "content": "给定 n 个整数，求和。",
        "inputDescription": "第一行 n，第二行 n 个整数。",
        "outputDescription": "输出整数和。",
        "constraints": "1 <= n <= 100000",
    }


def valid_draft_data() -> dict:
    return {
        "examples": [
            {"input": "3\n1 2 3\n", "expectedOutput": "6\n", "explanation": "基础求和"},
            {"input": "1\n-2\n", "expectedOutput": "-2\n"},
        ],
        "runTestCases": [
            {"input": "2\n4 5\n", "expectedOutput": "9\n", "hidden": False, "weight": 1},
            {"input": "1\n0\n", "expectedOutput": "0\n", "hidden": False, "weight": 1},
        ],
        "generatorLanguage": "python",
        "generatorSource": "import json, sys\nargs = json.load(sys.stdin)\nprint(args['n'])\n",
        "testcases": [
            {
                "caseNo": index,
                "category": category,
                "generatorArgs": {"n": index, "seed": 100 + index},
                "weight": 1,
                "hidden": True,
                "sample": False,
                "description": f"覆盖场景 {index}",
            }
            for index, category in enumerate(
                ["MINIMUM", "EDGE", "NORMAL", "RANDOM", "MAXIMUM", "WORST_CASE"], start=1
            )
        ],
    }


class TestJudgeConfigDraftModels:
    def test_valid_contract_uses_camel_case(self):
        request = JudgeConfigDraftRequest.model_validate(valid_request_data())
        draft = JudgeConfigDraft.model_validate(valid_draft_data())

        assert request.input_description.startswith("第一行")
        assert draft.generator_language == "python"
        assert len(draft.examples) == 2
        assert draft.testcases[-1].generator_args["seed"] == 106

    @pytest.mark.parametrize("field", list(valid_request_data()))
    def test_statement_fields_are_required_and_non_blank(self, field: str):
        data = valid_request_data()
        data[field] = "  "
        with pytest.raises(ValidationError):
            JudgeConfigDraftRequest.model_validate(data)

    def test_statement_fields_have_length_limit(self):
        data = valid_request_data()
        data["content"] = "x" * 20001
        with pytest.raises(ValidationError):
            JudgeConfigDraftRequest.model_validate(data)

    @pytest.mark.parametrize(
        ("field", "size"),
        [("examples", 1), ("examples", 4), ("runTestCases", 1), ("runTestCases", 5), ("testcases", 5), ("testcases", 11)],
    )
    def test_output_collection_sizes_are_bounded(self, field: str, size: int):
        data = valid_draft_data()
        original = data[field]
        data[field] = [original[index % len(original)] for index in range(size)]
        if field == "testcases":
            data[field] = [{**item, "caseNo": index + 1} for index, item in enumerate(data[field])]
        with pytest.raises(ValidationError):
            JudgeConfigDraft.model_validate(data)

    def test_case_numbers_must_be_contiguous(self):
        data = valid_draft_data()
        data["testcases"][2]["caseNo"] = 9
        with pytest.raises(ValidationError, match="caseNo"):
            JudgeConfigDraft.model_validate(data)

    def test_category_must_be_supported(self):
        data = valid_draft_data()
        data["testcases"][0]["category"] = "UNKNOWN"
        with pytest.raises(ValidationError):
            JudgeConfigDraft.model_validate(data)

    @pytest.mark.parametrize(
        ("field", "value"),
        [("weight", 2), ("hidden", False), ("sample", True), ("generatorArgs", "n=1")],
    )
    def test_formal_case_defaults_and_args_are_enforced(self, field: str, value):
        data = valid_draft_data()
        data["testcases"][0][field] = value
        with pytest.raises(ValidationError):
            JudgeConfigDraft.model_validate(data)

    def test_generator_must_be_python(self):
        data = valid_draft_data()
        data["generatorLanguage"] = "cpp"
        with pytest.raises(ValidationError):
            JudgeConfigDraft.model_validate(data)


class TestJudgeConfigDraftGenerator:
    def test_structured_output_is_validated_and_returned(self):
        llm = MagicMock()
        structured = MagicMock()
        structured.invoke.return_value = JudgeConfigDraft.model_validate(valid_draft_data())
        llm._llm.with_structured_output.return_value = structured
        request = JudgeConfigDraftRequest.model_validate(valid_request_data())

        result = JudgeConfigDraftGenerator(llm=llm).generate(request)

        assert result == structured.invoke.return_value
        llm._llm.with_structured_output.assert_called_once_with(JudgeConfigDraft)
        llm.invoke.assert_not_called()

    def test_json_fallback_accepts_markdown_fence(self):
        llm = MagicMock(spec=["invoke"])
        llm.invoke.return_value = f"```json\n{json.dumps(valid_draft_data(), ensure_ascii=False)}\n```"

        result = JudgeConfigDraftGenerator(llm=llm).generate(
            JudgeConfigDraftRequest.model_validate(valid_request_data())
        )

        assert result.examples[0].expected_output == "6\n"

    def test_non_json_fallback_is_rejected(self):
        llm = MagicMock(spec=["invoke"])
        llm.invoke.return_value = "无法生成"

        with pytest.raises(JudgeConfigDraftGenerationError, match="JSON"):
            JudgeConfigDraftGenerator(llm=llm).generate(
                JudgeConfigDraftRequest.model_validate(valid_request_data())
            )

    def test_invalid_model_output_is_rejected(self):
        llm = MagicMock(spec=["invoke"])
        invalid = valid_draft_data()
        invalid["testcases"][0]["caseNo"] = 8
        llm.invoke.return_value = json.dumps(invalid)

        with pytest.raises(JudgeConfigDraftGenerationError, match="校验"):
            JudgeConfigDraftGenerator(llm=llm).generate(
                JudgeConfigDraftRequest.model_validate(valid_request_data())
            )

    def test_invalid_output_is_repaired_once(self):
        llm = MagicMock(spec=["invoke"])
        invalid = valid_draft_data()
        original = invalid["testcases"]
        invalid["testcases"] = [
            {**original[index % len(original)], "caseNo": index + 1} for index in range(14)
        ]
        llm.invoke.side_effect = [json.dumps(invalid), json.dumps(valid_draft_data())]

        result = JudgeConfigDraftGenerator(llm=llm).generate(
            JudgeConfigDraftRequest.model_validate(valid_request_data())
        )

        assert len(result.testcases) == 6
        assert llm.invoke.call_count == 2
        repair_request = llm.invoke.call_args_list[1].args[0][-1]["content"]
        assert "最多 10 个" in repair_request

    def test_repair_request_covers_run_case_limit(self):
        llm = MagicMock(spec=["invoke"])
        invalid = valid_draft_data()
        run_case = invalid["runTestCases"][0]
        invalid["runTestCases"] = [run_case] * 6
        llm.invoke.side_effect = [json.dumps(invalid), json.dumps(valid_draft_data())]

        result = JudgeConfigDraftGenerator(llm=llm).generate(
            JudgeConfigDraftRequest.model_validate(valid_request_data())
        )

        assert len(result.run_test_cases) == 2
        repair_request = llm.invoke.call_args_list[1].args[0][-1]["content"]
        assert "runTestCases 最多 4 个" in repair_request
        assert "examples 为 2～3 个" in repair_request

    def test_provider_failure_is_wrapped(self):
        llm = MagicMock(spec=["invoke"])
        llm.invoke.side_effect = RuntimeError("provider down")

        with pytest.raises(JudgeConfigDraftGenerationError, match="失败") as exc_info:
            JudgeConfigDraftGenerator(llm=llm).generate(
                JudgeConfigDraftRequest.model_validate(valid_request_data())
            )
        assert isinstance(exc_info.value.__cause__, RuntimeError)


class TestJudgeConfigDraftApi:
    def make_client(self, generator) -> TestClient:
        app = FastAPI()
        app.include_router(router, prefix="/ai/v1")
        app.dependency_overrides[get_judge_config_draft_generator] = lambda: generator
        return TestClient(app)

    def test_success_returns_structured_draft(self):
        generator = MagicMock()
        generator.generate.return_value = JudgeConfigDraft.model_validate(valid_draft_data())
        client = self.make_client(generator)

        response = client.post("/ai/v1/judge-config-drafts", json=valid_request_data())

        assert response.status_code == 200
        assert response.json()["generatorLanguage"] == "python"
        assert len(response.json()["testcases"]) == 6

    def test_invalid_request_does_not_call_generator(self):
        generator = MagicMock()
        client = self.make_client(generator)
        data = valid_request_data()
        data["constraints"] = ""

        response = client.post("/ai/v1/judge-config-drafts", json=data)

        assert response.status_code == 422
        generator.generate.assert_not_called()

    def test_generation_failure_becomes_bad_gateway(self):
        generator = MagicMock()
        generator.generate.side_effect = JudgeConfigDraftGenerationError("模型输出无效")
        client = self.make_client(generator)

        response = client.post("/ai/v1/judge-config-drafts", json=valid_request_data())

        assert response.status_code == 502
        assert response.json()["detail"] == "模型输出无效"
