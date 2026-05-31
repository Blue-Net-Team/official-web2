---
name: local-dev-infra
description: 本地 Docker 基础设施的搭建与管理。使用 Docker Compose（项目名 bluenet）启动、停止、检查基础设施服务（PostgreSQL、Redis、RabbitMQ、MinIO），提供健康检查、日志查看、常见问题排查。不涉及前后端开发调试流程。
metadata:
  author: IVEN
  version: "2.0"
---

当用户提到「启动环境」「本地调试」「基础设施」「docker」「compose」「数据库」「启动服务」「infra」「MinIO」「PostgreSQL」「Redis」「RabbitMQ」「停止服务」「容器日志」「端口冲突」等关键词时，自动激活此 skill。

本 skill 的职责是：**仅管理 Docker Compose 基础设施层的生命周期**。前后端应用的编译、运行、调试由其他 skill 负责。

职责边界：
- 本 skill：Docker compose 的启停、健康检查、日志、端口、配置、问题排查

---

## 启动前自检

每次激活时，按以下顺序检查环境状态：

### 1. 检查 docker/.env 是否存在

```bash
cd docker && ls -la .env
```

- **不存在** → 引导用户从 .env.example 复制并配置
- **存在** → 继续下一步

### 2. 检查关键环境变量是否已配置

读取 `docker/.env`，检查以下变量是否非空：

| 变量 | 用途 | 是否必须 |
|------|------|----------|
| `DB_PASSWORD` | PostgreSQL 密码 | ✅ |
| `JWT_SECRET` | JWT 签名密钥 | ✅ |
| `MINIO_PUBLIC_URL` | MinIO 浏览器访问地址 | ✅（minio 场景） |
| `STORAGE_PROVIDER` | 存储提供商 | ✅ |

如果 `STORAGE_PROVIDER=minio`，必须检查 `MINIO_PUBLIC_URL` 是否配置为宿主机可访问的地址（如 `http://192.168.x.x:7000`），不能是容器内部地址。

### 3. 检查端口占用

基础设施使用的端口映射：

| 端口 | 服务 | 检查命令 |
|------|------|----------|
| 15432 | PostgreSQL | `netstat -ano \| findstr :15432` (Win) / `lsof -i :15432` (Mac/Linux) |
| 6739 | Redis | `netstat -ano \| findstr :6739` / `lsof -i :6739` |
| 5672/15672 | RabbitMQ | `netstat -ano \| findstr :5672` / `lsof -i :5672` |
| 7000/7001 | MinIO | `netstat -ano \| findstr :7000` / `lsof -i :7000` |

**若端口被占用**：
- 如果占用者是本项目容器（`bluenet-*`），说明服务已在运行
- 如果占用者是其他进程，需引导用户关闭占用进程或修改 `docker-compose.yml` 中的端口映射

---

## 启动基础设施

基础设施包含：PostgreSQL、Redis、RabbitMQ、MinIO。它们使用 `infra` 或 `infra-local` profile。

### 方式一：使用外部存储（不含 MinIO）

当 `STORAGE_PROVIDER=aliyun-oss` 或已有外部 MinIO 时：

```bash
cd docker
docker compose -p bluenet --profile infra up -d
```

启动的服务：PostgreSQL + Redis + RabbitMQ

### 方式二：含本地 MinIO

当 `STORAGE_PROVIDER=minio` 且需要本地 MinIO 时：

```bash
cd docker
docker compose -p bluenet --profile infra-local up -d
```

启动的服务：PostgreSQL + Redis + RabbitMQ + MinIO

### 方式三：启动全部服务（含后端/前端/AI/判题）

```bash
cd docker
docker compose -p bluenet --profile full up -d
```

---

## 健康检查

### 一键查看所有容器状态

```bash
cd docker
docker compose -p bluenet ps
```

### 各服务独立健康检查

```bash
# PostgreSQL
docker exec bluenet-pgsql pg_isready -U postgres -d db_blue_net

# Redis
docker exec bluenet-redis redis-cli ping

# RabbitMQ
docker exec bluenet-rabbitmq rabbitmq-diagnostics -q ping

# MinIO
curl -f http://localhost:7000/minio/health/live
```

---

## 查看日志

### 实时跟踪所有基础设施日志

```bash
cd docker
docker compose -p bluenet logs -f pgsql redis rabbitmq oss
```

### 查看指定服务最近 100 行日志

```bash
cd docker
docker compose -p bluenet logs --tail=100 pgsql
docker compose -p bluenet logs --tail=100 redis
docker compose -p bluenet logs --tail=100 rabbitmq
docker compose -p bluenet logs --tail=100 oss
```

---

## 停止与清理

### 停止基础设施（保留数据卷）

```bash
cd docker
docker compose -p bluenet --profile infra down
```

### 停止全部服务（保留数据卷）

```bash
cd docker
docker compose -p bluenet down
```

### 停止并删除所有数据卷（⚠️ 清空数据库、Redis、MinIO 数据）

```bash
cd docker
docker compose -p bluenet down -v
```

### 重启单个服务

```bash
cd docker
docker compose -p bluenet restart pgsql
docker compose -p bluenet restart redis
docker compose -p bluenet restart rabbitmq
docker compose -p bluenet restart oss
```

---

## 常见问题排查

### 问题 1：容器启动失败 / 端口冲突

**排查步骤**：
1. 查看容器日志：`docker logs bluenet-pgsql`
2. 检查端口占用：`netstat -ano | findstr :<端口>`
3. 若端口被其他进程占用，停止该进程或修改 `docker-compose.yml` 中的端口映射
4. 查看容器状态：`docker compose -p bluenet ps`

### 问题 2：PostgreSQL 连接失败

**症状**：`Connection refused` 或 `database "db_blue_net" does not exist`

**排查**：
1. 检查 `docker/.env` 中的 `DB_PASSWORD` 是否与 `docker-compose.yml` 中 `POSTGRES_PASSWORD` 一致
2. 检查 PostgreSQL 容器是否健康：`docker exec bluenet-pgsql pg_isready -U postgres`
3. 如果数据库不存在，后端 Flyway 会自动创建，首次启动需等待后端启动
4. 查看 PostgreSQL 日志：`docker logs bluenet-pgsql --tail=50`

### 问题 3：MinIO 无法访问

**排查**：
1. 检查 MinIO 容器是否运行：`docker compose -p bluenet ps`
2. 检查宿主机 7000/7001 端口是否被占用
3. 检查防火墙是否放行 7000/7001 端口
4. `MINIO_PUBLIC_URL` 需配置为宿主机可访问的地址（如 `http://192.168.x.x:7000`）
5. 浏览器访问 `http://localhost:7001` 验证 MinIO Console

### 问题 4：RabbitMQ 连接失败

**排查**：
1. 检查 RabbitMQ 容器是否健康：`docker exec bluenet-rabbitmq rabbitmq-diagnostics -q ping`
2. 检查 `docker/.env` 中的 `RABBITMQ_USERNAME`/`RABBITMQ_PASSWORD` 是否与容器内配置一致
3. 浏览器访问 `http://localhost:15672` 验证管理界面（默认 guest/guest 或 admin/admin）

### 问题 5：Redis 连接失败

**排查**：
1. 检查 Redis 容器是否运行：`docker exec bluenet-redis redis-cli ping`
2. 检查 `docker/.env` 中的 `REDIS_PASSWORD` 是否与容器内配置一致
3. 如果设置了密码，连接时需要使用 `-a` 参数

### 问题 6：Flyway 迁移失败（数据库相关）

**排查**：
1. 查看后端启动日志中的 Flyway 错误信息
2. 检查 `src/backend/src/main/resources/db/migration/` 中脚本是否有语法错误
3. 如需重置数据库：`docker compose -p bluenet down -v` 后重新启动（会清空所有数据）

### 问题 7：Docker 卷数据混乱

**排查**：
1. 检查当前 ENV 环境变量：`cat docker/.env | grep ENV`
2. 查看现有卷：`docker volume ls | grep bluenet`
3. 不同 ENV 值的卷是相互隔离的
4. 如需清理旧卷：`docker volume rm bluenet-database-dev-data` 等

---

## 常用命令速查

| 操作 | 命令 |
|------|------|
| 启动基础设施 | `cd docker && docker compose -p bluenet --profile infra up -d` |
| 启动含 MinIO | `cd docker && docker compose -p bluenet --profile infra-local up -d` |
| 启动全部服务 | `cd docker && docker compose -p bluenet --profile full up -d` |
| 查看状态 | `cd docker && docker compose -p bluenet ps` |
| 停止基础设施 | `cd docker && docker compose -p bluenet --profile infra down` |
| 停止全部 | `cd docker && docker compose -p bluenet down` |
| 清空数据 | `cd docker && docker compose -p bluenet down -v` |
| 查看日志 | `cd docker && docker compose -p bluenet logs -f <服务名>` |
| 重启服务 | `cd docker && docker compose -p bluenet restart <服务名>` |
| 进入容器 | `docker exec -it bluenet-pgsql psql -U postgres -d db_blue_net` |
| 进入 Redis | `docker exec -it bluenet-redis redis-cli` |
