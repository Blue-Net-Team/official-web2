## 1. 后端 - 修改文件上传接口

- [x] 1.1 修改 `FileService.uploadAssessmentWork` 方法签名：参数从 `answerId` 改为 `questionId`
- [x] 1.2 修改 `FileServiceImpl.uploadAssessmentWork` 实现：通过 questionId 查询题目并校验方向权限，移除 answer 相关逻辑
- [x] 1.3 修改 `FileUploadController.uploadAssessmentWork`：参数从 `answerId` 改为 `questionId`

## 2. 后端 - 新增题目详情接口

- [x] 2.1 `AssessmentQuestionService` 新增 `getQuestionDetail(Long id)` 方法
- [x] 2.2 `AssessmentQuestionServiceImpl` 实现：查询题目详情，CANDIDATE 权限校验方向和年级
- [x] 2.3 `AssessmentQuestionController` 新增 `GET /api/v1/assessment-questions/{id}` 端点

## 3. 后端 - 新增答案 CRUD API

- [x] 3.1 创建 `CreateAnswerRequestDTO`（questionId, fileId）
- [x] 3.2 创建 `AssessmentAnswerDTO`（id, questionId, fileId, submitTime）
- [x] 3.3 `AssessmentAnswerDomainService` 新增 `createAnswer` 方法（含重复提交检查）
- [x] 3.4 `AssessmentAnswerDomainServiceImpl` 实现 createAnswer
- [x] 3.5 `AssessmentAnswerRepository` 新增 `findByUserIdAndQuestionId` 方法
- [x] 3.6 `AssessmentAnswerRepositoryImpl` 实现 findByUserIdAndQuestionId
- [x] 3.7 `AssessmentAnswerMapper` 新增 SQL 查询
- [x] 3.8 创建 `AssessmentAnswerService` 应用服务接口
- [x] 3.9 创建 `AssessmentAnswerServiceImpl`：编排创建答案逻辑（校验题目、关联文件、设置提交时间）
- [x] 3.10 创建 `AssessmentAnswerController`：`POST /api/v1/assessment-answers` 和 `GET /api/v1/assessment-answers?questionId=X`

## 4. 前端 - API 服务层

- [x] 4.1 `assessment.ts` 新增 `AssessmentAnswerDTO` 类型定义和 `QuestionContent` 子类型
- [x] 4.2 创建 `assessment-answer.service.ts`：createAnswer + getAnswer 方法
- [x] 4.3 `assessment-question.service.ts` 新增 getQuestionDetail 方法
- [x] 4.4 `file.service.ts` 新增 uploadWork 方法（调用修改后的接口，传 questionId）

## 5. 前端 - 答题页面

- [x] 5.1 创建答题页路由目录 `[timeId]/questions/[questionId]/page.tsx`
- [x] 5.2 创建答题页样式文件 `styles.module.css`
- [x] 5.3 实现答题页主体结构：顶部导航（返回+题目标题+状态标签）+ 主内容区 + 右侧栏
- [x] 5.4 实现题目描述展示区域（题目要求 + 附件下载）
- [x] 5.5 实现文件上传区域（拖拽上传 + 点击上传 + 上传进度 + 已上传文件展示/删除）
- [x] 5.6 实现提交按钮逻辑（上传文件 → 创建答案 → 状态更新）
- [x] 5.7 实现答题状态恢复（重新进入已答题目展示已提交信息）
- [x] 5.8 实现倒计时轮盘组件（限时考核展示圆环进度 + 数字倒计时）
- [x] 5.9 实现时间范围展示（非限时考核展示时间范围 + 状态标签）
- [x] 5.10 实现上一题/下一题导航
- [x] 5.11 修改考题目录页：题目行可点击跳转到答题页

## 6. 后端 - 考核会话计时机制

- [x] 6.0 创建 Flyway 迁移 `V23__create_assessment_session.sql`：建表 `tb_assessment_session`（id, user_id, assessment_time_id, start_time, deadline），唯一约束 (user_id, assessment_time_id)
- [x] 6.1 创建 `AssessmentSession` 实体（对应 tb_assessment_session）
- [x] 6.2 创建 `AssessmentSessionVO`（id, userId, assessmentTimeId, startTime, deadline）
- [x] 6.3 创建 `AssessmentSessionMapper`（extends BaseMapper）
- [x] 6.4 创建 `AssessmentSessionRepository` 接口 + `AssessmentSessionRepositoryImpl`（save, findByUserIdAndAssessmentTimeId）
- [x] 6.5 创建 `AssessmentSessionDomainService` 接口 + `AssessmentSessionDomainServiceImpl`（getOrCreateSession：有则返回，无则创建并计算 deadline）
- [x] 6.6 修改 `AssessmentQuestionController.listQuestions`：限时考核时，调用 session 服务获取/创建 session，将 deadline 返回在响应中
- [x] 6.7 修改 `AssessmentQuestionDTO` 或 `PageDTO` 响应：新增可选字段 `deadline`（限时考核时返回 ISO 时间戳）
- [x] 6.8 新增 `GET /api/v1/assessment-sessions/{assessmentTimeId}` 接口：查询当前用户的考核会话（含 deadline），供答题页使用

## 7. 前端 - 倒计时改为服务端 deadline 驱动

- [x] 7.1 修改 `assessment.ts` 类型：`AssessmentQuestionDTO` 或列表响应新增 `deadline` 字段
- [x] 7.2 修改 `CountdownTimer` 组件：接收 `deadline: string`（ISO 时间戳），直接计算 `max(0, deadline - now)`，移除 startTime/endTime/timeLimitMinutes 参数
- [x] 7.3 修改答题页 `page.tsx`：从列表接口或 session 接口获取 deadline，传入 CountdownTimer
- [x] 7.4 实现超时自动提交：倒计时归零时自动触发当前已上传文件的答案提交
- [x] 7.5 实现超时锁定：deadline 已过后禁用上传和提交按钮，显示"考核已结束"

## 8. 集成验证

- [x] 8.1 后端接口联调测试（上传文件 → 创建答案 → 查询答案）
- [x] 8.2 前端完整流程测试（进入答题 → 上传 → 提交 → 恢复状态）
- [x] 8.3 限时考核流程测试（进入考题 → session 创建 → 倒计时 → 超时自动提交）
