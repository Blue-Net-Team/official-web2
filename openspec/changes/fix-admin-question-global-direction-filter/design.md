## Context

Issue #26 指出：管理端考题管理页面 (`/admin/assessment/question`) 的方向筛选器仅包含 `DIRECTION_LABELS` 枚举的三个具体方向，导致 `direction = null` 的全局考核无法被筛选，也无法为其创建/编辑题目。

当前页面实现使用两个 AntD Select 依次选择方向和考核时间，然后加载考题表格。考核时间管理页面 (`/admin/assessment/time`) 已完整支持全局考核：使用 `__GLOBAL__` 作为 Select 的哨兵值，在提交/状态转换时映射为业务上的 `null`。

## Goals / Non-Goals

**Goals:**
- 考题管理页面的方向筛选器支持「全局」选项，能正确过滤出 `direction = null` 的考核时间。
- 仅 `SUPER_ADMIN` 可见并操作「全局」选项；`DIRECTION_ADMIN` 保持只能管理本方向考题。
- 保持与考核时间管理页面一致的 `__GLOBAL__` 交互模式。

**Non-Goals:**
- 不改动考题创建/编辑 Drawer 内部的表单逻辑（`QuestionDrawer.tsx` 已经只依赖 `assessmentTimeId`，无需调整）。
- 不改动后端接口、数据库、权限注解。
- 本次变更不处理评审评分页和录用决策页的同样问题（可单独提 issue）。

## Decisions

### 1. 使用 `__GLOBAL__` 作为 Select 哨兵值
**理由**：
- 考核时间管理页已采用该约定，保持一致性可降低维护成本。
- AntD Select 的受控组件对 `null` value 存在空值语义歧义（会触发 placeholder / allowClear 异常），使用显式字符串哨兵更安全。

### 2. `filterDirection` 类型扩展为 `Direction | null | undefined`
**理由**：
- `undefined`：未选择方向
- `null`：已选择「全局」
- `Direction`：已选择具体方向

### 3. 全局考题仅 `SUPER_ADMIN` 可操作
**理由**：
- 与考核时间管理页一致（`canOperate` 对 `direction == null` 仅允许超管）。
- `cross-direction-global-assessment` 设计中已明确：全局考核仅 `SUPER_ADMIN` 可创建/修改。

### 4. 考核时间过滤逻辑
```typescript
if (filterDirection === null) {
  assessmentTimes.filter((t) => t.direction === null)
} else {
  assessmentTimes.filter((t) => t.direction === filterDirection)
}
```
**理由**：从所有考核时间中根据当前筛选方向过滤，全局场次单独处理。

## Risks / Trade-offs

- [Risk] 当前考题管理页面已从旧版 Steps 改为 Select，但 `assessment-question-admin-ui` spec 仍描述 Steps 行为，spec 与实际实现存在历史偏差。
  → Mitigation：本 change 只更新与全局筛选相关的需求条；Steps 与 Select 的差异超出 #26 范围，不在本次处理。
- [Risk] `DIRECTION_ADMIN` 之前看不到全局考核，修复后若后端权限校验有漏洞，可能产生越权。
  → Mitigation：仅做前端显隐控制，实际创建/编辑/删除仍由后端 `@RequiresPermission` + 应用层校验负责。
