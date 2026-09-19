## Context

学习路径当前用 `tb_direction_learning_step.step_number` 同时承载**排序键**与**人工输入的唯一编号**：

```
tb_direction_learning_step
  step_number INT NOT NULL
  CONSTRAINT uk_direction_step UNIQUE (direction, step_number)

ORDER BY step_number ASC  →  前端渲染 String(stepNumber).padStart(2,'0')  →  "01/02/03"
```

管理页只能通过 Drawer 手填序号来调整顺序，任何"改动已有序号"的操作都会撞唯一约束（重排 1↔2 会在语句中途产生重复值）。删除一步会留下断档（`01/03/04`）。

同仓库的 `tb_competition.sort_order` 与 `tb_software_resource.sort_order` 已经采用了"无语义排序列 + 拖拽 + 批量写顺序"的模式，是本次可以直接对齐的既有约定。

约束：

- 学习路径接口**不分页**，整份列表一次返回。
- 公开 DTO 的 `stepNumber` 唯一消费者是本站前端（已核对 `grep`），无外部调用方。
- `@RequiresPermission` 的 `value` 必须全局唯一，`PermissionScanner` 启动时校验，重复会导致启动失败。
- 历史迁移 V1/V26 已执行，不可回头修改。

## Goals / Non-Goals

**Goals:**

- 后台通过拖拽维护学习路径顺序，不再手工填写编号。
- 序号在展示层永远连续、不可能冲突、删除后天然补位。
- 后端只负责*顺序*，前端只负责*编号*，两者职责不重叠。
- 与竞赛/资源页使用同一套拖拽实现与批量排序约定，降低维护成本。

**Non-Goals:**

- 不引入分页、不做跨方向拖拽、不做排序动画增强或键盘可达性改造。
- 不保留"插入到 2 和 3 之间"或间隔编号的能力（顺序连续由渲染位置保证，不需要持久化间隔语义）。
- 不改动历史迁移 V1/V26，不改动学习路径的公开路由与 ISR 策略。

## Decisions

### 决策 1：把序号降级为视图（前端按位置派生）

**选择**：后端不返回任何序号；公开页与后台表格都用 `index + 1` 渲染（`String(index + 1).padStart(2, '0')`）。

**理由**：这是"删除后自动编号"唯一零成本的实现方式——编号在渲染那一刻才存在，永远连续，不存在断档 bug，也不需要任何补位事务。

**备选**：

- *持久化连续序号 + 删除时把后续行整体 -1*：每次删除多一个补位事务，并发下易错，唯一收益是 API 返回的序号连续——而该字段没有外部消费者。
- *保留唯一约束 + 两阶段重排（先取负再回填）*：保留约束作为不变量，但让**每一次**排序/删除重排都背上"先写临时负数"的技巧，永久复杂度换一个已无真实错误可拦的约束。

### 决策 2：列改名 `step_number` → `sort_order`，而非保留列名去掉约束

**选择**：新增 `sort_order INT NOT NULL DEFAULT 0`，迁移回填后删除 `step_number`。

**理由**：保留 `step_number` 这个名字会持续误导后来人（"序号"却不再唯一、不再连续、不再对外展示）。改名后字段语义单一，且与 `tb_competition.sort_order` / `tb_software_resource.sort_order` 命名一致，三处实现可以互相参照。

**备选**：保留列名只删约束——改动面更小，但留下一处长期语义债。

### 决策 3：删除 `uk_direction_step` 唯一约束

**选择**：迁移中 `DROP CONSTRAINT uk_direction_step`。

**理由**：该约束的历史价值是"兜住人工手填序号重复"。一旦移除人工填写、且所有写路径都整体重写 `sort_order`，重复来源即消失，约束不再拦任何真实错误，只会阻碍单条 `CASE WHEN` 批量重排。

**备选**：保留约束 → 见决策 1 的备选分析。

### 决策 4：创建步骤时后端按方向取 `MAX(sort_order) + 1` 追加到末尾

**选择**：`findMaxSortOrder(direction)`，映射竞赛的 `findMaxSortOrder()`（竞赛是全局，学习路径需**按方向隔离**）。创建请求体不再含任何位置信息。

**理由**：与竞赛一致；避免前端计算位置带来的并发歧义。

### 决策 5：批量排序接口全量覆盖该方向

```
PUT /api/v1/admin/directions/{slug}/learning-steps/sort
{ "items": [ { "id": 1, "sortOrder": 1 }, { "id": 2, "sortOrder": 2 } ] }
@RequiresPermission(name = "调整学习步骤排序", value = "direction-learning-path:sort")
```

**选择**：请求携带该方向**全部**步骤的新顺序；后端在单个事务内用一条 `CASE id WHEN ... THEN ...` 更新，并校验所有 `id` 均属于该 `slug`（防串方向）。

**理由**：列表不分页，全量覆盖最简单且无中间态；因唯一约束已删除，单条语句即可完成，无需两阶段。归属校验是新增接口唯一的额外安全点。

**备选**：只传被移动的一项 + 目标位置 → 后端需自行推导位移，逻辑更复杂且仍需全量重写，收益为零。

### 决策 6：拖拽实现直接复用竞赛页模式

**选择**：`@dnd-kit` 的 `DndContext` + `SortableContext` + `useSortable` 封装的 `DraggableRow`，`PointerSensor` 配 `activationConstraint: { distance: 5 }`，新增 `HolderOutlined` 把手列，乐观更新 + 失败回滚。

**与竞赛页的差异**：竞赛页把 `listeners` 铺在**整行** `<tr>` 上（因为整行点击是打开的 Drawer 的主交互）；学习路径行内已有编辑/删除按钮，若同样铺满整行容易与按钮点击互相干扰。因此本次**只把 `listeners` 绑定在把手图标上**，`setNodeRef`/`transform` 仍作用于整行。

```
handleDragEnd:
  newList = arrayMove(displayList, oldIndex, newIndex)
  setDisplayList(newList)                                  // 乐观
  await batchUpdateSortOrder({ items: newList.map((s, i) => ({ id: s.id, sortOrder: i + 1 })) })
  失败 → setDisplayList(steps) + message.error('排序更新失败')
```

**理由**：与 `admin/competition/page.tsx`、`admin/resources/page.tsx` 完全同构；且学习路径不分页，`handleDragEnd` 比竞赛更简单（无 `baseSortOrder = currentPage * PAGE_SIZE` 计算）。

## Risks / Trade-offs

- **[`index + 1` 与分页强耦合]** → 只在"整份列表一次性渲染"时正确；若将来给学习路径接口加分页，序号会退化为每页都从 01 开始且静默出错。缓解：在 `LearningPath` 组件与 `sortOrder` 相关代码处留注释，并在本 spec 中固化为显式约束。
- **[并发双管理员拖拽 → last-write-wins]** → 两人同时拖拽会以后写者覆盖前者。缓解：该页面为方向管理员低频操作、单方向步骤数极少（个位数），当前接受；若后续出现真实争用再引入 version 乐观锁。
- **[迁移含 DDL，需短时停机/锁表]** → 缓解：`tb_direction_learning_step` 数据量极小，`ADD COLUMN`/`DROP COLUMN` 均为瞬时操作；迁移按"回填后再删列"顺序执行，保证回填阶段旧列仍可读。
- **[字段移除是公开 API 的破坏性变更]** → 缓解：已确认无外部消费者；前端与后端在同一次发布中同步更新。
- **[新权限标识可能撞名]** → 缓解：`direction-learning-path:sort` 与既有 `:view/:create/:update/:delete` 命名空间一致，实现前先全局搜索确认唯一。

## Migration Plan

V28 迁移 `V28__replace_step_number_with_sort_order.sql`，单事务内四步：

```
1. ALTER TABLE tb_direction_learning_step ADD COLUMN sort_order INT NOT NULL DEFAULT 0
2. UPDATE tb_direction_learning_step SET sort_order = step_number      -- 回填既有顺序
3. ALTER TABLE tb_direction_learning_step DROP CONSTRAINT uk_direction_step
4. ALTER TABLE tb_direction_learning_step DROP COLUMN step_number
```

- 历史迁移 V1/V26 不改动：新库按 V1 → V26 → V28 顺序执行，V28 执行时 `step_number` 仍存在，回填成立。
- 部署顺序：先迁移数据库，再发布后端（公开响应去掉 `stepNumber`），前端随后端同批发布。
- 回滚策略：反向迁移用 `row_number() OVER (PARTITION BY direction ORDER BY sort_order)` 重建 `step_number` 与唯一约束（编号连续，因此必然可重建）。

## Open Questions

- 无。展示层序号保留大号视觉已确认；后端不再参与编号，公开页卡片版面不变。
