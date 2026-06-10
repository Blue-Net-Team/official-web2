## 1. Repository & Mapper 扩展

- [ ] 1.1 `AssessmentJudgementRepository` 接口新增 `findLatestByAnswerIdAndSource(Long answerId, JudgementSource source)`
- [ ] 1.2 `AssessmentJudgementRepository` 接口新增 `findAnswerIdsBySource(List<Long> answerIds, JudgementSource source)`
- [ ] 1.3 `AssessmentJudgementMapper` 新增 `selectLatestByAnswerIdAndSource` SQL
- [ ] 1.4 `AssessmentJudgementMapper` 新增 `selectAnswerIdsBySource` SQL（IN 查询，返回已有 ADMIN_FINALIZED 的 answer_id 列表）
- [ ] 1.5 `AssessmentJudgementRepositoryImpl` 实现上述两个新方法

## 2. DomainService 覆盖更新

- [ ] 2.1 `AssessmentJudgementDomainServiceImpl.finalizeJudgement()` 改为"先查后更新"：查询该 answer_id + ADMIN_FINALIZED，存在则 update，不存在则 save
- [ ] 2.2 `AssessmentJudgementDomainService` 接口无需变更（实现行为变化，签名不变）

## 3. AppService 传播逻辑重构

- [ ] 3.1 `AssessmentJudgementAppServiceImpl.finalizeScore()` 重写主流程：判断当前 answer 是否已有 ADMIN_FINALIZED，区分首次/再次评分
- [ ] 3.2 新增私有方法 `finalizeSingleAnswer()` — 构建 JudgementVO 并调用 `domainService.finalizeJudgement()`，供个人/队员/队长再次评分复用
- [ ] 3.3 新增私有方法 `finalizeTeamFirstTime()` — 队长首次评分：更新队长 + 批量传播给无评分队员
- [ ] 3.4 删除旧方法 `propagateFinalizedJudgementToTeamMembers()` 或其内部逻辑替换为新实现
- [ ] 3.5 确保 `finalizeScore()` 的 `@Transactional` 事务边界覆盖全部操作

## 4. 测试更新

- [ ] 4.1 更新 `AssessmentJudgementDomainServiceImplTest`：新增"同一 answer 多次 finalize 应覆盖更新"的测试用例
- [ ] 4.2 更新 `AssessmentJudgementAppServiceImplTest`：
  - 个人答题多次 finalize 覆盖更新
  - 队长首次 finalize 传播给无评分队员
  - 队长再次 finalize 只更新自己、不传播
  - 队员单独 finalize 只更新自己
  - 队长首次 finalize 时部分队员已有评分则跳过这些队员
- [ ] 4.3 如有集成测试涉及评判历史记录数量断言，同步更新预期值

## 5. 验证

- [ ] 5.1 后端编译通过：`./mvnw clean compile`
- [ ] 5.2 单元测试全部通过：`./mvnw test -Dtest=AssessmentJudgementDomainServiceImplTest,AssessmentJudgementAppServiceImplTest`
