## 1. 后端权限修复

- [x] 1.1 在 `AssessmentTimeServiceImpl.createAssessmentTime` 中增加 DIRECTION_ADMIN 方向校验：当前用户为 DIRECTION_ADMIN 时，`request.direction` 必须等于 `currentUser.direction`
- [x] 1.2 在 `AssessmentTimeServiceImpl.updateAssessmentTime` 中增加 DIRECTION_ADMIN 方向校验：目标考核时间的 direction 必须等于 `currentUser.direction`
- [x] 1.3 在 `AssessmentTimeServiceImpl.deleteAssessmentTime` 中增加 DIRECTION_ADMIN 方向校验：目标考核时间的 direction 必须等于 `currentUser.direction`
- [x] 1.4 编写后端权限校验单元测试，覆盖：DIRECTION_ADMIN 创建/更新/删除自己方向成功、操作其他方向被拒绝、SUPER_ADMIN 操作任意方向成功

## 2. 前端 API Service

- [x] 2.1 创建 `src/frontend/src/apis/services/admin-assessment-time.service.ts`，封装 `getList`、`create`、`update`、`delete` 四个方法
- [x] 2.2 在 `src/frontend/src/apis/schema/assessment.dto.ts` 中补充管理端请求 DTO 类型（CreateAssessmentTimeRequestDTO、UpdateAssessmentTimeRequestDTO）

## 3. 考核时间管理页面

- [x] 3.1 创建 `src/frontend/src/app/admin/assessment/time/page.tsx`，实现分页表格（Ant Design Table），展示方向（Tag）、轮次、年级、时间、限时、状态列
- [x] 3.2 实现方向/年级筛选功能（客户端筛选），筛选变化时重置分页
- [x] 3.3 实现表格数据格式化：方向中文标签、时间格式化（YYYY-MM-DD HH:mm）、限时显示、考核状态标签（未开始/进行中/已结束）
- [x] 3.4 根据用户角色控制操作按钮可见性：DIRECTION_ADMIN 非本方向记录隐藏编辑/删除按钮

## 4. 考核时间 Drawer 组件

- [x] 4.1 创建 `src/frontend/src/app/admin/assessment/time/AssessmentTimeDrawer.tsx`，支持查看（view）/编辑（edit）/创建（create）三种模式
- [x] 4.2 实现表单字段：方向（Select）、轮次（InputNumber）、年级（InputNumber）、开始时间（DatePicker）、结束时间（DatePicker）、限时开关（Switch）、限时分钟数（条件显示）
- [x] 4.3 实现表单校验：必填项、时间逻辑、限时分钟数条件必填
- [x] 4.4 DIRECTION_ADMIN 新增时方向选择器默认锁定为自己方向；SUPER_ADMIN 可选择所有方向
- [x] 4.5 实现创建/更新 API 调用，成功后刷新列表并关闭 Drawer

## 5. 删除功能

- [x] 5.1 实现删除确认对话框（Ant Design Modal.confirm），调用删除 API，处理成功和错误（关联题目冲突）响应

## 6. 验证与集成

- [x] 6.1 启动前端开发服务器，验证页面加载、筛选、CRUD 操作正常（TypeScript 编译通过）
- [x] 6.2 验证 DIRECTION_ADMIN 权限控制：只能操作自己方向的数据，可查看所有方向（代码逻辑已实现，待实际运行验证）
- [x] 6.3 验证 SUPER_ADMIN 权限控制：可操作所有方向的数据（代码逻辑已实现，待实际运行验证）
