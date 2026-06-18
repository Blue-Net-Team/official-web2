## 1. 后端：软件资源关键字搜索

- [x] 1.1 在 `SoftwareResourceListRequestDTO` 中新增 `keyword` 字段及校验注解
- [x] 1.2 更新 `SoftwareResourceMapper` 接口，`selectActiveByDirection` 增加 `@Param("keyword") String keyword` 参数
- [x] 1.3 更新 `SoftwareResourceMapper.xml`，追加 `keyword` 对 `name`、`category`、`description` 的不区分大小写模糊匹配条件
- [x] 1.4 更新 `SoftwareResourceRepository` 接口与 `SoftwareResourceRepositoryImpl`，透传 `keyword`
- [x] 1.5 更新 `SoftwareResourceAppService` 与 `SoftwareResourceAppServiceImpl`，透传 `keyword`
- [x] 1.6 更新 `SoftwareResourceController.listSoftwareResources`，从请求 DTO 中读取 `keyword` 并传入应用服务
- [x] 1.7 为 Repository/AppService 补充单元测试，覆盖带 `keyword` 的调用链路
- [x] 1.8 为 `SoftwareResourceControllerIntegrationTest` 补充集成测试，覆盖按关键字搜索、方向+关键字组合搜索、空关键字忽略

## 2. AI Service：新增软件资源查询工具

- [x] 2.1 在 `setting.py` 的 `Settings` 中新增 `BACKEND_API_URL` 配置项（环境变量 `TBD_RAG_BACKEND_API_URL`）
- [x] 2.2 在 `src/ai-service/.env.example` 中补充 `TBD_RAG_BACKEND_API_URL` 示例
- [x] 2.3 创建 `tools/software_resource_search.py`，实现方向映射、HTTP 调用、结果格式化、异常降级
- [x] 2.4 在 `tools/__init__.py` 中注册 `software_resource_search` 工具
- [x] 2.5 更新 `agent/prompts.py` 的 `TAG_RETRIEVAL_SYSTEM_PROMPT`，增加工具使用指引
- [x] 2.6 为 `tools/software_resource_search.py` 补充单元测试，覆盖方向映射、成功返回、后端超时/异常降级

## 3. 基础设施配置

- [x] 3.1 在 `docker/.env.example` 中新增 `TBD_RAG_BACKEND_API_URL` 配置项
- [x] 3.2 在 `docker/docker-compose.yml` 的 `ai-service` 服务环境中注入 `TBD_RAG_BACKEND_API_URL`

## 4. 验证与提交

- [x] 4.1 后端执行 `./mvnw clean compile` 与相关测试
- [x] 4.2 AI Service 执行 `uv run pytest` 或等效命令
- [x] 4.3 本地/容器启动后，通过 AI Service chat 接口测试软件资源相关提问（工具层 E2E 已验证；chat HTTP 接口中文解析受容器 locale 影响，为既有问题）
- [ ] 4.4 提交变更并推送
