## 1. 准备工作

- [x] 1.1 分析后端 API 返回的 `AssessmentTimeDTO` 数据结构
- [x] 1.2 确认前端 `Assessment` 类型定义是否需要调整
- [x] 1.3 确认现有的 `assessmentTimeService`、`assessmentSessionService` 服务是否满足需求

## 2. 实现考核数据获取

- [x] 2.1 在 profile page 中导入 `assessmentTimeService` 和 `assessmentSessionService`
- [x] 2.2 创建数据转换函数，将 `AssessmentTimeDTO` 转换为 `Assessment` 类型
- [x] 2.3 实现考核状态计算函数（根据时间计算未开始/进行中/已结束）
- [x] 2.4 实现剩余时间计算函数（格式化剩余时间为"X 天 Y 小时"）
- [x] 2.5 修改 `loadData` 函数，调用后端 API 获取考核时间列表
- [x] 2.6 对于限时考核，并行调用会话接口获取截止时间
- [x] 2.7 移除 `MockProfileService` 的导入和调用

## 3. 处理加载和错误状态

- [x] 3.1 确保加载状态正确显示（使用现有的 `loading` 状态）
- [x] 3.2 添加错误处理逻辑（静默失败，控制台输出错误信息）
- [x] 3.3 处理空状态（无考核记录时的展示）

## 4. 清理和优化

- [x] 4.1 移除 `src/frontend/src/mocks/data/profile.ts` 中的 `mockAssessments` 数据
- [x] 4.2 移除 `src/frontend/src/mocks/services/profile.service.ts` 中的 `getAssessments` 方法
- [x] 4.3 检查并移除不再使用的 mock 相关导入
- [x] 4.4 运行前端代码检查（`pnpm lint`）确保代码质量

## 5. 测试验证

- [x] 5.1 启动前端开发服务器，访问个人主页
- [x] 5.2 验证考核列表数据是否正确显示
- [x] 5.3 验证考核状态是否正确计算（未开始/进行中/已结束）
- [x] 5.4 验证限时考核的剩余时间是否正确显示
- [x] 5.5 验证答题进度是否正确显示
- [x] 5.6 验证空状态展示（如无考核记录）
