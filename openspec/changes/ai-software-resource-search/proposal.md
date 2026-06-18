## Why

软件资源库已经上线，但考生在前端 /resources 页面之外，无法在 AI 智能客服对话中获取下载地址。为了让考生在咨询方向、报名、考核准备时直接通过 AI 对话拿到当前有效的软件下载链接，需要让 AI Service 具备查询后端软件资源库的能力。

## What Changes

- 后端公开接口 `/api/v1/software-resources` 增加 `keyword` 查询参数，支持按资源名称、分类、描述做不区分大小写的模糊搜索。
- AI Service 新增可调用的工具 `software_resource_search(query, direction)`，内部调用后端公开接口。
- AI Service 的配置中新增后端 API 地址 `TBD_RAG_BACKEND_API_URL`。
- 更新 AI Service 的 system prompt，明确在用户询问软件下载、安装工具、方向推荐软件时使用该工具。
- 更新 Docker Compose 与 `.env` 模板，注入后端 API 地址。
- 后端与 AI Service 分别补充单元/集成测试。

## Capabilities

### New Capabilities

- `backend-software-resource-search`：后端软件资源关键字搜索能力，公开接口按关键词、方向筛选已启用资源。
- `ai-software-resource-tool`：AI Service 查询软件资源的工具能力，包含方向映射、结果格式化、失败降级。

### Modified Capabilities

- 无现有 spec 的需求变更。

## Impact

- **后端**：`SoftwareResourceListRequestDTO`、`SoftwareResourceMapper`（XML/接口）、`SoftwareResourceRepository`、`SoftwareResourceAppService`、`SoftwareResourceController` 及其测试。
- **AI Service**：`setting.py`、`tools/__init__.py`、新增 `tools/software_resource_search.py`、`agent/prompts.py`、环境配置与测试。
- **基础设施**：`docker/.env.example`、`docker/docker-compose.yml`、`src/ai-service/.env.example`。
- **无破坏性变更**：现有 `/api/v1/software-resources` 接口新增可选参数，返回结构不变。
