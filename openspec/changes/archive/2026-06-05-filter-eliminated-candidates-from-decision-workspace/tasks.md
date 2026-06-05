## 1. 后端过滤逻辑实现

- [x] 1.1 在 `AssessmentJudgementAppServiceImpl.getDecisionWorkspace()` 中加载当前 `AssessmentTime`
- [x] 1.2 对 `scoreboards` 调用 `assessmentDecisionDomainService.isEliminatedFromPriorEpoch()` 过滤 prior epoch 淘汰考生
- [x] 1.3 确保 `calculateDecisionStatistics()` 基于过滤后的 `scoreboards` 计算统计

## 2. 单元测试补充

- [x] 2.1 在 `AssessmentJudgementAppServiceImplTest` 新增/修改测试：prior epoch 淘汰考生应被排除
- [x] 2.2 补充测试：当前轮次淘汰考生仍应保留在工作台中
- [x] 2.3 补充测试：prior epoch 淘汰考生排除后，统计数字正确更新

## 3. 编译与验证

- [x] 3.1 运行后端 Maven 测试，确保新增和原有测试通过（22 tests, 0 failures）
- [x] 3.2 后端打包并构建 Docker 镜像
- [x] 3.3 启动基础设施和 API 服务，通过 API 验证录用决策工作台正确过滤 prior epoch 淘汰考生（43 被排除，44 当前轮次淘汰保留）
