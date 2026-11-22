# BlueNet API/Judge 内存压测

用于本地 Docker Compose 环境的可重复业务压测，覆盖报名、考核作品文件上传、多道 Python 算法题运行与正式提交。目标是给 JVM/容器调优提供同负载对照数据，不是生产容量承诺。

## 前置条件

- Windows 本机安装 k6，并确保 `k6 version` 可用；当前验证版本为 v2.3.0。
- Docker / Docker Compose 可用，API 与 Judge 镜像已构建。
- 仓库根目录存在 `docker/.env`；系统管理员与 RabbitMQ 凭据从该文件读取，不打印到结果。
- 本地端口 8080、8090、7000、15672 可供对应服务使用。
- Python 命令行入口使用 Click，依赖由 uv 管理；首次运行前执行 `uv sync --project src/load-test`。
- 执行 Node.js 聚焦测试需要 Node.js；日常 k6 运行不依赖 Node 包安装。

## 危险操作：本地环境重置

`--reset` 会删除本地 `bluenet` / `ENV=dev` 的 PostgreSQL、Redis、RabbitMQ、MinIO/RustFS 数据卷并重建服务。只允许在明确不再需要这些本地数据时执行。

PowerShell 中必须显式设置确认：

```powershell
$env:LOADTEST_RESET_CONFIRM = "DELETE"
```

先查看 reset dry-run，不执行删除：

```powershell
uv run --project src/load-test python src/load-test/setup/reset.py --project bluenet --env-name dev --confirm-project bluenet
```

实际重置：

```powershell
uv run --project src/load-test python src/load-test/setup/reset.py --project bluenet --env-name dev --confirm-project bluenet --execute
```

脚本会等待 API/Judge 健康。空数据库由 Flyway 重建，系统管理员由现有 `SystemUserInitializer` 创建。业务数据随后全部由 k6 调用 API 初始化，不执行手写 SQL。

## 推荐执行方式

在仓库根目录使用统一编排器。它会保存 k6 摘要、容器/JVM/RabbitMQ 时序和报告：

```powershell
# 报名：30 秒、10 个新报名/秒
uv run --project src/load-test python src/load-test/run.py enrollment --reset --run-id enrollment-baseline-01

# 文件考核：正式窗口 5 分钟，至少 200 个完整上传答题人次
uv run --project src/load-test python src/load-test/run.py file --reset --run-id file-baseline-01

# Python 第一步：只创建 15 道题并运行标准解 benchmark，不提交考生任务
$env:ALGORITHM_PREPARE_OUTPUT = "src/load-test/results/algorithm-prepared-context.json"
k6 run src/load-test/scenarios/prepare-algorithm.js

# Python 第二步：按每题 ceil(maxTimeMs)+100ms 确认限时
uv run --project src/load-test python src/load-test/setup/measure_algorithm_limits.py `
  src/load-test/results/algorithm-prepared-context.json `
  src/load-test/results/algorithm-confirmed-context.json
# k6 open() 按脚本目录解析相对路径，因此这里传绝对路径
$env:ALGORITHM_CONTEXT_FILE = (Resolve-Path "src/load-test/results/algorithm-confirmed-context.json").Path

# Python 第三步：100 名在线用户，默认 2.5 任务/秒、30 分钟
uv run --project src/load-test python src/load-test/run.py algorithm --run-id algorithm-baseline-01

# 高峰 5 任务/秒、短时尖峰 10 任务/秒（可直接修改 ALGORITHM_DURATION）
$env:ALGORITHM_RATE = "5"
uv run --project src/load-test python src/load-test/run.py algorithm --run-id algorithm-peak-01
$env:ALGORITHM_RATE = "10"
uv run --project src/load-test python src/load-test/run.py algorithm --run-id algorithm-spike-01
Remove-Item Env:ALGORITHM_RATE
```

只进行单用户/小样本预跑：

```powershell
uv run --project src/load-test python src/load-test/run.py enrollment --preflight --run-id enrollment-preflight
uv run --project src/load-test python src/load-test/run.py file --preflight --run-id file-preflight
uv run --project src/load-test python src/load-test/run.py algorithm --preflight --run-id algorithm-preflight
```

文件正式阶段之前已经完成用户创建和登录；201 是 k6 在当前版本/调度边界下的实际完成数，验收阈值为至少 200，不将登录请求计入有效人次。

## 基线配置

`config/baseline.js` 固定：

- 100 个测试考生与独立预登录会话；文件场景 VU 与用户一对一，算法到达率模式按全局样本轮转在线用户。
- 1 KiB 固定 ZIP fixture。
- 15 道不同题型、独立 READY 判题配置的 Python 算法题：两数相加、数组求和/最大值/排序、闭区间求和、GCD、素数、斐波那契、阶乘、字符串反转、回文、字符计数、二分查找、去重计数、矩阵求和。AC 提交使用当前题自己的标准解。
- 每题主标准解重复测速 5 次，正式限时来自真实 `maxTimeMs` 向上取整再加 100ms，不使用生产建议公式或固定 5000ms。
- 算法准备上下文和确认上下文只保存题目、用户与测量数据，不保存登录会话或密码；正式场景 setup 会重新登录。准备完成后不能在正式场景前使用 `--reset` 删除这些数据。
- 算法默认 2.5 任务/秒、30 分钟，运行/提交约 10:2；AC/WA/RE/TLE 为 63%/25%/10%/2%。
- 可用 `ALGORITHM_RATE`、`ALGORITHM_DURATION` 切换到 5/s 高峰、10/s 尖峰或完整 2 小时。
- 每 VU 同时最多一个非终态任务；任务 SUCCEEDED 后仍校验实际 case outcome 等于该样本预期。
- 1 秒判题轮询间隔与 120 秒任务超时。

修改负载后应使用新 runId，并在报告中明确实验差异。JVM 对照实验必须保持镜像、数据和负载一致，每轮只改变一个变量，并通过 `--unique-variable` 记录：

```powershell
uv run --project src/load-test python src/load-test/run.py algorithm --reset --run-id algorithm-xmx384-a --unique-variable "judge.Xmx=384m"
```

## 阶段与指标

监控使用统一 runId、UTC 时间戳和阶段文件，覆盖 `idle/preflight/warmup/steady/drain/recovery`。编排器的 warmup→steady 转换采用固定 10 秒节奏；对照实验必须保持相同设置。

- Docker：API/Judge RSS、limit、CPU、OOMKilled、重启数。
- Actuator：Heap 内存池求和、Non-heap、直接缓冲区、GC、存活线程。
- RabbitMQ：分别采集 run/formal ready 和 unacked。
- k6：业务闭环成功/失败、延迟、吞吐量及各算法题/操作类型/预期判题结果标签。

Judge 容器内存包含 JVM 和 Python/isolate 子进程；不能只根据 Heap 判断容器总内存。Heap used 回落但 RSS 不回落也不能直接判定泄漏。

## 结果

每轮生成：

```text
results/<runId>/
├── metadata.json
├── k6-summary.json
├── containers.jsonl
├── jvm.jsonl
├── queues.jsonl
├── report.json
└── report.md
```

结果目录默认不入库。报告不包含密码、Cookie、JWT、CSRF、预签名 URL 或密钥。采样失败、队列未排空、任务超时和提前中止会保留部分结果并标记不完整。

## 聚焦测试

```powershell
npm test --prefix src/load-test
uv run --project src/load-test python -m unittest discover -s src/load-test/tests -p 'test_*.py'
```

## 本地 Kubernetes 外部指标对照（可选）

Docker Desktop Kubernetes 压测可使用 `deploy/local-observability/README.md` 中的集群外 Prometheus/Grafana。它仅提供 Pod 资源、HTTP、JVM 和中间件历史曲线；本目录的 k6 吞吐、有效业务闭环定义、JSONL 采样和报告保持独立，不依赖 Prometheus/Grafana，也不由这些系统替代。

对照时在 Grafana 选择与实验报告相同的绝对时间范围；Grafana 使用 Asia/Shanghai，实验报告时间戳为 UTC。应用 HTTP 请求计数不等于 k6 的有效业务完成数；未启动外部监控时，本目录现有场景和报告仍可独立运行。

## 停止与回滚

停止当前 k6 进程即可中止负载；编排器仍会尝试保存部分结果。若需要恢复业务服务，只需去掉 `--reset` 并不再使用 `setup/docker-compose.loadtest.yml` 的压测 override。API Actuator metrics 暴露只由该 override 开启，不新增公网端口。

本工具不会自动修改 JVM 参数。任何调优建议都应引用实验 runId，并作为独立配置变更评审。
