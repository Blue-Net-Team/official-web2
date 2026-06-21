## 1. Repository 与 Mapper 批量方法

- [x] 1.1 在 `AssessmentQuestionRepository` 接口新增 `Map<Long, Integer> countByAssessmentTimeIds(List<Long> assessmentTimeIds)`
- [x] 1.2 在 `AssessmentQuestionMapper` Java 接口和 XML 中实现 `countByAssessmentTimeIds`，使用 `IN` 子句按 `assessment_time_id` 分组计数
- [x] 1.3 在 `AssessmentQuestionRepositoryImpl` 中实现批量 count，处理空列表返回空 Map，并将 `Long` 计数转为 `Integer`
- [x] 1.4 在 `AssessmentAnswerRepository` 接口新增 `Map<Long, Integer> countByUserIdAndAssessmentTimeIds(Long userId, List<Long> assessmentTimeIds)`
- [x] 1.5 在 `AssessmentAnswerMapper` Java 接口和 XML 中实现 `countByUserIdAndAssessmentTimeIds`，JOIN `tb_assessment_question` 后按 `assessment_time_id` 分组计数
- [x] 1.6 在 `AssessmentAnswerRepositoryImpl` 中实现批量 count，处理空列表返回空 Map
- [x] 1.7 在 `AssessmentTimeRepository` 接口新增 `List<AssessmentTime> findAllById(List<Long> ids)`
- [x] 1.8 在 `AssessmentTimeMapper` Java 接口和 XML 中实现 `findAllById`，使用 `IN` 子句
- [x] 1.9 在 `AssessmentTimeRepositoryImpl` 中实现 `findAllById`，处理空列表返回空列表

## 2. 自定义结果对象

- [x] 2.1 创建 `AssessmentQuestionCountResult` / `AssessmentAnswerCountResult` record 作为 MyBatis 批量 count 的返回类型
- [x] 2.2 在对应 Mapper XML 中使用新 record 作为 `resultType`
- [x] 2.3 在 RepositoryImpl 中将 record 列表转换为 `Map<Long, Integer>`

## 3. 应用服务层重构

- [x] 3.1 修改 `AssessmentTimeAppServiceImpl.listAssessmentTimesForUser`，在 `Page.map()` 前收集所有 `assessmentTimeId`
- [x] 3.2 在循环外调用批量 count 方法，分别得到 `totalQuestionCounts` 和 `completedQuestionCounts`
- [x] 3.3 在循环外预加载当前用户的淘汰决策列表 `eliminatedDecisions`
- [x] 3.4 在循环外调用 `AssessmentTimeRepository.findAllById` 加载所有决策关联的考核场次，并构建 `Map<Long, AssessmentTime>`
- [x] 3.5 在 `Page.map()` 回调内从内存 Map 中取值，并调用改造后的淘汰判断方法
- [x] 3.6 确保 `getAssessmentProgress` 等仍使用原有单条 count 方法的调用方不受影响

## 4. 领域服务层重构

- [x] 4.1 在 `AssessmentDecisionDomainService` 接口新增支持预加载数据的淘汰判断方法签名
- [x] 4.2 在 `AssessmentDecisionDomainServiceImpl` 中实现新的 `isEliminatedFromPriorEpoch` 重载，接收 `targetTime`、`List<AssessmentDecisionVO>`、`Map<Long, AssessmentTime>`
- [x] 4.3 新实现中复用现有的 `isSameDirectionAndGrade` 和 `isPriorEpoch` 私有方法，保持判断逻辑不变
- [x] 4.4 保留原有 `isEliminatedFromPriorEpoch(Long userId, AssessmentTime targetTime)` 方法作为兼容入口，内部调用新重载或保持原实现

## 5. 测试

- [x] 5.1 为 `AssessmentQuestionRepository.countByAssessmentTimeIds` 编写单元测试，覆盖空列表、部分命中、全部命中场景
- [x] 5.2 为 `AssessmentAnswerRepository.countByUserIdAndAssessmentTimeIds` 编写单元测试，覆盖空列表、部分命中、全部命中场景
- [x] 5.3 为 `AssessmentTimeRepository.findAllById` 编写单元测试，覆盖空列表、部分命中、全部命中场景
- [x] 5.4 为 `AssessmentDecisionDomainServiceImpl` 新的内存判断方法编写单元测试，覆盖全局考核、方向考核、不限年级、epoch 边界
- [x] 5.5 更新 `AssessmentTimeAppServiceImpl` 集成测试，验证批量查询路径下的 `totalQuestions`、`completedQuestions`、`eliminated` 与原逻辑一致
- [x] 5.6 运行相关单元测试和集成测试，确保全部通过

## 6. 编译与打包验证

- [x] 6.1 执行 `mvnw clean compile` 确认后端编译通过
- [x] 6.2 执行 `mvnw test` 确认新增和既有测试通过
- [x] 6.3 构建 Docker 镜像 `bluenet-api-service:latest` 并启动容器
- [ ] 6.4 使用 Playwright 或 API 调用验证 `GET /api/v1/assessment-times` 响应正确（等待用户提供/授权测试账号）

## 7. Issue 关联与归档

- [x] 7.1 在提交信息中引用 issue #30、#31、#32（使用 `ref #30`、`ref #31`、`ref #32`）
- [ ] 7.2 完成后更新三个 issue 的进度说明
- [ ] 7.3 用户确认后调用 `/opsx:archive` 归档变更
