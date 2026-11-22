# Change: Measure load-test judge limits from benchmark max

## Why
算法压测不能继续把每个用例的限时固定为 5000ms。限时必须来自待压测题目在真实 Judge benchmark 中的最大标准解耗时，避免限时不合理导致非预期 TLE。

## What Changes
- k6 提供仅创建题目、生成测试数据并采集 benchmark 结果的准备模式，不执行考生判题负载。
- 15 道压测题使用不同题型、题面、标准解和测试输入；AC 考生代码必须对应当前题，不再重复两数相加。
- Python 工具校验每道题的 Python benchmark 为 DONE，按 `ceil(maxTimeMs) + 100ms` 计算限时并调用管理员确认接口。
- 正式算法压测必须读取已测速、已确认的上下文；准备上下文写入磁盘时移除登录会话。
- 不修改生产 benchmark 的建议限时公式；该问题由后续独立变更处理。

## Impact
- 仅影响本地压测工具与文档，不修改生产 API/Judge 行为。
- 准备阶段会执行有限次数的标准解 benchmark，但不启动 run/submit 压测。
