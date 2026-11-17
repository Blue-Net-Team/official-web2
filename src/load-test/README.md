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
# 报名：30 秒、约 1 个新报名/秒
uv run --project src/load-test python src/load-test/run.py enrollment --reset --run-id enrollment-baseline-01

# 文件考核：正式窗口 5 分钟，至少 200 个完整上传答题人次
uv run --project src/load-test python src/load-test/run.py file --reset --run-id file-baseline-01

# Python：默认 3 道题、12 个任务，运行/提交各 50%
uv run --project src/load-test python src/load-test/run.py algorithm --reset --run-id algorithm-baseline-01
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

- 8 个测试考生，考生与 VU 一对一，不共享登录账号。
- 1 KiB 固定 ZIP fixture。
- 3 道具有独立 READY 判题配置的 Python 算法题。
- 算法 12 个任务，运行/提交 50:50，每 VU 同时最多一个非终态任务。
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
- k6：业务闭环成功/失败、延迟、吞吐量及各算法题/操作类型标签。

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

对照时在 Grafana 选择与实验报告一致的 UTC 时间范围。应用 HTTP 请求计数不等于 k6 的有效业务完成数；未启动外部监控时，本目录现有场景和报告仍可独立运行。

## 停止与回滚

停止当前 k6 进程即可中止负载；编排器仍会尝试保存部分结果。若需要恢复业务服务，只需去掉 `--reset` 并不再使用 `setup/docker-compose.loadtest.yml` 的压测 override。API Actuator metrics 暴露只由该 override 开启，不新增公网端口。

本工具不会自动修改 JVM 参数。任何调优建议都应引用实验 runId，并作为独立配置变更评审。
