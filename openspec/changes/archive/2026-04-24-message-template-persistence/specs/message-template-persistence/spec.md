# Spec: Message Template Persistence

## Background

当前消息模板注册表为纯内存实现，管理后台编辑的模板内容和启禁用状态在应用重启后丢失。数据库表 `tb_message_template` 已存在但未被使用。

## Requirements

### REQ-1: 启动时从数据库加载模板覆盖数据

应用启动时，`MessageTemplateRegistry` 在注册完内存模板后，从 `tb_message_template` 表读取所有记录：
- 若 `content` 字段非 null，将其作为覆盖内容存入内存缓存
- 若 `enabled` 为 false，将其编码加入禁用集合

### REQ-2: 编辑内容同步持久化到数据库

调用 `updateContent(code, content)` 时：
- 更新内存 `contentOverrides`
- 同步 upsert 数据库记录：
  - 若记录已存在，更新 `content` 字段
  - 若记录不存在，插入新记录，`name`/`subject`/`description` 取自内存 entry 元数据

### REQ-3: 启禁用状态同步持久化到数据库

调用 `setEnabled(code, enabled)` 时：
- 更新内存 `disabledCodes`
- 同步 upsert 数据库记录：
  - 若记录已存在，更新 `enabled` 字段
  - 若记录不存在，插入新记录，`content` 为 null（表示使用默认），`enabled` 为指定值

### REQ-4: Subject 持久化

`MessageTemplateRegistry` 新增 `updateSubject(code, subject)` 方法，行为与 `updateContent` 相同：更新内存缓存并同步 upsert 数据库。

`MessageTemplateAppServiceImpl.updateTemplate()` 将前端传入的 `subject` 也传递给 registry（当前被忽略）。

### REQ-5: 读取时优先使用数据库覆盖数据

`getTemplateContent(code)` 保持现有行为：优先返回 `contentOverrides`（从 DB 加载或编辑后存入），否则返回内存中的 `defaultContent`。

新增 `getTemplateSubject(code)`：优先返回数据库中的 subject 覆盖，否则返回内存 entry.subject。

## Data Model

### Existing Table: tb_message_template

| Column | Type | Notes |
|--------|------|-------|
| id | SERIAL PK | |
| code | VARCHAR(100) UNIQUE | 模板编码 |
| name | VARCHAR(100) | 模板名称（来自内存元数据）|
| subject | VARCHAR(200) | 邮件主题（可覆盖）|
| content | TEXT | 模板内容（可覆盖，null 表示使用默认）|
| description | VARCHAR(500) | 描述（来自内存元数据）|
| enabled | BOOLEAN DEFAULT TRUE | 是否启用 |

### In-Memory Entry (unchanged)

```
TemplateEntry(code, name, subject, description, variables, defaultContent)
```

## API Changes

No breaking changes. `AdminMessageTemplateController` 和前端保持不变。

`MessageTemplateAppService.updateTemplate()` 内部开始实际保存 subject。

## Test Requirements

- `MessageTemplateRegistry` 启动加载测试：预置 DB 数据，验证启动后 `getTemplateContent` 返回 DB 中的覆盖内容
- `updateContent` 持久化测试：调用后查询数据库验证记录存在且 content 正确
- `setEnabled` 持久化测试：调用后查询数据库验证 enabled 字段正确
- `updateSubject` 持久化测试：同上
- 恢复默认测试：清除 DB 覆盖后，`getTemplateContent` 返回 defaultContent
