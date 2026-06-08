## 1. 后端 — 移除 finalizeScore 评语机制

- [x] 1.1 修改 `FinalizeScoreRequestDTO`，删除 `comment` 字段
- [x] 1.2 修改 `AssessmentJudgementCommands.FinalizeScoreCommand`，删除 `comment` 字段
- [x] 1.3 修改 `AdminAssessmentJudgementController.finalizeScore()`，不传 `comment` 参数
- [x] 1.4 修改 `AssessmentJudgementAppServiceImpl.finalizeScore()`，移除 `.comment(command.comment())`
- [x] 1.5 修改 `AssessmentJudgementAppServiceImpl.propagateFinalizedJudgementToTeamMembers()`，移除 `command.comment()` 传播

## 2. 后端 — 移除自动判题评语

- [x] 2.1 修改 `FormalJudgeWorkflow.buildJudgement()`，删除 `.comment("自动判题完成...")` 赋值

## 3. 后端 — 删除评论过滤逻辑

- [x] 3.1 删除 `AssessmentAnswerAppServiceImpl.filterMemberCommentsOnly()` 方法
- [x] 3.2 修改 `AssessmentAnswerAppServiceImpl.toAnswerResult()`，直接传入原始 `comments` 列表

## 4. 后端 — 清理评判结果中的 comment 传递（兼容层）

- [x] 4.1 修改 `AssessmentAnswerAppServiceImpl.toJudgementResult()`，`comment` 参数固定传 `null`
- [x] 4.2 修改 `AssessmentJudgementAppServiceImpl.toResult()`，`comment` 参数固定传 `null`
- [x] 4.3 修改 `AssessmentJudgementAppServiceImpl.getLatestByAnswerId()` 返回值中 `comment` 为 `null`
- [x] 4.4 修改 `AssessmentJudgementAppServiceImpl.convertJudgementFromScoreRow()`，不传 `comment`

## 5. 前端管理端 — 移除评分评语输入

- [x] 5.1 修改 `score/page.tsx`，form 定义中删除 `comment` 字段
- [x] 5.2 修改 `score/page.tsx`，移除表单初始化时的 `comment` 赋值
- [x] 5.3 修改 `score/page.tsx`，移除 `handleFinalizeScore` 提交时的 `comment` 参数
- [x] 5.4 修改 `score/page.tsx`，删除 JSX 中 `<Form.Item name="comment" label="评语">` 块

## 6. 前端用户端 — 移除评语显示

- [x] 6.1 修改 `QuestionSidebar.tsx`，删除 `answer.judgement.comment` 显示块（第 204-211 行）

## 7. 测试更新

- [x] 7.1 更新 `AssessmentJudgementAppServiceImplTest` 中 finalizeScore 相关测试，移除 comment 断言
- [x] 7.2 更新 `AssessmentAnswerAppServiceImplTest` 中评论过滤相关测试
- [x] 7.3 更新 `AdminCommentControllerIntegrationTest` 中需要调整的测试用例
- [x] 7.4 运行后端单元测试和集成测试，确保全部通过

## 8. 验证

- [x] 8.1 编译打包后端
- [x] 8.2 构建 Docker 镜像并启动
- [x] 8.3 前端验证：管理端评分页面不再显示评语输入框
- [x] 8.4 前端验证：用户端考题界面不再显示评语，仍显示成员评论列表
- [x] 8.5 端到端验证：最终评分流程正常（只传分数，不传评语）
