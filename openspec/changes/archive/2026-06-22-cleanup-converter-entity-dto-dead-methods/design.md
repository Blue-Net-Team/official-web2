## Context

项目后端采用 DDD 分层架构，后端开发手册规定了严格的数据转换链：

```text
RequestDTO → Command → Entity → Result → ResponseDTO
```

其中 `Result → ResponseDTO` 应由 API 层 ResponseConverter 完成。当前 `api/converter/assessment_time/AssessmentTimeResponseConverter` 和 `api/converter/assessment_question/AssessmentQuestionResponseConverter` 中残留了从领域实体直接转响应 DTO 的方法，这些方法当前无调用方，且无法正确设置用户相关的派生字段（`eliminated`、`answered`），属于设计债。

## Goals / Non-Goals

**Goals：**
- 删除上述 converter 中从领域实体直接转 DTO 的所有死方法。
- 通过 ArchUnit 架构测试，禁止 `api.converter` 包中出现参数为领域实体的 public `toDTO` / `toDTOForUser` / `toDTOList` / `toDTOListForUser` 方法。
- 修正后端开发手册中 converter 位置描述，与实际代码结构（`api/converter`）保持一致。
- 更新 GitHub issue #35 的标题和正文，反映扩大的清理范围。

**Non-Goals：**
- 不修改现有 Controller、Application Service、Domain Entity 的行为。
- 不新增或删除任何 REST 接口。
- 不修改前端代码。
- 不引入除 ArchUnit 之外的额外依赖。

## Decisions

### 1. 删除死方法而非补默认值

**选择**：直接删除 `toDTO(Entity)` 系列方法，而不是给 `eliminated` / `answered` 补默认值。

**理由**：
- 这些方法当前无调用方，删除是安全的。
- 默认值会掩盖语义错误：`eliminated` 和 `answered` 都依赖当前用户上下文，从实体无法正确推导，默认值 `false` 可能误导前端。
- 删除可以强制未来开发者走 `Entity → Result → ResponseDTO` 的规范链路。

### 2. 使用 ArchUnit 进行架构测试

**选择**：引入 `com.tngtech.archunit:archunit-junit5` 依赖，编写 JUnit 5 架构规则测试。

**理由**：
- 规则与业务无关，一次编写长期生效。
- 能在 CI 阶段自动拦截违反 converter 契约的代码。
- 项目已有 JUnit 5 基础设施，接入成本低。

**规则范围**：
- 目标包：`com.bluenet.web.api.converter..`
- 目标方法：public 方法且方法名匹配 `toDTO`、`toDTOForUser`、`toDTOList`、`toDTOListForUser`
- 禁止参数类型属于 `com.bluenet.web.domain.model.entity..` 包。

### 3. 文档更新仅做最小修正

**选择**：仅修改后端开发手册中 converter 位置描述（`application/converter` → `api/converter`）和转换链说明，不动其他章节。

**理由**：
- 实际代码结构已经稳定在 `api/converter`，文档应反映现实。
- 转换链规则本身正确，只需强化执行机制。

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
 某条 feature branch 或 PR 依赖这些死方法 | 全局搜索确认当前无调用方；删除后在 PR 中明确说明 |
 ArchUnit 规则过于严格，误伤合理场景 | 规则仅针对 `toDTO*` 系列方法；如有合理例外，可在测试中显式豁免 |
 文档更新遗漏其他不一致处 | 本次只修正 converter 相关描述，不做全量文档重构 |

## Migration Plan

无需数据迁移或部署特殊步骤。变更纯为代码清理和测试增强，按常规 CI/CD 流程合并即可。

## Open Questions

- ArchUnit 规则是否需要同时覆盖 `application/converter` 包（如果未来重建该包）？建议当前只针对 `api/converter`，后续如有新增包再扩展。
