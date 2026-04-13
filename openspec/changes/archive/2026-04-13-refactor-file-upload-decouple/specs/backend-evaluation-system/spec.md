## MODIFIED Requirements

### Requirement: 答案提交 fileId 校验
系统 SHALL 在创建和更新答案时校验 fileId 的有效性。当 fileId 不为 null 时，系统 SHALL 验证文件存在且类型为 WORK。

#### Scenario: fileId 对应文件不存在
- **WHEN** 用户提交答案 fileId=9999，但 tb_file 中不存在该记录
- **THEN** 系统 SHALL 返回 400 错误，提示"文件不存在"

#### Scenario: fileId 类型不是 WORK
- **WHEN** 用户提交答案 fileId=100，但文件类型为 AVATAR
- **THEN** 系统 SHALL 返回 400 错误，提示"文件类型不匹配"

#### Scenario: fileId 为 null 且 content 有值
- **WHEN** 用户提交答案 fileId=null, content="答案文本"
- **THEN** 系统 SHALL 正常创建答案（非文件题）

#### Scenario: 更新答案时 fileId 校验
- **WHEN** 用户更新答案 fileId=200，但文件类型非 WORK
- **THEN** 系统 SHALL 返回 400 错误，提示"文件类型不匹配"

### Requirement: 答案提交方向匹配校验
系统 SHALL 在创建和更新答案时校验用户方向与题目所属考核方向一致。

#### Scenario: 方向匹配正常提交
- **WHEN** 用户方向=FRONTEND，题目考核方向=FRONTEND
- **THEN** 系统 SHALL 正常创建/更新答案

#### Scenario: 方向不匹配
- **WHEN** 用户方向=FRONTEND，题目考核方向=BACKEND
- **THEN** 系统 SHALL 返回 403 错误，提示"方向不匹配"
