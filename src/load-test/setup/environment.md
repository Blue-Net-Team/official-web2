# 本地压测环境记录

## 已确认工具与资源

- k6：`v2.3.0`，Windows 可执行文件 `C:\Program Files\k6\k6.exe`。
- Docker Server：`29.5.3`，Linux/x86_64，overlayfs。
- Docker Compose：`v5.1.4`。
- 压测机 Docker 资源：8 CPU，8,328,015,872 bytes 内存（约 7.76 GiB）。

## 当前授权环境

用户已明确授权清理并使用仓库现有的本地 Compose 环境：

| 项目 | 值 |
|---|---|
| Compose 项目 | `bluenet` |
| Compose `ENV` | `dev`，对应本地 `bluenet-*-dev-data` 卷 |
| Compose 配置 | `docker/docker-compose.yml` + `src/load-test/setup/docker-compose.loadtest.yml` |
| 环境变量 | 现有 `docker/.env`，脚本不输出其中的密钥 |
| API 地址 | `http://localhost:8080` |
| Judge 地址 | `http://localhost:8090` |
| 业务/判题 bucket | 沿用本地 Compose 的 `STORAGE_BUCKET` / `JUDGE_STORAGE_BUCKET` |
| MinIO/RustFS 公网地址（供 k6 直传） | `http://host.docker.internal:7000` |
| 系统账号来源 | `SYSTEM_USER_STUDENT_ID` / `SYSTEM_USER_PASSWORD`，不写死在脚本中 |
| k6 结果 | `src/load-test/results/<runId>/`，不入库 |

重置会删除该本地环境的数据库、Redis、RabbitMQ、MinIO/RustFS 数据卷，然后重建服务。它不得用于生产、测试服务器或远程 Docker。

## 安全边界

- 重置脚本只接受显式列出的本地项目/ENV 组合、匹配的项目确认和 `LOADTEST_RESET_CONFIRM=DELETE`。
- 未提供完整确认时必须在任何 Docker 命令之前失败。
- 不使用远程 Docker、生产或共享服务器地址。
- 外部邮件、GitHub、AI 等副作用配置在压测环境中保持关闭；本变更不伪造或输出第三方密钥。
