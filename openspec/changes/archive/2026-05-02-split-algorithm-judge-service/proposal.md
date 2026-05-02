## Why

现有算法判题能力仍与 BlueNet 主后端耦合，判题执行、测试数据、沙箱和资源限制需要拆出为更专业、可独立部署的服务。现在需要在保留主应用业务入口、权限和轮询体验的前提下，将判题执行、安全沙箱、测试数据生成和多语言资源限制独立出来。

## What Changes

- 新增 `src/judge-service` 独立 Spring Boot JAR，负责消费判题任务、运行沙箱、生成测试数据、执行标准解 benchmark，并直接写入判题结果表。
- 主应用 `src/backend` 继续作为唯一 Flyway migration 执行方，负责业务、权限、题目、答案、测试数据配置、OSS 管理和前端 API。
- 用户提交代码继续存入数据库，正式判题任务保存 `source_code` 快照，避免用户后续修改答案影响已入队任务。
- 判题相关文件复用主应用同一个 OSS 服务，使用独立 `bluenet-judge` bucket 存储 generator、标准解、系统生成的 manifest、生成后的 `.in/.out` 测试用例和可选日志。
- 正式测试数据不通过逐条手填或上传数据包维护，而是由管理员上传 generator 和各语言标准解，在后台配置测试用例参数，再由 Judge Service 在沙箱中生成输入、标准输出和语言资源限制。
- manifest 由 Backend 根据后台配置生成并保存到 OSS，作为当前测试数据生成快照，不要求管理员手写上传。
- 每题每语言使用独立 `timeLimit`/`memoryLimit`，通过多次运行对应语言标准解推导建议值，由管理员确认后用于正式判题。
- 前端仍然只访问 Backend，通过现有轮询路径查看判题任务和评判结果。
- **BREAKING**: 旧的内置 Judge Worker 责任将迁移到独立 Judge Service；主后端不再承担候选人代码、generator 或标准解的沙箱执行。

## Capabilities

### New Capabilities

- `judge-test-data-generation`: 管理算法题 generator、标准解、测试用例配置、manifest 生成、OSS 存储、沙箱生成 `.in/.out` 和标准解 benchmark。

### Modified Capabilities

- `algorithm-online-judge`: 将算法判题从主后端内置 worker 调整为独立 Judge Service，并改为使用 OSS 测试用例、每语言资源限制和沙箱执行。
- `assessment-judgement`: 明确独立 Judge Service 完成正式判题后直接创建或更新自动评判记录，Backend 继续提供轮询和展示接口。

## Impact

- Affected backend areas: assessment answer submission, algorithm judge job creation, RabbitMQ publishing, admin algorithm question/test-data management, Flyway migrations, OSS configuration, judgement result query APIs.
- New service: `src/judge-service` with its own application configuration, RabbitMQ consumers, DB writers, OSS client, sandbox runner, generator runner, benchmark runner, and result finalizer.
- Infrastructure: Linux Judge Service runtime with `isolate` or `nsjail`, access to PostgreSQL, RabbitMQ, and the shared OSS service's `bluenet-judge` bucket.
- Data model: add current test data configuration, standard solution metadata, test case index, and per-language resource limit tables through Backend migrations only.
- Security: generator, standard solutions, and candidate submissions are all untrusted code and must run in the sandbox with network, CPU, wall-time, memory, process, output, and filesystem limits.
