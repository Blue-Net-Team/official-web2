## Why

学习路径的 `step_number` 一列承担了两个互相冲突的职责：它既是**排序键**（`ORDER BY step_number ASC`），又是有 `UNIQUE (direction, step_number)` 约束的**人工输入编号**。结果是管理员调整展示顺序必须手工改数字，而任何一个"改动已有序号"的操作都会撞上唯一约束（重排 1↔2 会在中途产生重复值）。后台表格因此只能提供"手填序号"这种易冲突、易断档的维护方式。

本次变更把**序号降级为视图**：后端只负责*顺序*，前端按位置*编号*。这样拖拽排序成为唯一维护顺序的手段，编号永远连续、永远不可能冲突。

## What Changes

- **BREAKING** 删除 `tb_direction_learning_step` 的唯一约束 `uk_direction_step`，并把 `step_number` 列替换为无语义的排序列 `sort_order`（V28 迁移回填既有顺序）。
- **BREAKING** 公开接口 `GET /api/v1/directions/{slug}/learning-path` 的 steps 元素**不再返回** `stepNumber`，数组顺序即语义。管理接口的创建/更新请求体同样去掉 `stepNumber`（创建时后端自动追加到该方向末尾）。
- **BREAKING** 移除"该方向的步骤序号已存在"的 400 校验路径（含 `existsByDirectionAndStepNumber` 仓储方法与 mapper 查询）。
- 新增管理接口 `PUT /api/v1/admin/directions/{slug}/learning-steps/sort`，批量写入拖拽后的顺序，权限标识 `direction-learning-path:sort`。
- 后台学习路径页面表格改为拖拽排序：新增 `HolderOutlined` 拖拽把手列，沿用竞赛/资源页的 `dnd-kit` + 乐观更新 + 失败回滚模式；"步骤序号"列改由行位置派生。
- 后台 Drawer 表单移除"步骤序号"输入项，仅保留标题与相关链接。
- 公开方向详情页保留大号序号视觉，但改由前端 `index + 1` 生成（`String(index + 1).padStart(2, '0')`），不再依赖后端字段。
- 删除步骤后无需任何补位逻辑：编号由渲染位置派生，天然连续。

## Capabilities

### New Capabilities

<!-- 无新增能力：本次变更全部落在既有能力的需求修订上 -->

### Modified Capabilities

- `backend-direction-learning-path`: 存储模型由"唯一序号列"改为"无语义排序列"；公开/管理 DTO 去掉 `stepNumber`；新增拖拽排序接口与 `sort` 权限；移除序号冲突校验。
- `admin-learning-path-page`: 表格新增拖拽把手与拖拽排序交互，序号列改为按行位置派生；Drawer 移除步骤序号字段。
- `frontend-direction-learning-path-service`: `LearningStepDTO` 去掉 `stepNumber`；创建/更新入参去掉 `stepNumber`；新增 `batchUpdateSortOrder` 方法。
- `frontend-direction-detail-page`: 学习路径大号序号由后端字段改为前端按位置派生，其余展示行为不变。

## Impact

- **数据库**：新增迁移 `V28__replace_step_number_with_sort_order.sql`（ADD `sort_order` → 回填 → DROP CONSTRAINT → DROP `step_number`）。历史迁移 V1/V26 不改动，新库按 V1→V26→V28 顺序执行仍然成立。
- **后端**：`DirectionLearningStepDO`、`DirectionLearningStep`（领域实体）、`LearningPathMapper`(+XML)、`LearningPathRepository`(+Impl)、`LearningPathCommands`、`LearningPathResult`、`LearningStepDTO`/`CreateLearningStepRequestDTO`/`UpdateLearningStepRequestDTO`、两个 response/request Converter、`AdminLearningPathController`、`LearningPathAppServiceImpl`。新增权限标识 `direction-learning-path:sort`（受 `PermissionScanner` 全局唯一性校验约束）。
- **前端**：`src/app/admin/learning-path/page.tsx`、`LearningStepDrawer.tsx`、`src/apis/schema/direction.dto.ts`、`src/apis/services/direction.service.ts`、`src/components/Direction/LearningPath/index.tsx`、`src/components/Direction/types.ts`。
- **测试**：`DirectionLearningStepTest`、`LearningPathAppServiceImplIntegrationTest`、`LearningPathRepositoryImplIntegrationTest`、`AdminLearningPathControllerIntegrationTest`、`LearningPathControllerIntegrationTest` 需按 TDD 顺序先行修订。
- **兼容性**：公开 `stepNumber` 字段的唯一消费者是本站前端（已核对 `grep` 结果），无外部/移动端调用方，故去掉字段是安全的。
- **风险/约束**：`index + 1` 只在"整份列表一次性渲染"时正确。学习路径接口不分页，当前成立；若未来对该接口分页，序号派生方案会静默失效，需改为后端持久化并返回顺序值。
