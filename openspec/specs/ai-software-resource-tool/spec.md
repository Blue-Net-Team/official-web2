## ADDED Requirements

### Requirement: AI Service 提供软件资源查询工具
AI Service SHALL 向 `ToolRegistry` 注册一个名为 `software_resource_search` 的工具，供 `RagAgent` 在对话中调用。

#### Scenario: 工具被注册到注册表
- **WHEN** AI Service 启动并导入 tools 模块
- **THEN** `ToolRegistry` 中存在名为 `software_resource_search` 的工具定义

#### Scenario: 工具通过名称执行
- **WHEN** 调用 `ToolRegistry.execute("software_resource_search", query="git")`
- **THEN** 返回格式化的软件资源列表文本

### Requirement: 工具接受中文方向标签和后端枚举值
工具的 `direction` 参数 SHALL 同时接受中文方向标签（如"视觉方向"）和后端枚举值（如 `COMPUTER_VISION`）。无法识别时 SHALL 按无方向处理。

#### Scenario: 中文方向标签映射
- **WHEN** 调用工具参数 `direction="视觉方向"`
- **THEN** 向后端请求时转换为 `direction=COMPUTER_VISION`

#### Scenario: 枚举值直接透传
- **WHEN** 调用工具参数 `direction="STRUCTURAL_DESIGN"`
- **THEN** 向后端请求时保持 `direction=STRUCTURAL_DESIGN`

#### Scenario: 未知方向降级为全库搜索
- **WHEN** 调用工具参数 `direction="不明方向"`
- **THEN** 向后端请求时不传递 `direction` 参数

### Requirement: 工具输出包含下载地址
工具返回的文本 SHALL 包含资源名称、方向、分类、描述和外部下载地址。

#### Scenario: 返回单个资源
- **WHEN** 工具查询到 1 个名为 "Git" 的通用资源，下载地址为 "https://git-scm.com"
- **THEN** 返回文本中 SHALL 包含 "Git"、"通用"、"https://git-scm.com"

### Requirement: 工具对后端故障做降级处理
当后端 API 不可用时，工具 SHALL 返回友好提示，而不是抛出未处理异常。

#### Scenario: 后端超时
- **WHEN** 后端 API 在配置的超时时间内无响应
- **THEN** 工具返回 "软件资源服务暂不可用，请稍后重试" 或类似提示

### Requirement: 系统提示词引导 Agent 使用工具
`RagAgent` 的 system prompt SHALL 包含使用 `software_resource_search` 的明确指引。

#### Scenario: 用户询问软件下载
- **WHEN** 用户输入 "计算机视觉需要安装什么软件"
- **THEN** Agent 在生成最终答案前 SHALL 调用 `software_resource_search` 工具

#### Scenario: 用户询问下载地址
- **WHEN** 用户输入 "SolidWorks 在哪里下载"
- **THEN** Agent SHALL 调用 `software_resource_search` 工具并在回答中包含下载地址

### Requirement: 配置后端 API 地址
AI Service SHALL 通过环境变量 `TBD_RAG_BACKEND_API_URL` 配置后端 API 根地址。

#### Scenario: Docker 环境
- **WHEN** 环境变量 `TBD_RAG_BACKEND_API_URL=http://api-service:8080`
- **THEN** 工具 SHALL 调用 `http://api-service:8080/api/v1/software-resources`

#### Scenario: 本地开发环境
- **WHEN** 环境变量 `TBD_RAG_BACKEND_API_URL=http://localhost:8080`
- **THEN** 工具 SHALL 调用 `http://localhost:8080/api/v1/software-resources`
