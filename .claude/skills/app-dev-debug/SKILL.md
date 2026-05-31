---
name: app-dev-debug
description: 前后端应用服务的开发调试引导。后端 API 服务、判题服务、AI 服务均通过 Maven/uv 编译打包后在 Docker 中运行；前端支持 pnpm dev 本地开发或 Docker 构建运行。提供编译、构建、运行、联调、排障全流程命令。
metadata:
  author: IVEN
  version: "2.0"
---

当用户提到「启动后端」「启动前端」「pnpm dev」「maven 编译」「打包」「构建镜像」「运行服务」「调试后端」「调试前端」「前后端联调」「api-service」「judge-service」「ai-service」「3000 端口」「8080 端口」等关键词时，自动激活此 skill。

本 skill 的职责是：**管理应用服务（API、判题、AI、前端）的编译、构建、运行和调试**。所有操作均通过命令行/Docker 完成，适配 agent coding 工具。

职责边界：
- ✅ 本 skill：后端编译打包、Docker 镜像构建、应用服务启停、前端 pnpm dev / Docker 运行、联调配置
- ❌ 其他 skill `local-dev-infra`：PostgreSQL、Redis、RabbitMQ、MinIO 等基础设施的启停

---

## 启动前自检

### 1. 检查基础设施是否已运行

应用服务依赖基础设施，先确认基础设施已启动：

```bash
cd docker && docker compose -p bluenet ps
```

如果基础设施（pgsql、redis、rabbitmq）未运行，先启动：

```bash
cd docker && docker compose -p bluenet --profile infra up -d
```

### 2. 检查端口占用

| 端口 | 服务 | 检查命令 |
|------|------|----------|
| 8080 | 后端 API | `netstat -ano \| findstr :8080` (Win) / `lsof -i :8080` (Mac/Linux) |
| 8090 | 判题服务 | `netstat -ano \| findstr :8090` / `lsof -i :8090` |
| 8000 | AI 服务 | `netstat -ano \| findstr :8000` / `lsof -i :8000` |
| 3000 | 前端 | `netstat -ano \| findstr :3000` / `lsof -i :3000` |

**若端口被占用**：
- 如果占用者是本项目 Docker 容器，说明服务已在运行
- 如果占用者是其他进程，需关闭或调整

### 3. 检查 docker/.env 关键配置

```bash
cat docker/.env | grep -E "^STORAGE_PROVIDER=|^MINIO_PUBLIC_URL=|^JWT_SECRET="
```

- `STORAGE_PROVIDER=minio` 时，`MINIO_PUBLIC_URL` 必须配置为宿主机可访问的地址
- `JWT_SECRET` 不能为空

---

## 后端开发调试

### API 服务（api-service）

#### 编译打包

```bash
cd src/backend
./mvnw clean compile package
# Windows: mvnw.cmd clean compile package
```

#### 构建 Docker 镜像

```bash
cd docker
docker build -t bluenet-api-service:latest -f api-service.Dockerfile ..
```

#### 运行容器

```bash
cd docker
docker compose -p bluenet --profile app up -d api-service
```

#### 查看日志

```bash
cd docker
docker compose -p bluenet logs -f api-service
```

> 每次修改后端代码后，需重新执行编译打包 → 构建镜像 → 重启容器。

#### 重启（代码修改后）

```bash
cd src/backend && ./mvnw clean compile package
cd docker && docker build -t bluenet-api-service:latest -f api-service.Dockerfile ..
cd docker && docker compose -p bluenet restart api-service
```

---

### 判题服务（judge-service）

#### 编译打包

```bash
cd src/judge-service
./mvnw clean compile package
# Windows: mvnw.cmd clean compile package
```

#### 构建 Docker 镜像

```bash
cd docker
docker build -t bluenet-judge-service:latest -f judge-service.Dockerfile ..
```

#### 运行容器

```bash
cd docker
docker compose -p bluenet --profile judge up -d judge-service
```

#### 查看日志

```bash
cd docker
docker compose -p bluenet logs -f judge-service
```

#### 重启（代码修改后）

```bash
cd src/judge-service && ./mvnw clean compile package
cd docker && docker build -t bluenet-judge-service:latest -f judge-service.Dockerfile ..
cd docker && docker compose -p bluenet restart judge-service
```

---

### AI 服务（ai-service）

#### 构建 Docker 镜像

```bash
cd docker
docker build -t bluenet-ai-service:latest -f ai-service.Dockerfile ..
```

#### 运行容器

```bash
cd docker
docker compose -p bluenet --profile app up -d ai-service
```

#### 查看日志

```bash
cd docker
docker compose -p bluenet logs -f ai-service
```

#### 重启（代码修改后）

```bash
cd docker && docker build -t bluenet-ai-service:latest -f ai-service.Dockerfile ..
cd docker && docker compose -p bluenet restart ai-service
```

---

### 一键启动全部后端服务

```bash
# 1. 编译 API 服务
cd src/backend && ./mvnw clean compile package

# 2. 编译判题服务
cd ../judge-service && ./mvnw clean compile package

# 3. 构建全部镜像并启动
cd ../../docker
docker build -t bluenet-api-service:latest -f api-service.Dockerfile ..
docker build -t bluenet-judge-service:latest -f judge-service.Dockerfile ..
docker build -t bluenet-ai-service:latest -f ai-service.Dockerfile ..
docker compose -p bluenet --profile full up -d
```

---

## 前端开发调试

### 方式一：pnpm dev（推荐前端日常开发）

优点：热重载最快、HMR 支持完整。

1. 确保后端 API 已运行（Docker 中）
2. 检查 3000 端口是否被占用：
   ```bash
   # Windows
   netstat -ano | findstr :3000
   # Linux/macOS
   lsof -i :3000
   ```
3. 如果被占用（可能是之前启动的前端），直接使用现有服务
4. 如果未占用，启动前端：
   ```bash
   cd src/frontend
   pnpm install
   pnpm dev
   ```

> **端口占用规则**：如果 3000 已被占用，说明前端开发服务可能已启动，**禁止重复启动**，直接使用现有服务。

#### 前端环境变量配置

开发时前端需要知道后端地址，在 `src/frontend/.env.local` 中配置：

```bash
NEXT_PUBLIC_BACKEND_HOST=localhost
NEXT_PUBLIC_BACKEND_PORT=8080
NEXT_PUBLIC_SSL_ENABLED=false
NEXT_PUBLIC_API_PREFIX=/api/v1
```

> `NEXT_PUBLIC_*` 变量在构建时注入，运行 `pnpm dev` 时也会读取。修改后需重启 dev server。

---

### 方式二：Docker 中运行（与 CICD 一致）

用于验证生产构建或测试 Docker 镜像。

1. 构建镜像：
   ```bash
   cd docker
   docker build \
     --build-arg NEXT_PUBLIC_BACKEND_HOST=localhost \
     --build-arg NEXT_PUBLIC_BACKEND_PORT=8080 \
     -t bluenet-frontend:latest \
     -f frontend.Dockerfile ..
   ```

2. 运行容器：
   ```bash
   cd docker
   docker compose -p bluenet --profile app up -d frontend
   ```

3. 访问：`http://localhost:3000`

> Docker 中的前端是静态构建产物，**不支持热重载**，每次修改代码需重新构建镜像。

---

## 前后端联调

### 联调检查清单

| 检查项 | 配置 |
|--------|------|
| 后端地址 | `localhost:8080`（Docker 映射到宿主机） |
| 前端 `.env.local` | `NEXT_PUBLIC_BACKEND_HOST=localhost` |
| CORS | `CORS_ALLOWED_ORIGINS=*`（开发环境） |
| Cookie Domain | `COOKIE_DOMAIN=`（空） |
| Cookie Secure | `COOKIE_SECURE=false` |
| Cookie SameSite | `COOKIE_SAME_SITE=Lax` |
| 文件上传 | `MINIO_PUBLIC_URL` 为浏览器可访问地址 |

### 常见问题：前端请求 403 或 CORS 错误

1. 检查后端 `docker/.env` 中 `CORS_ALLOWED_ORIGINS` 是否包含前端地址
2. 开发环境设为 `*` 最宽松
3. 检查 Cookie 的 Domain/Secure/SameSite 设置

### 常见问题：前端文件上传失败

1. 检查 `MINIO_PUBLIC_URL` 是否为浏览器可访问的地址
2. Docker 环境下填写宿主机局域网 IP + 7000 端口
3. 不要配置 `MINIO_SERVER_URL` 环境变量给 MinIO 容器

---

## 健康检查

### 后端服务健康检查

```bash
# API 服务
curl -f http://localhost:8080/api/v1/health

# 判题服务
curl -f http://localhost:8090/api/v1/judge/health

# AI 服务
curl -f http://localhost:8000/ai/v1/health
```

### 查看所有应用服务状态

```bash
cd docker && docker compose -p bluenet ps
```

---

## 停止与重启

### 停止单个应用服务

```bash
cd docker
docker compose -p bluenet stop api-service
docker compose -p bluenet stop judge-service
docker compose -p bluenet stop ai-service
docker compose -p bluenet stop frontend
```

### 重启单个服务（修改代码后）

```bash
# API 服务
cd src/backend && ./mvnw clean compile package
cd docker && docker build -t bluenet-api-service:latest -f api-service.Dockerfile ..
cd docker && docker compose -p bluenet restart api-service

# 判题服务
cd src/judge-service && ./mvnw clean compile package
cd docker && docker build -t bluenet-judge-service:latest -f judge-service.Dockerfile ..
cd docker && docker compose -p bluenet restart judge-service

# AI 服务
cd docker && docker build -t bluenet-ai-service:latest -f ai-service.Dockerfile ..
cd docker && docker compose -p bluenet restart ai-service

# 前端 Docker
cd docker && docker build -t bluenet-frontend:latest -f frontend.Dockerfile ..
cd docker && docker compose -p bluenet restart frontend
```

### 停止全部应用服务（保留基础设施）

```bash
cd docker
docker compose -p bluenet --profile app down
docker compose -p bluenet --profile judge down
```

---

## 常见问题排查

### 问题 1：Maven 编译失败

**排查**：
1. 检查 JDK 版本：`java -version` 应为 21
2. 检查 Maven 版本：`./mvnw -v`
3. 清理缓存后重试：`./mvnw clean compile package -U`
4. 检查网络：依赖下载失败时检查 Maven 镜像配置

### 问题 2：Docker 构建失败（No such file: target/*.jar）

**排查**：
1. 先执行 Maven 编译打包：`./mvnw clean compile package`
2. 确认 `target/` 目录下已生成 jar 文件
3. Dockerfile 构建上下文应为项目根目录：`docker build -f docker/api-service.Dockerfile ..`

### 问题 3：后端启动失败（数据库连接失败）

**排查**：
1. 检查基础设施是否已启动：`docker compose -p bluenet ps`
2. 检查 `docker/.env` 中的数据库配置
3. Docker 运行时使用容器主机名：`DATABASE_HOST=database`
4. 确认 PostgreSQL 容器已运行且健康

### 问题 4：前端 pnpm dev 启动失败

**排查**：
1. 检查 3000 端口是否被占用
2. 检查 Node.js 版本：应为 20+
3. 删除 `node_modules` 后重新安装：`rm -rf node_modules && pnpm install`
4. 检查 `.env.local` 配置是否正确

### 问题 5：前端请求后端返回 401/403

**排查**：
1. 检查是否已登录（JWT Token 是否过期）
2. 检查后端 CORS 配置
3. 检查 Cookie 的 Domain/Secure/SameSite 设置
4. 检查用户权限（Role + Permission）

### 问题 6：判题服务无法消费任务

**排查**：
1. 检查 RabbitMQ 是否运行：`docker exec bluenet-rabbitmq rabbitmq-diagnostics -q ping`
2. 检查判题服务日志：`docker logs bluenet-judge-service`
3. 检查 `docker/.env` 中 RabbitMQ 配置是否与容器内一致

### 问题 7：AI 服务启动失败

**排查**：
1. 检查 AI 服务日志：`docker logs bluenet-ai-service`
2. 检查 API 密钥是否配置：`TBD_RAG_SILICONFLOW_API_KEY` 或 `TBD_RAG_DEEPSEEK_API_KEY`
3. 检查向量数据库连接：Milvus 或 PgVector 是否可访问

---

## 常用命令速查

| 操作 | 命令 |
|------|------|
| 编译 API 服务 | `cd src/backend && ./mvnw clean compile package` |
| 编译判题服务 | `cd src/judge-service && ./mvnw clean compile package` |
| 构建 API 镜像 | `cd docker && docker build -t bluenet-api-service:latest -f api-service.Dockerfile ..` |
| 构建前端镜像 | `cd docker && docker build -t bluenet-frontend:latest -f frontend.Dockerfile ..` |
| 构建 AI 镜像 | `cd docker && docker build -t bluenet-ai-service:latest -f ai-service.Dockerfile ..` |
| 启动后端 | `cd docker && docker compose -p bluenet --profile app up -d` |
| 启动判题 | `cd docker && docker compose -p bluenet --profile judge up -d` |
| 启动前端 dev | `cd src/frontend && pnpm dev` |
| 停止应用服务 | `cd docker && docker compose -p bluenet --profile app down` |
| 查看 API 日志 | `cd docker && docker compose -p bluenet logs -f api-service` |
| 查看判题日志 | `cd docker && docker compose -p bluenet logs -f judge-service` |
| 查看 AI 日志 | `cd docker && docker compose -p bluenet logs -f ai-service` |
| API 健康检查 | `curl -f http://localhost:8080/api/v1/health` |
| 判题健康检查 | `curl -f http://localhost:8090/api/v1/judge/health` |
| AI 健康检查 | `curl -f http://localhost:8000/ai/v1/health` |
