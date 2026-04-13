## ADDED Requirements

### Requirement: 管理员创建介绍图片接口
系统 SHALL 提供接口 `POST /api/v1/admin/introduce-images`，允许管理员通过 fileId 创建介绍图片记录。

#### Scenario: 成功创建介绍图片
- **WHEN** 管理员 POST `/api/v1/admin/introduce-images` body=`{fileId: 100, type: "LABORATORY", description: "实验室环境"}`
- **THEN** 系统 SHALL 校验 fileId 对应文件存在且类型为 NORMAL_IMG
- **AND** 系统 SHALL 创建 tb_introduce_image 记录（type、fileId、description）
- **AND** 返回 200 + 创建的介绍图片信息

#### Scenario: 文件不存在
- **WHEN** POST body=`{fileId: 9999, type: "LABORATORY"}`
- **THEN** 返回 404 错误

#### Scenario: 文件类型不匹配
- **WHEN** POST body=`{fileId: 100, type: "LABORATORY"}` 但文件类型不是 NORMAL_IMG
- **THEN** 返回 400 错误

#### Scenario: 未认证用户
- **WHEN** 未认证用户 POST `/api/v1/admin/introduce-images`
- **THEN** 返回 401 错误

### Requirement: 管理员删除介绍图片接口
系统 SHALL 提供接口 `DELETE /api/v1/admin/introduce-images/{id}`，允许管理员删除介绍图片。

#### Scenario: 成功删除介绍图片
- **WHEN** 管理员 DELETE `/api/v1/admin/introduce-images/1`
- **THEN** 系统 SHALL 删除 tb_introduce_image 记录
- **AND** 系统 SHALL 删除关联的文件记录和 MinIO 对象
- **AND** 返回 200
