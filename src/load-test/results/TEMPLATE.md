# 实验记录模板

- runId：
- 场景：enrollment / file / algorithm
- 日期与执行人：
- Git revision：
- API/Judge 镜像 ID：
- Docker/OS/压测机资源：
- k6 版本：
- 唯一调优变量（没有则填 baseline）：
- 其他输入是否一致：

## 负载

- VU/到达速率/持续时间：
- 用户池规模：
- 文件大小或算法题数量：
- Python 运行/提交比例及总任务数：
- 各阶段时间：idle / preflight / warmup / steady / drain / recovery

## 业务正确性

- 有效业务样本：
- 失败/冲突/超时：
- dropped iterations：
- HTTP 与业务错误率：
- P95 / P99：
- run/formal 队列峰值与最终深度：

## 内存与 JVM

| 服务 | 空闲 RSS MiB | 峰值 RSS MiB | 回收期 RSS MiB | Heap used 峰值 | Heap committed 峰值 | Non-heap 峰值 | 最大线程 | GC 次数/累计停顿 | OOM/重启 |
|---|---:|---:|---:|---:|---:|---:|---:|---|---|
| API | | | | | | | | | |
| Judge | | | | | | | | | |

## 数据完整性与结论

- 缺失样本/采样失败：
- 实验是否完整：
- 观察到的现象：
- 可支持的调优假设：
- 需要追加的实验：

> 仅描述同环境、同负载下的结果，不将单次短时峰值表述为生产容量。
