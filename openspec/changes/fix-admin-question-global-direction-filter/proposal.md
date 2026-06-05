## Why

Issue #26 报告：管理端「考题管理」页面 (`/admin/assessment/question`) 的方向筛选器只包含三个具体方向，导致 `direction = null` 的全局考核（如第三轮）无法被筛选出来，也无法为全局考核创建或编辑题目。随着 `cross-direction-global-assessment` 的落地，后端已支持全局考核，前端考题管理页面需要补齐对应的筛选与操作入口。

## What Changes

- 在考题管理页面的方向筛选器中增加「全局」选项，对应 `direction = null` 的全局考核。
- 选择「全局」后，考核时间下拉框正确列出所有全局考核场次。
- 仅 `SUPER_ADMIN` 可见并操作「全局」选项，`DIRECTION_ADMIN` 保持只能管理本方向考题（与考核时间管理页一致）。
- 对齐考核时间管理页已有的 `__GLOBAL__` 哨兵值模式，避免 Select 组件对 `null` value 的受控状态歧义。
- 空状态文案随「全局」筛选状态正确展示。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `assessment-question-admin-ui`：前端考题管理页的方向筛选器需要支持「全局」选项，并仅对 `SUPER_ADMIN` 暴露该选项。

## Impact

- 前端：`src/frontend/src/app/admin/assessment/question/page.tsx`
- 后端：无改动，复用现有的 `listQuestionsForAdmin` 与题目 CRUD 接口。
- 权限：全局考题的创建/编辑/删除仍由后端 `@RequiresPermission` 控制；前端仅控制可见性与操作按钮显隐。
