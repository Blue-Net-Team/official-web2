## 1. 后端 - 组队生命周期管理

- [ ] 1.1 `AssessmentTeamAppServiceImpl`：新增 `hasTeamSubmittedAnswer(teamId)` 辅助方法，检查队伍是否已提交 FILE_UPLOAD 答案
- [ ] 1.2 `AssessmentTeamAppServiceImpl.leaveTeam()`：增加"队伍已提交答案"锁定检查，已锁定则抛 Forbidden
- [ ] 1.3 `AssessmentTeamAppServiceImpl.transferLeader()`：增加"队伍已提交答案"锁定检查
- [ ] 1.4 `AssessmentTeamAppServiceImpl.disbandTeam()`：增加"队伍已提交答案"锁定检查；解散前清理该队伍所有 answer 和关联 judgement
- [ ] 1.5 `AssessmentTeamAppServiceImpl.joinTeam()`：增加 `hasTeamAnswer()` 检查，已有队伍答案者禁止加入新队伍
- [ ] 1.6 编写上述锁定和清理逻辑的单元测试

## 2. 后端 - 组员 answer 自动创建与同步

- [ ] 2.1 `AssessmentAnswerAppServiceImpl.createAnswer()`：队长提交 FILE_UPLOAD 答案后，遍历组员批量创建 answer 记录（事务包裹）
- [ ] 2.2 `AssessmentAnswerAppServiceImpl.updateAnswer()`：队长更新 FILE_UPLOAD 答案后，同步更新所有组员 answer 记录
- [ ] 2.3 `AssessmentAnswerRepository`：新增 `findByTeamIdAndQuestionId(teamId, questionId)` 和 `deleteByTeamId(teamId)` 方法
- [ ] 2.4 `AssessmentAnswerMapper.xml`：实现上述 Repository 方法对应的 SQL
- [ ] 2.5 编写组员 answer 自动创建和同步的单元测试

## 3. 后端 - 评分查询支持 team_id 展开

- [ ] 3.1 `AssessmentJudgementMapper.xml.selectQuestionSubmissions`：修改 SQL 以支持通过 `team_id` 展开组员数据
- [ ] 3.2 `AssessmentJudgementMapper.xml.selectCandidateScoreRows`：同上修改，确保组员显示在人员评分矩阵
- [ ] 3.3 `AssessmentJudgementMapper.xml.selectQuestionScoreboard`：修改统计逻辑，包含组员 answer 记录
- [ ] 3.4 `AssessmentJudgementMapperIntegrationTest`：补充全局考核 + 组队场景的集成测试
- [ ] 3.5 `AssessmentJudgementAppServiceImpl.finalizeScore()`：支持为所有组员创建 judgement（全队同分）
- [ ] 3.6 编写评分查询和团队评分的单元测试

## 4. 后端 - 全局考核评分/决策支持

- [ ] 4.1 `AssessmentJudgementAccessGuard`：确认全局考核时方向校验已跳过（`cross-direction-global-assessment` 已实现）
- [ ] 4.2 `AssessmentJudgementAppServiceImpl.publishDecisions()`：确认全局考核邮件方向标签显示为"全局"
- [ ] 4.3 `AssessmentDecisionDomainService`：确认全局考核淘汰限制逻辑正确
- [ ] 4.4 运行现有测试套件，确认无回归

## 5. 前端 - 评分/决策页面全局选项

- [ ] 5.1 `score/page.tsx`：方向选择器增加"全局"选项（SUPER_ADMIN 可见）
- [ ] 5.2 `score/page.tsx`：`fetchAssessmentTimes` 支持加载 `direction=null` 的全局考核
- [ ] 5.3 `decision/page.tsx`：方向选择器增加"全局"选项
- [ ] 5.4 `decision/page.tsx`：`fetchAssessmentTimes` 支持加载全局考核
- [ ] 5.5 两个页面的 `timeOptions` 显示兼容 `direction=null`（显示"全局"标签）

## 6. 前端 - 组队规则声明

- [ ] 6.1 `TeamPanel.tsx`：增加组队规则 Alert/折叠面板，包含 5 条规则声明
- [ ] 6.2 `TeamPanel.tsx`：解散确认弹窗增加"已提交答案将被删除"警告
- [ ] 6.3 `TeamPanel.tsx`：退出确认弹窗增加"退出后可重新加入"提示
- [ ] 6.4 确认前端类型定义中 `direction` 支持 `"GLOBAL"` 转 `null`

## 7. 验收测试

- [ ] 7.1 创建全局考核 + 开启组队，验证 SUPER_ADMIN 可在评分页面选择
- [ ] 7.2 跨方向组队，队长提交 FILE_UPLOAD 答案，验证组员自动获得 answer 记录
- [ ] 7.3 评分页面题目视图，验证组员显示在提交列表中
- [ ] 7.4 评分页面人员视图，验证组员显示评分矩阵
- [ ] 7.5 人工评分时验证全队同分 + 单独调整某成员
- [ ] 7.6 录用决策页面验证所有组员（含跨方向）出现在候选人列表
- [ ] 7.7 验证队长提交后队员无法退队/转让/解散
- [ ] 7.8 验证解散队伍后 answer 记录被清理
- [ ] 7.9 验证已有队伍答案者无法加入其他队伍
- [ ] 7.10 编译打包后端，验证构建成功
- [ ] 7.11 运行完整测试套件，确认无回归
