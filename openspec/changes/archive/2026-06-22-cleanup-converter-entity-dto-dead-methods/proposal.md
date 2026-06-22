## Why

`AssessmentTimeResponseConverter` 中存在从领域实体 `AssessmentTime` 直接转换为 `AssessmentTimeDTO` 的死方法，导致 `eliminated` 等用户相关派生字段未被设置。同类问题也出现在 `AssessmentQuestionResponseConverter` 中，`answered` 字段同样缺失。这些死方法当前无调用方，但保留了语义错误的转换路径，给未来代码复用留下隐患。本次变更通过删除死方法、补充架构测试防护，确保 DTO 转换严格遵循 `Entity → Result → ResponseDTO` 的规范链路。

## What Changes

- 删除 `AssessmentTimeResponseConverter` 中的 `toDTO(AssessmentTime entity)` 和 `toDTOList(List<AssessmentTime>)` 死方法。
- 删除 `AssessmentQuestionResponseConverter` 中的 `toDTO(AssessmentQuestion entity)`、`toDTOForUser(AssessmentQuestion entity)` 及对应的 `toDTOList` / `toDTOListForUser` 死方法。
- 引入 ArchUnit 架构测试，禁止 `api.converter` 包下的 `toDTO` / `toDTOList` / `toDTOForUser` 等方法直接接收领域实体参数。
- 更新后端开发手册，修正 converter 位置描述与实际代码结构一致（`api/converter`），并明确禁止实体直转 DTO。
- 调整 GitHub issue #35 的标题和正文，反映扩大的清理范围。

## Capabilities

### New Capabilities

- `backend-converter-contract-enforcement`：通过 ArchUnit 架构测试强制 converter 层遵守 `Entity → Result → ResponseDTO` 转换链，防止未来出现从领域实体直接转响应 DTO 的死方法。

### Modified Capabilities

- 无。本次变更仅删除死代码并补充架构约束，不改变现有接口行为或需求。

## Impact

- 后端代码：`src/backend/src/main/java/com/bluenet/web/api/converter/assessment_time/AssessmentTimeResponseConverter.java`
- 后端代码：`src/backend/src/main/java/com/bluenet/web/api/converter/assessment_question/AssessmentQuestionResponseConverter.java`
- 测试代码：新增 ArchUnit 架构测试类
- 构建配置：`src/backend/pom.xml` 增加 `archunit-junit5` 依赖
- 文档：`docs/后端开发手册.md` 中 converter 位置与转换链描述
- GitHub issue：#35 标题和正文更新
