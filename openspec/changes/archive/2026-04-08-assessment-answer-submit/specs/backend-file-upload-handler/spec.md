## MODIFIED Requirements

### Requirement: 上传考题作品
系统 SHALL 提供接口 `POST /api/v1/file/upload/assessment/work`，接收参数 `questionId`（而非 answerId）和 `file`。系统 SHALL 校验：
1. 用户已认证
2. 题目存在
3. 用户方向与题目所属考核方向匹配

上传成功后，文件存储到 MinIO 的 `work` 桶，返回 FileInfo（含 fileId）。

#### Scenario: 正常上传作品文件
- **WHEN** 已认证用户 POST `/api/v1/file/upload/assessment/work` params=`{questionId: 1, file: my-project.zip}`
- **THEN** 文件存储到 MinIO work 桶，tb_file 新增记录，返回 200 + FileInfo

#### Scenario: 题目不存在
- **WHEN** POST params=`{questionId: 9999, file: ...}`
- **THEN** 返回 404 错误，提示"题目不存在"

#### Scenario: 方向不匹配
- **WHEN** 用户的 direction 与题目所属考核的 direction 不一致
- **THEN** 返回 403 错误，提示"方向不匹配"

#### Scenario: 未认证用户
- **WHEN** 未认证用户调用上传接口
- **THEN** 返回 401 错误
