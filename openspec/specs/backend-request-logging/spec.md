## 新增需求

### Requirement: HTTP 请求日志记录
系统 SHALL 在控制层处理完成后记录每个HTTP请求的基本信息。

#### Scenario: 请求日志记录
- **WHEN** HTTP 请求处理完成
- **THEN** 系统 SHALL 记录请求方法（例如 GET、POST、PUT、DELETE）
- **THEN** 系统 SHALL 记录请求 URI（完整路径，包括查询参数）
- **THEN** 系统 SHALL 记录 HTTP 响应状态码和状态文本
- **THEN** 日志格式 SHALL 为：`{方法} {URI} {状态码} {状态文本}`，例如：`GET /api/v1/users 200 OK`

#### Scenario: 错误请求日志记录
- **WHEN** HTTP 请求导致错误响应（4xx 或 5xx 状态）
- **THEN** 系统 SHALL 以相同格式记录错误请求，例如：`POST /api/v1/login 401 Unauthorized`

#### Scenario: 日志级别
- **WHEN** 请求处理成功（2xx 状态码）
- **THEN** 系统 SHALL 在 INFO 级别记录日志
- **WHEN** 请求导致客户端错误（4xx 状态码）
- **THEN** 系统 SHALL 在 WARN 级别记录日志
- **WHEN** 请求导致服务器错误（5xx 状态码）
- **THEN** 系统 SHALL 在 ERROR 级别记录日志