## 1. 类型定义与 API Service

- [x] 1.1 在 `src/frontend/src/apis/schema/type.ts` 中补充管理端报名类型：`EnrollmentDetailDTO`（id, username, studentId, email, collegeId, collegeName, major, grade, direction, status, avatarFileId, introduction, internalReferralCode, referralUserName）、`EnrollmentStatisticsDTO`（total, byStatus, byDirection）、`EnrollmentApprovalResultDTO`（id, status, createdUserId）、`RejectEnrollmentRequestDTO`（reason）、`EnrollmentListQueryDTO`（page, size, keyword, status, direction）
- [x] 1.2 创建 `src/frontend/src/apis/services/admin-enroll.service.ts`，使用 `apiClient` 实现以下方法：`getEnrollmentList(params)` → GET `/admin/enrollments`、`getEnrollmentDetail(id)` → GET `/admin/enrollments/{id}`、`approveEnrollment(id)` → PUT `/admin/enrollments/{id}/approve`、`rejectEnrollment(id, data)` → PUT `/admin/enrollments/{id}/reject`、`getStatistics()` → GET `/admin/enrollments/statistics`

## 2. 报名管理页面

- [x] 2.1 创建 `src/frontend/src/app/admin/enroll/page.tsx` 作为 Client Component，定义页面状态：enrollmentList、statistics、loading、pagination（page, size, total）、filters（status, direction, keyword）、selectedEnrollment、drawerOpen
- [x] 2.2 实现统计数据获取与展示：页面顶部展示统计数字卡片（总数、待审核、已通过、已拒绝），使用 Ant Design Card + Statistic 组件，PC 端横向 flex 布局，移动端 2 列 grid 布局（Tailwind `md:` 断点）
- [x] 2.3 实现筛选栏：Input.Search（搜索姓名/学号）、Select（状态筛选：全部/待审核/已通过/已拒绝）、Select（方向筛选：全部/CV/结构设计/嵌入式），筛选变更触发数据刷新
- [x] 2.4 实现报名卡片网格列表：使用 Ant Design Card 组件，PC 端 `grid grid-cols-3`、移动端 `grid grid-cols-1`（Tailwind `md:grid-cols-3`），每张卡片展示头像（有 avatarFileId 则用 img 通过 `/api/v1/file/download/{fileId}` 加载，否则用 UserOutlined 图标）、姓名、学号、学院·方向、状态 Tag（待审核蓝/已通过绿/已拒绝红）、操作按钮（仅 PENDING 显示通过/拒绝）
- [x] 2.5 实现分页器：Ant Design Pagination，默认 pageSize=12，翻页触发列表刷新
- [x] 2.6 实现详情 Drawer：点击卡片打开 Ant Design Drawer（placement="right"），调用 `getEnrollmentDetail(id)` 获取完整信息并展示：头像大图、姓名、学号、邮箱、学院、专业、年级、方向、推荐人、自我介绍，底部显示操作按钮（仅 PENDING）
- [x] 2.7 实现通过操作：卡片行内和 Drawer 内的"通过"按钮调用 `approveEnrollment(id)`，成功后刷新列表和统计数据，关闭 Drawer
- [x] 2.8 实现拒绝操作：卡片行内和 Drawer 内的"拒绝"按钮弹出 Ant Design Modal，包含 TextArea（maxLength=200，placeholder="拒绝原因（可选）"）和确认/取消按钮，确认后调用 `rejectEnrollment(id, { reason })`，成功后刷新列表和统计数据，关闭 Drawer
- [x] 2.9 实现操作按钮点击阻止事件冒泡：通过/拒绝按钮添加 `onClick={e => e.stopPropagation()}` 防止触发 Drawer 打开
