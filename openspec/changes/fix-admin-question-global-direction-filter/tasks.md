## 1. 前端类型与状态调整

- [x] 1.1 在 `page.tsx` 引入全局方向哨兵常量 `__GLOBAL__`，扩展 `filterDirection` 类型为 `Direction | null | undefined`
- [x] 1.2 调整 `handleDirectionChange`，将 `__GLOBAL__` 映射为 `null`

## 2. 筛选器选项与考核时间过滤

- [x] 2.1 修改 `directionOptions`：为 `SUPER_ADMIN` 追加 `{ value: '__GLOBAL__', label: '全局' }` 选项
- [x] 2.2 修改 `fetchAssessmentTimes`：当 `direction === null` 时过滤 `t.direction === null` 的全局考核时间
- [x] 2.3 确保选择方向切换时清空已选考核时间并重置分页

## 3. 权限与文案

- [x] 3.1 调整 `canOperate`：全局考核（`filterDirection === null`）仅 `SUPER_ADMIN` 可操作
- [x] 3.2 更新空状态/提示文案，对「全局」无考核时间场景给出清晰提示

## 4. 验证

- [x] 4.1 TypeScript 类型检查通过（`npx tsc --noEmit`）
- [x] 4.2 Prettier 格式检查通过并自动修复
- [x] 4.3 Playwright 确认登录页可正常加载，无编译错误
- [ ] 4.4 需用户以 `SUPER_ADMIN` 登录，在 `/admin/assessment/question` 验证「全局」选项可筛选并管理全局考题
- [ ] 4.5 需用户以 `DIRECTION_ADMIN` 登录，验证看不到「全局」选项
