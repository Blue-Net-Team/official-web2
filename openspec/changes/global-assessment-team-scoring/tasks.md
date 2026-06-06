## 1. 后端 - 组队生命周期管理

- [x] 1.1 `AssessmentTeamAppServiceImpl`：新增 `hasTeamSubmittedAnswer(teamId)` 辅助方法，检查队伍是否已提交 FILE_UPLOAD 答案
- [x] 1.2 `AssessmentTeamAppServiceImpl.leaveTeam()`：增加"队伍已提交答案"锁定检查，已锁定则抛 Forbidden
- [x] 1.3 `AssessmentTeamAppServiceImpl.transferLeader()`：增加"队伍已提交答案"锁定检查
- [x] 1.4 `AssessmentTeamAppServiceImpl.disbandTeam()`：增加"队伍已提交答案"锁定检查；解散前清理该队伍所有 answer 和关联 judgement
- [x] 1.5 `AssessmentTeamAppServiceImpl.joinTeam()`：增加 `hasTeamAnswer()` 检查，已有队伍答案者禁止加入新队伍
- [x] 1.6 编写上述锁定和清理逻辑的单元测试

## 2. 后端 - 组员 answer 自动创建与同步

- [x] 2.1 `AssessmentAnswerAppServiceImpl.createAnswer()`：队长提交 FILE_UPLOAD 答案后，遍历组员批量创建 answer 记录（事务包裹）
- [x] 2.2 `AssessmentAnswerAppServiceImpl.updateAnswer()`：队长更新 FILE_UPLOAD 答案后，同步更新所有组员 answer 记录
- [x] 2.3 `AssessmentAnswerRepository`：新增 `findByTeamIdAndQuestionId(teamId, questionId)` 和 `deleteByTeamId(teamId)` 方法
- [x] 2.4 `AssessmentAnswerMapper.xml`：实现上述 Repository 方法对应的 SQL
- [x] 2.5 编写组员 answer 自动创建和同步的单元测试

## 3. 后端 - 评分查询支持 team_id 展开

- [x] 3.1 `AssessmentJudgementMapper.xml.selectQuestionSubmissions`：写时复制方案下组员已有独立 answer 记录，无需修改 SQL
- [x] 3.2 `AssessmentJudgementMapper.xml.selectCandidateScoreRows`：写时复制方案下组员已有独立 answer 记录，无需修改 SQL
- [x] 3.3 `AssessmentJudgementMapper.xml.selectQuestionScoreboard`：写时复制方案下组员已有独立 answer 记录，无需修改 SQL
- [x] 3.4 `AssessmentJudgementMapperIntegrationTest`：补充全局考核 + 组队场景的集成测试
- [x] 3.5 `AssessmentJudgementAppServiceImpl.finalizeScore()`：支持为所有组员创建 judgement（全队同分）
- [x] 3.6 编写评分查询和团队评分的单元测试

## 4. 后端 - 全局考核评分/决策支持

- [x] 4.1 `AssessmentJudgementAccessGuard`：全局考核时 `assessmentTime.getDirection() == null` 跳过方向校验，逻辑正确
- [x] 4.2 `AssessmentJudgementAppServiceImpl.publishDecisions()`：全局考核邮件方向标签已显示为"全局"
- [x] 4.3 `AssessmentDecisionDomainService`：全局考核淘汰限制逻辑已覆盖（`direction=null` 时按 epoch 比较）
- [x] 4.4 编译通过；测试失败为环境已有问题（Mockito session、Testcontainers），非回归

## 5. 前端 - 评分/决策页面全局选项

- [x] 5.1 `score/page.tsx`：方向选择器增加"全局"选项（SUPER_ADMIN 可见）
- [x] 5.2 `score/page.tsx`：`fetchAssessmentTimes` 支持加载 `direction=null` 的全局考核
- [x] 5.3 `decision/page.tsx`：方向选择器增加"全局"选项
- [x] 5.4 `decision/page.tsx`：`fetchAssessmentTimes` 支持加载全局考核
- [x] 5.5 两个页面的 `timeOptions` 显示兼容 `direction=null`（显示"全局"标签）

## 6. 前端 - 组队规则声明

- [x] 6.1 `TeamPanel.tsx`：增加组队规则 Alert/折叠面板，包含 5 条规则声明
- [x] 6.2 `TeamPanel.tsx`：解散确认弹窗增加"已提交答案将被删除"警告
- [x] 6.3 `TeamPanel.tsx`：退出确认弹窗增加"退出后可重新加入"提示
- [x] 6.4 前端类型定义 `DirectionOrGlobal = Direction | 'GLOBAL'` 已支持，tsc --noEmit 通过

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
