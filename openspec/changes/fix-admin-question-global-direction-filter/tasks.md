## 1. 前端类型与状态调整

- [ ] 1.1 在 `page.tsx` 引入全局方向哨兵常量 `__GLOBAL__`，扩展 `filterDirection` 类型为 `Direction | null | undefined`
- [ ] 1.2 调整 `handleDirectionChange`，将 `__GLOBAL__` 映射为 `null`

## 2. 筛选器选项与考核时间过滤

- [ ] 2.1 修改 `directionOptions`：为 `SUPER_ADMIN` 追加 `{ value: '__GLOBAL__', label: '全局' }` 选项
- [ ] 2.2 修改 `fetchAssessmentTimes`：当 `direction === null` 时过滤 `t.direction === null` 的全局考核时间
- [ ] 2.3 确保选择方向切换时清空已选考核时间并重置分页

## 3. 权限与文案

- [ ] 3.1 调整 `canOperate`：全局考核（`filterDirection === null`）仅 `SUPER_ADMIN` 可操作
- [ ] 3.2 更新空状态/提示文案，对「全局」无考核时间场景给出清晰提示

## 4. 验证

- [ ] 4.1 本地运行前端，确认 `SUPER_ADMIN` 可选择「全局」并列出全局考核时间
- [ ] 4.2 确认选择全局考核时间后能正常显示/新增/编辑/删除考题
- [ ] 4.3 确认 `DIRECTION_ADMIN` 看不到「全局」选项，也无法操作全局考题
