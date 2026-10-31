## ADDED Requirements

### Requirement: 请求 DTO 校验约束必须具有显式消息

API 请求 DTO 中使用的 Jakarta Validation 约束 SHALL 配置非空白的 `message`，且消息 MUST 能识别对应业务字段；数值范围约束的消息 MUST 表明有效边界，具有明确单位的字段 MUST 在消息中体现单位。

#### Scenario: 必填字段未提供

- **WHEN** 请求中的 `answerId`、判题配置字段、知识库标签描述或 Markdown 文件等必填值未提供
- **THEN** 校验消息明确指出对应业务字段不能为空，而非仅返回“不能为空”

#### Scenario: 数值字段超出允许范围

- **WHEN** 判题资源限制、判题配置数值或软件资源分页参数不满足 `Min`、`Max` 或 `DecimalMin` 约束
- **THEN** 校验消息明确指出对应字段及其有效数值边界

#### Scenario: 新增校验约束遗漏消息

- **WHEN** 自动化约定测试发现 `api.dto` 下任何 Jakarta Validation 约束未配置非空白 `message`
- **THEN** 测试失败并指出需要补充消息的 DTO 和约束

### Requirement: 校验失败响应保持现有接口契约

请求参数校验失败时，系统 SHALL 继续使用现有 HTTP 400 和 `ResponseMessage` 响应结构，并 SHALL 将 DTO 中配置的字段级消息放入 `msg`；系统 MUST NOT 因本次修改改变校验条件或成功请求行为。

#### Scenario: Controller 请求体校验失败

- **WHEN** 客户端向受影响接口提交缺少必填字段或字段越界的请求
- **THEN** 接口返回 HTTP 400
- **AND** 响应 `msg` 为该字段配置的明确中文提示

#### Scenario: 合法请求通过校验

- **WHEN** 客户端提交满足全部既有校验约束的请求
- **THEN** 请求继续按原有业务流程处理，响应行为不因校验消息补充而变化
