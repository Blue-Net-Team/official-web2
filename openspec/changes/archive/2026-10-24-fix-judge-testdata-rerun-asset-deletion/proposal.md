## Why

重跑「生成测试数据」时，`TestDataGenerationWorkflow` 在沙箱生成**之后**才删除旧测试用例对象；而对象键是内容寻址的（`questions/{questionId}/current/testcases/{caseNo:04d}-{sha256(内容)}.in|out`），generator 源码与 `generatorArgs` 不变时新旧键完全相同，删除动作把刚上传的活对象一并删掉，`BenchmarkWorkflow` 随后读取即抛 `NoSuchKey`，配置被永久置为 `FAILED`（ref #85）。

## What Changes

- 将旧测试用例对象清理从「生成并落库之后」提前到「读取旧记录之后、沙箱生成之前」，即**先清理上一版资产，再生成并覆盖**
- 明确清理语义为「重跑即顶替上一版」：本次生成一旦失败，上一版的测试用例资产不再保留，配置整体随本次结果一起变为 `FAILED`
- 补充回归测试，用 `InOrder` 锁死「先 `delete` 后 `put`」的执行顺序，并覆盖「同一内容连续生成两次」的读取可用性

不在本次范围：

- MQ 侧毒性消息治理（重试上限 / 死信队列）。issue 中「任何确定性异常都触发无限重投」的问题仍按 #85 修复建议第 3 条单独处理
- 键布局改为版本前缀（`.../testcases/{configVersion}/...`）
- `generator` / `manifest` / `standard` 三类对象的历史资产清理策略

## Capabilities

### New Capabilities

<!-- 无新增能力，本次仅修正既有生成流程内的资产清理时序 -->

### Modified Capabilities

- `judge-test-data-generation`: 「Test data updates replace current configuration」需求新增重跑时的资产清理时序约束，以及生成失败不保留上一版资产的行为约定

## Impact

**代码**

- `src/judge-service/.../application/service/workflow/TestDataGenerationWorkflow.java`
  - `handle()` 中 `deleteOldTestcaseAssets(oldTestCases)` 调用位置从落库/日志之后移动到旧记录查询之后
  - `deleteOldTestcaseAssets` 的 Javadoc 语意由「删除旧的测试用例 OSS 资产」改为「生成前清理上一版测试用例资产」
- `src/judge-service/.../application/service/workflow/TestDataGenerationWorkflowTest.java`
  - 新增顺序断言与重跑可用性测试

**行为**

- 同一配置在 generator 与 `generatorArgs` 未变时重跑，`BenchmarkWorkflow` 能正常读取测试用例对象
- 生成失败时不再保留上一版资产；数据库记录随本次替换一起被覆盖或保持失败状态

**数据**

- 已有 `NoSuchKey` 失效配置无需人工修数据：修复后重新触发一次生成，内容寻址键相同，对象会被重新写回
- 队列中堆积的 unacked 消息需在上线前后清空，避免新旧镜像并行消费

**不受影响**

- 前端、`ai-service`、主应用 `backend`（`upsert` 的键生成与 `deleteByPrefix` 均不变）
- 判题 bucket 布局、manifest 结构、语言限时确认流程
- 正式判题与自测判题链路
