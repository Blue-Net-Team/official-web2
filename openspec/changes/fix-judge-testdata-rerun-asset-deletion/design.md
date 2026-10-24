## Context

`TestDataGenerationWorkflow.handle(configId)` 的执行顺序当前为：读取旧测试用例记录 → 加载 manifest/generator/标准解 → 沙箱生成并上传 `.in`/`.out` → 事务内 `replaceGeneratedTestCases` → 删除旧记录指向的对象 → 标记 `GENERATED` → `BenchmarkWorkflow.handle`。

判题 bucket 内的对象键由内容哈希决定：

```
questions/{questionId}/current/testcases/{caseNo:04d}-{sha256(内容)}.in
questions/{questionId}/current/testcases/{caseNo:04d}-{sha256(内容)}.out
```

因此当 generator 源码与 `generatorArgs` 未变时，重跑产生的键与上一版**完全相同**。现有顺序下，第 5 步删除的是第 3 步刚写入的同一批对象，第 7 步 benchmark 读取即 `NoSuchKey`。同一失败还会被无重试上限、无死信的 `TestDataGenerationListener` 无限重投，把问题放大为持续空转。

本次的约束是：hotfix 范围内不改对象键布局、不改 MQ 语义、不引入跨服务契约变更。

## Goals / Non-Goals

**Goals:**

- 重跑时不再删除本次生成写入的对象，使 `BenchmarkWorkflow` 能正常读取
- 修正后的清理语义对「内容未变的重跑」成立，无需依赖生成内容是否随机
- 用测试锁住删除与写入的相对顺序，防止顺序被再次改回
- 提供线上失效配置的恢复与队列积压处置步骤

**Non-Goals:**

- MQ 重试上限 / 死信队列（`TestDataGenerationListener` 的毒性消息治理）
- 对象键改为版本前缀（`.../testcases/{configVersion}/...`）
- `generator` / `manifest` / `standard` 三类对象的历史资产清理
- 生成失败的异常分类与自动恢复
- 前端或主应用 `backend` 的任何改动

## Decisions

### Decision 1：把清理提前到生成之前，而不是做差集删除

`deleteOldTestcaseAssets(oldTestCases)` 由「落库之后」移动到「读取旧记录之后、加载 manifest/生成之前」。

理由：清理的语义变成「重跑即顶替上一版」，先腾出旧对象空间，再写入本次对象，顺序上不存在「删除动作晚于写入动作」的窗口，`NoSuchKey` 从结构上消失。改动量为一次调用位置迁移。

替代方案 A：差集删除（只删 `旧键集合 − 新键集合`）。判据本身正确，能同时保住「失败保留旧状态」与「将来给上传加 hash 相同则跳过写」的优化空间，但需要构造键集合并增加一段集合运算逻辑。

替代方案 B：键带版本前缀。结构性免疫键复用，且顺带解决多配置共存问题，但需要改主应用的键生成、`deleteByPrefix` 前缀以及存量数据迁移，属跨服务契约变更，hotfix 范围外。

取舍：选择顺序修复而非判据修复，换取最小改动与最短评审路径；代价见 Risks。

### Decision 2：接受「生成失败不保留上一版资产」

重跑一旦失败，上一版测试用例对象已在生成前被清理，配置直接进入 `FAILED`，不会回退到上一次成功的测试数据。

理由（产品侧已确认）：重跑本身就代表管理员要顶替上一版，新的失败结果应当顶掉旧的。若失败后仍让旧数据继续可判，会出现「配置状态为 FAILED、但正式判题仍在用旧用例」的语义分叉，与「只维护当前测试数据配置」的既有需求冲突。

替代方案：把清理放到 benchmark 成功、状态置 `READY` 之后。这样失败时旧资产完好，但会重新引入「删除发生在使用之后」的时序，且删除点进一步远离生成点、更难推理，与本次目标相悖。

### Decision 3：回归测试用 `InOrder` 锁顺序，而不是锁删除次数

现有测试构造的旧键是 `0001-old.in`，与真实生成键天然不同，且 `JudgeAssetStorage` 是 mock、`put` 为空操作，因此无论顺序如何该测试都是绿的——它是这个 bug 得以合入的直接原因。

新的顺序测试断言：在同一次 `handle` 中，`judgeAssetStorage.delete(...)` 的全部调用先于 `judgeAssetStorage.put(...)` 的全部调用。重跑可用性测试则改用记录状态的存储替身（`delete` 移除键、`put` 写回键），验证「内容不变、连续生成两次」后旧记录键与本次生成键相同且对象可读。

替代方案：只断言「不再删除与本次生成键相同的对象」。该断言在差集方案下最自然，但在顺序方案下需要额外模拟 put 的回写语义，且无法阻止未来把调用位置挪回生成之后。

### Decision 4：不修改 MQ 消费语义

`TestDataGenerationListener` 保持现状，任何异常仍会 rethrow 并触发重投。本次仅确保该异常不再由正常重跑路径产生。

理由：重试上限与死信涉及交换机/队列参数变更，需要与已在队列中的持久化消息及主应用侧的队列声明（`AlgorithmJudgeQueueConfig`）同步协调，不适合塞进这个 hotfix。作为独立问题跟踪。

## Risks / Trade-offs

- [生成失败丢弃上一版可用数据] → 已在 Decision 2 中作为产品决策接受；失败后由管理员重试恢复。若失败原因与数据无关（沙箱资源、OSS 抖动），恢复窗口取决于重试时机
- [将来给上传加「hash 相同则跳过 put」的优化会让此 bug 复现] → 在 `deleteOldTestcaseAssets` 与 `uploadAndBuildRecord` 处保留注释，说明「清理依赖后续必有写入」；`InOrder` 测试只锁顺序，无法表达该前提，故必须靠注释与评审约束
- [MQ 无限重投仍然存在] → 本次不修；但修复后重跑路径不再产生确定性异常，重投风暴的触发源被消除。issue #85 中的重试上限问题需另行处理
- [并发消费同一 configId 的两条消息] → 顺序方案下两个任务各自「先删旧键再写新键」，若内容不同可能短暂互相覆盖；由于两任务写入的键集合都由各自内容决定、且 benchmark 在写入后读取，影响限于冗余上传，不会产生 `NoSuchKey`
- [上线时新旧镜像同时消费] → 部署前先停止 judge-service 消费者并清空 `algorithm.judge.test-data` 队列中堆积的 unacked 消息，否则旧镜像会继续在重投循环中删除对象

## Migration Plan

1. 停 `bluenet-judge-service` 容器，使 `algorithm.judge.test-data` 不再被消费
2. 清空该队列中残留消息（管理台 purge 或 `rabbitmqctl purge_queue`），确认无 unacked 积压
3. 构建并部署包含本次修复的镜像，启动容器
4. 对受影响的算法题（如 question 2 / judge config 1）在管理端重新触发一次「生成测试数据」；由于键为内容寻址，重新生成会把被删除的同名对象写回，无需手工修复数据或修改数据库
5. 验证：配置状态由 `FAILED`/`GENERATING` 转为 `READY`（或 benchmark 后的等价正常态），judge-service 日志中不再出现 `NoSuchKey`，`tb_judge_test_case` 行数与 manifest 中 testcase 数量一致
6. 回滚：若部署后出现异常，回退到上一个 `judge-0.1.5` 之前的镜像；已删除对象可通过再次触发生成重建，回滚不丢数据

## Open Questions

- 是否需要在本 issue 内同时清掉 `tb_judge_test_case_id_seq` 的历史空洞（`last_value = 5072`）？建议不处理——序列空洞不影响功能，重建序列需改主键生成策略
- `generator` / `manifest` / `standard` 三类对象长期只增不删的孤儿累积，是否另开 issue 统一治理（生命周期规则 / 周期性对账）？
