## 1. 回归测试（先红）

- [x] 1.1 在 `TestDataGenerationWorkflowTest` 新增顺序测试：给定配置已存在旧测试用例记录，`handle()` 执行后被调用的 `judgeAssetStorage.delete(...)` 全部早于 `judgeAssetStorage.put(...)`，用 Mockito `InOrder` 断言
- [x] 1.2 新增「同一内容连续生成两次」测试：用记录状态的 `JudgeAssetStorage` 替身（`delete` 移除键、`put` 写入键与内容），第一次生成后第二次重跑（generator 与标准解输出保持字节一致），断言 `findTestCasesByConfigId` 返回记录的 `inputObjectKey` / `outputObjectKey` 在替身中仍可读
- [x] 1.3 新增失败路径测试：generator 在重跑时失败，断言配置被标记 `FAILED`、异常向上抛出，且不再保留上一版资产（替身中旧键已被清除）
- [x] 1.4 确认新增测试在当前实现下失败（红灯），并记录失败表现与 issue #85 的 `NoSuchKey` 一致

## 2. 工作流顺序修复

- [x] 2.1 将 `TestDataGenerationWorkflow.handle()` 中 `deleteOldTestcaseAssets(oldTestCases)` 的调用从落库/日志之后移动到读取旧记录之后、加载 manifest 之前
- [x] 2.2 更新 `deleteOldTestcaseAssets` 的 Javadoc，明确语义为「生成前清理上一版测试用例资产」，并说明清理依赖后续必然发生的对象写入
- [x] 2.3 在 `deleteOldTestcaseAssets` 与 `uploadAndBuildRecord` 处补注释，写明内容寻址键的前提：清理必须早于写入，若未来引入「hash 相同则跳过上传」的优化需重新评估该顺序
- [x] 2.4 确认第 1 组测试全部转绿（绿灯），且未修改任何测试断言以迁就实现

## 3. 回归验证

- [x] 3.1 在 `src/judge-service` 执行 `mvnw clean compile package`，确认编译与全部单元测试通过
- [x] 3.2 复核现有 `handle_withOldTestCases_shouldDeleteOldAssets` 测试仍成立（旧键不同、删除被调用），并确认其不再被当作该 bug 的保护测试使用
- [x] 3.3 检查 `deleteOldTestcaseAssets` 的单键删除失败仍只记 warn 日志、不中断主流程，对应 spec 中「Object removal failure does not abort generation」

## 4. 部署与线上恢复

- [x] 4.1 构建镜像 `bluenet-judge-service:latest`，按 `docker/docker-compose.yml` 替换 judge-service 容器（实际由 CD 构建并部署为 `judge-local-20261024-9ae17cd`）
- [x] 4.2 确认 `algorithm.judge.test-data` 无重投循环（实际无需 purge：新镜像消费掉积压消息后队列自然归零，见下条）
- [x] 4.3 验证重跑结果：`tb_judge_problem_config` 中 question 2 / config 1 状态为 `READY`，`tb_judge_test_case` 行数（1）与 manifest 中 testcase 数量一致
- [x] 4.4 验证日志：新镜像在 19:05:28~19:05:33 连续消费 4 条积压消息，每条均 `测试数据生成完成` + `Benchmark 完成，结果=READY`，`NoSuchKey` 出现 0 次，队列 ready/unacked 均为 0
