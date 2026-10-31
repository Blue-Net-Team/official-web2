## Why

部分请求 DTO 的 Jakarta Validation 约束未配置 `message`，导致校验失败时仅返回“不能为空”等通用提示，调用方无法判断具体出错字段。该问题已记录为 GitHub issue #88，需要统一补齐提示并建立回归验证。

## What Changes

- 为 API 请求 DTO 中缺少 `message` 的 34 个校验约束补充明确的中文提示，覆盖必填、非空、数值上下限等校验。
- 保持现有校验规则、接口字段、HTTP 状态码及 `ResponseMessage` 响应结构不变。
- 增加针对受影响 DTO 的校验消息测试，并防止 API 请求 DTO 新增未配置消息的校验约束。
- 不修改全局异常处理器，不调整已经配置好的校验消息。

## Capabilities

### New Capabilities

- `backend-validation-messages`: 请求参数校验失败时返回能识别具体字段和约束条件的错误消息，并保证请求 DTO 的校验约束具有显式消息。

### Modified Capabilities

无。

## Impact

- 影响 `src/backend/src/main/java/com/bluenet/web/api/dto` 下 7 个请求 DTO 文件，主要涉及判题配置、考核评分、知识库和软件资源查询。
- 影响对应后端校验与 Controller 集成测试。
- 校验失败的 `msg` 内容将更准确；成功请求及其他错误响应行为不变。
- 不引入新依赖，不涉及数据库、前端或部署配置变更。
