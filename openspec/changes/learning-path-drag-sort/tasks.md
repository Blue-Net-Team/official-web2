## 1. 测试先行（红灯）

- [ ] 1.1 修订 `DirectionLearningStepTest`：`create` 不再接收序号（改为系统分配的顺序值），移除 `stepNumber >= 1` 输入校验相关用例，新增"更新不改变顺序"用例
- [ ] 1.2 修订 `LearningPathRepositoryImplIntegrationTest`：移除 `existsByDirectionAndStepNumber` 用例，新增 `findMaxSortOrder(direction)`（按方向隔离，空方向返回 null）与 `batchUpdateSortOrder`（接受非连续值、接受瞬时重复值不报唯一约束）用例
- [ ] 1.3 修订 `LearningPathAppServiceImplIntegrationTest`：创建步骤追加到该方向末尾、更新步骤不改变位置、删除步骤不重排、批量排序全量覆盖、跨方向 id 与不存在 id 被拒绝
- [ ] 1.4 修订 `LearningPathControllerIntegrationTest`：公开响应 steps 元素不含 `stepNumber`、按 `sort_order` 升序返回
- [ ] 1.5 修订 `AdminLearningPathControllerIntegrationTest`：创建/更新请求体不含序号、新增 `PUT /{slug}/learning-steps/sort` 的成功与 403 用例
- [ ] 1.6 运行后端测试确认上述用例失败（红灯），作为实施基线

## 2. 数据库迁移

- [ ] 2.1 新增 `V28__replace_step_number_with_sort_order.sql`：`ADD COLUMN sort_order INT NOT NULL DEFAULT 0` → 用 `step_number` 回填 → `DROP CONSTRAINT uk_direction_step` → `DROP COLUMN step_number`
- [ ] 2.2 确认历史迁移 V1/V26 未被修改，且新库按 V1→V26→V28 顺序执行仍成立
- [ ] 2.3 准备回滚 SQL（`row_number() OVER (PARTITION BY direction ORDER BY sort_order)` 重建 `step_number` 及唯一约束）并记录

## 3. 基础设施层

- [ ] 3.1 `DirectionLearningStepDO`：`stepNumber` 字段改为 `sortOrder`，补 `@TableField("sort_order")` 映射（如启用自动映射则确认下划线策略）
- [ ] 3.2 `LearningPathMapper.xml`：所有查询的 `step_number AS stepNumber` 改为 `sort_order AS sortOrder`，`ORDER BY sort_order ASC`；移除 `existsByDirectionAndStepNumber` 查询
- [ ] 3.3 `LearningPathMapper.xml`：新增 `selectMaxSortOrder(direction)` 查询
- [ ] 3.4 `LearningPathMapper.xml`：新增 `batchUpdateSortOrder`，用一条 `UPDATE ... SET sort_order = CASE id WHEN ... THEN ... END WHERE id IN (...)`，并限定 `direction`
- [ ] 3.5 `LearningPathMapper` 接口：移除 `existsByDirectionAndStepNumber`，新增 `selectMaxSortOrder`、`batchUpdateSortOrder`
- [ ] 3.6 `LearningPathRepository`（接口）：移除 `existsByDirectionAndStepNumber`，新增 `findMaxSortOrder(Direction)`、`batchUpdateSortOrder(List<SortItem>)`，并在接口内定义 `SortItem` 记录
- [ ] 3.7 `LearningPathRepositoryImpl`：实现上述两个新方法，并按既有约定完成 DO↔聚合转换
- [ ] 3.8 `LearningPathRepositoryConverter`：字段改名跟随（`stepNumber` → `sortOrder`）

## 4. 领域层

- [ ] 4.1 `DirectionLearningStep`：`stepNumber` 字段改名为 `sortOrder`，`create` 签名改为接收系统分配的 `sortOrder`，移除"序号 ≥ 1"的人工输入校验（保留非空与顺序值合法性校验）
- [ ] 4.2 `DirectionLearningStep`：`updateStepNumber` 改为 `updateSortOrder`（仅供重排路径使用或改为包内可见）
- [ ] 4.3 确认领域实体不再承担"序号唯一"职责，唯一性概念从领域词汇中彻底移除

## 5. 应用层

- [ ] 5.1 `LearningPathCommands`：`CreateLearningStepCommand` / `UpdateLearningStepCommand` 去掉 `stepNumber`，新增 `BatchUpdateSortOrderCommand`（含 `slug` 与 items）
- [ ] 5.2 `LearningPathResult`：`stepNumber` 改为不对外暴露的 `sortOrder`（或移除，由转换器按需取用）
- [ ] 5.3 `LearningPathAppServiceImpl.createStep`：移除序号冲突校验，改为 `findMaxSortOrder(direction) + 1`（无记录时为 1）后追加
- [ ] 5.4 `LearningPathAppServiceImpl.updateStep`：移除序号冲突校验与序号更新，只更新标题与相关链接
- [ ] 5.5 `LearningPathAppServiceImpl.deleteStep`：确认不引入任何补位/重排逻辑
- [ ] 5.6 `LearningPathAppServiceImpl`：新增 `batchUpdateSortOrder`，`@Transactional` 内校验所有 id 属于该方向且均存在，否则抛错回滚，通过后调用仓储批量写入
- [ ] 5.7 `LearningPathAppService`：接口同步新增方法签名

## 6. API 层

- [ ] 6.1 `LearningStepDTO`：移除 `stepNumber` 字段（不新增 sortOrder，数组顺序即契约）
- [ ] 6.2 `CreateLearningStepRequestDTO` / `UpdateLearningStepRequestDTO`：移除 `stepNumber` 字段及其校验注解
- [ ] 6.3 新增 `BatchUpdateSortOrderRequestDTO`：`List<Item>`，`Item` 含 `id` 与 `sortOrder`，带 `@Valid` / `@NotNull` 校验
- [ ] 6.4 `LearningPathRequestConverter`：创建/更新命令转换去掉序号，新增排序请求 → 命令的转换
- [ ] 6.5 `LearningPathResponseConverter`：响应转换去掉 `stepNumber`
- [ ] 6.6 `AdminLearningPathController`：新增 `PUT /{slug}/learning-steps/sort`，注解 `@RequiresPermission(name = "调整学习步骤排序", value = "direction-learning-path:sort", access = AccessLevel.PROTECTED)` 与 Swagger 文档
- [ ] 6.7 全局搜索确认 `direction-learning-path:sort` 权限标识唯一，未与既有 value 冲突
- [ ] 6.8 更新已有接口的 Swagger 示例，移除 400 "步骤序号已存在" 的错误示例
- [ ] 6.9 运行后端测试至全绿，`mvnw.cmd clean compile package`

## 7. 前端

- [ ] 7.1 `src/apis/schema/direction.dto.ts`：`LearningStepDTO` 去掉 `stepNumber`，`LearningStepRequestDTO` 去掉 `stepNumber`，新增 `BatchSortRequestDTO`
- [ ] 7.2 `src/apis/services/direction.service.ts`：创建/更新请求体去掉序号，新增 `batchUpdateSortOrder(slug, { items })` 调用 `PUT /admin/directions/{slug}/learning-steps/sort`
- [ ] 7.3 `src/components/Direction/types.ts`：`LearningStep` 去掉 `stepNumber`
- [ ] 7.4 `src/components/Direction/LearningPath/index.tsx`：序号改为 `String(index + 1).padStart(2, '0')`，并在代码注释中说明"序号由位置派生，依赖接口不分页"
- [ ] 7.5 `admin/learning-path/page.tsx`：新增 `DraggableRow` 组件与 `DndContext` + `SortableContext` 包裹表格
- [ ] 7.6 `admin/learning-path/page.tsx`：新增拖拽把手列（`HolderOutlined`），`listeners` 只绑定在把手图标上，`setNodeRef`/`transform` 作用于整行
- [ ] 7.7 `admin/learning-path/page.tsx`：步骤序号列改为 `render: (_, __, index) => String(index + 1).padStart(2, '0')`
- [ ] 7.8 `admin/learning-path/page.tsx`：实现 `handleDragEnd` 的乐观更新与失败回滚（`arrayMove` → `setSteps(newList)` → 提交失败则恢复并提示"排序更新失败"）
- [ ] 7.9 `LearningStepDrawer.tsx`：移除步骤序号 `Form.Item` 与 `InputNumber`，`FormValues` 与提交 payload 同步去掉该字段
- [ ] 7.10 移除 Drawer 中针对"步骤序号已存在"的 AxiosError 透传注释/分支（若仅为此场景存在）

## 8. 端到端验证

- [ ] 8.1 检查 3000 端口占用情况：已占用则复用现有前端服务，未占用才运行 `pnpm dev`
- [ ] 8.2 启动 compose 基础设施：`docker compose -p bluenet --profile infra up -d`
- [ ] 8.3 构建并运行后端镜像：`docker build -t bluenet-api-service:latest -f docker/api-service.Dockerfile .`，然后以 `--network bluenet_network` 运行 `backend-api-dev`
- [ ] 8.4 确认 V28 迁移在容器启动时成功执行，表中 `sort_order` 已回填、`step_number` 与唯一约束已移除
- [ ] 8.5 Playwright 验证后台：拖拽换序后刷新页面顺序保持、序号连续重排、失败场景回滚（可临时断网/改接口验证）
- [ ] 8.6 Playwright 验证后台：删除中间步骤后序号自动连续、无断档
- [ ] 8.7 Playwright 验证后台：新增步骤追加到末尾，Drawer 无序号字段
- [ ] 8.8 Playwright 验证公开页：`/direction/[slug]` 大号序号显示 `01/02/03` 且与拖拽后顺序一致
- [ ] 8.9 验证权限：无 `direction-learning-path:sort` 权限的账号调用排序接口返回 403
