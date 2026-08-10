# CLAUDE.md

This file guides AI assistants / agents when working in this repository.

## 项目速览

蓝网官方网站（BlueNet）是高校科技创新团队的综合管理平台，Monorepo 结构。后端 Spring Boot 3.5.10 + Java 21 + DDD，前端 Next.js 15 + React 19 + TypeScript + Ant Design 6。

详细背景见 [docs/02-项目概述/02-01-项目简介.md](./docs/02-项目概述/02-01-项目简介.md)。

## AI 开发工作流

所有功能开发必须按以下顺序执行：

```
1. Explore    → /opsx:explore  分析需求边界、风险、环境变量
2. Propose    → /opsx:propose  编写变更草案，等待用户确认
3. Implement  → TDD 开发：单元测试 → 领域层/应用层/基础设施 → 集成测试 → Controller
4. Frontend   → 如需前端对接，修改前端文件
5. Package    → 编译打包后端产物（见下方运行配置）
6. Docker     → 构建镜像、运行容器、启动 compose 基础设施
7. E2E Test   → Playwright 浏览器端到端验证
8. Archive    → /opsx:archive  用户确认后归档
```

**TDD 实施顺序**：先写测试确认功能边界 → 完成领域层和应用层代码（含基础设施实现）→ 继续完成集成测试代码 → 最后提供 Controller 接口完成集成测试。

**第三方服务配置**：涉及 GitHub OAuth、阿里云 OSS 等配置时，需主动指引用户检查并填写对应环境变量，不可自行编造密钥。

## 端到端验证指南

### 1. 编译打包后端

使用 IDEA 运行配置或等效命令：

```bash
cd src/backend
./mvnw clean compile package
# 或 Windows: mvnw.cmd clean compile package
```

### 2. 构建并运行后端 Docker 镜像

镜像标签：`bluenet-api-service:latest`，Dockerfile：`docker/api-service.Dockerfile`。

```bash
# 构建镜像
docker build -t bluenet-api-service:latest -f docker/api-service.Dockerfile .

# 运行容器（需先确保 compose 基础设施已启动）
docker run -d \
  --name backend-api-dev \
  -p 8080:8080 \
  --env-file docker/.env \
  --network bluenet_network \
  bluenet-api-service:latest
```

### 3. 启动基础设施

```bash
cd docker
docker compose -p bluenet --profile infra up -d
```

包含：PostgreSQL（15432）、Redis（6739）、RabbitMQ（5672/15672）、MinIO（7000/7001）。

### 4. 前端服务检查

**必须先检查 3000 端口是否被占用：**

```bash
# Windows
netstat -ano | findstr :3000

# Linux/macOS
lsof -i :3000
```

- **已占用** → 用户可能已启动前端开发服务，**禁止使用 `pnpm build` 和 `pnpm dev`**，直接使用现有服务验证
- **未占用** → 可自行运行 `pnpm dev`

### 5. Playwright 端到端验证

使用 Playwright MCP 或 CLI 打开浏览器，验证功能完整链路。后端修复需重新编译打包并重建镜像；前端修复需等待浏览器端渲染完成后再次验证。

## 关键约束速查

### 权限控制

- 所有 REST 接口必须使用 `@RequiresPermission` 注解，指定 `value`（全局唯一权限标识）、`name`（显示名称）、`access`（访问级别）
- `value` 在整个系统必须全局唯一，`PermissionScanner` 启动时扫描并校验，重复将导致启动失败。**不允许修改 `PermissionScanner` 逻辑**，开发者自行确保唯一性
- `AccessLevel`：`PUBLIC`（公开）、`AUTHENTICATED`（需登录）、`PROTECTED`（需特定权限，默认）

### 前端 ISR

- 所有从后端获取数据的 Server Component 页面必须设置 `export const revalidate = <秒数>`
- **必须是静态数字字面量**，不能是变量引用或表达式，否则 Next.js 编译报错

### 密码加密

- **前端 SHA256 + 后端 BCrypt**，后端不接触密码明文
- 后端涉及密码的逻辑（`User.resetPassword()`、`createUser()` 等）**不得**对密码再做额外哈希，直接 `passwordEncoder.encode(前端传来的哈希值)` 即可

### 层间返回类型约定

| 层级 | 返回类型 | 示例 |
|------|----------|------|
| Mapper | DO/PO（nullable） | `UserDO selectById(Long id)` |
| Repository 接口 | `Optional<Aggregate>` / `Page<Aggregate>` | `Optional<User> findById(Long id)` |
| RepositoryImpl | 由 DO 转换后返回 Optional/聚合 | — |
| 领域服务 | 领域实体/聚合 或 领域 VO | `Optional<User> getUser(Long id)` |
| Application | DTO / `PageDTO<DTO>` | — |
| Controller | `ResponseMessage<DTO>` | — |

例外：为性能须在 SQL 层做投影时，Mapper 可返回 VO/DTO，但必须在方法注释中说明。详见 [docs/03-开发指南/03-05-层间约定.md](./docs/03-开发指南/03-05-层间约定.md)。

### 分页接口

**必须返回 `PageDTO<T>`**，禁止返回 Spring Data 的 `Page<T>`（序列化字段与前端不兼容）。Service 层使用 `PageDTO.from(dtoPage)` 转换。

### 响应与异常

- 所有 REST 接口返回值必须使用 `ResponseMessage<T>` 包裹
- 接口层捕获预期异常，AOP 异常转交 `GlobalExceptionHandler` 处理

### 数据库

- 所有表以 `tb_` 开头，不使用物理外键，在应用层维护关系
- 不使用软删除字段，数据直接物理删除

### 文件权限（动态判断）

| 文件类型 | 权限判断逻辑 |
|----------|-------------|
| `work` | `currentUser.id == answer.user_id` OR `currentUser.role >= ROLE_MEMBER` |
| `assessment_attachment` | `currentUser.direction == question.direction` |
| `avatar` | `currentUser.role >= ROLE_MEMBER` 或 `currentUser.avatarId == fileId` |
| `normal_img` / `qrcode` | 公开可见 |

### 代码提交规范

```
type: description
```

支持类型：`feat`、`fix`、`docs`、`style`、`refactor`、`test`、`chore`、`perf`、`ci`、`build`、`revert`。

规则：冒号后必须有空格；描述至少 5 字符；不建议以句号结尾。

**Issue 引用规则**：

- 引用 issue 时默认使用 `ref #<issue号>`，例如：`fix: 容器设置 TZ=Asia/Shanghai 修复评论时间戳慢8小时问题\n\nref #22`
- **禁止**在提交消息中使用 `fixes #<issue号>`、`fix #<issue号>`、`close #<issue号>`、`closes #<issue号>`，因为这些关键字会自动关闭 issue
- 只有在你**明确确认**该 issue 已完全解决、且**经过用户同意**后，才允许使用 `fixes` 或 `close` 关键字

**PR 合并规则**：

- **禁止**使用 `gh` 工具直接合并 PR（如 `gh pr merge`），该操作必须由用户手动执行
- 你只能创建 PR（如 `gh pr create`），合并权限完全交由用户掌控

## 常见问题与排障

### PermissionScanner 启动失败

**现象**：`Duplicate permission value: xxx`

**原因**：`@RequiresPermission` 的 `value` 出现重复。

**解决**：全局搜索该权限标识，修改重复项，确保全局唯一。禁止修改 `PermissionScanner`。

### ISR 编译报错

**现象**：`Unsupported node type "MemberExpression"`

**原因**：`export const revalidate` 使用了变量引用或表达式。

**解决**：改为静态数字字面量，如 `export const revalidate = 3600`。

### 分页接口前端报错

**现象**：前端无法解析分页数据结构。

**原因**：Controller 返回了 Spring Data `Page<T>` 而非 `PageDTO<T>`。

**解决**：Service 层使用 `PageDTO.from(dtoPage)` 转换，Controller 返回 `ResponseMessage<PageDTO<XxxDTO>>`。

### 3000 端口冲突

**现象**：前端构建或 dev 服务启动失败。

**解决**：检查端口占用。若用户已启动前端服务，直接使用现有服务，禁止重复启动。

### 预签名 URL 上传失败（SignatureDoesNotMatch）

**原因**：`MINIO_PUBLIC_URL` 配置不正确，或请求 Host 与签名时不一致。

**解决**：确保 `MINIO_PUBLIC_URL` 是浏览器实际访问 MinIO 的地址（Docker 环境填宿主机可访问的外部地址）。不要设置 `MINIO_SERVER_URL` 环境变量给 MinIO 容器。

### 密码验证失败

**原因**：后端对密码做了额外哈希（如再次 SHA256 或多次 BCrypt）。

**解决**：后端密码逻辑直接 `passwordEncoder.encode(前端传来的SHA256哈希值)`，不得再做额外处理。

## 参考文档索引

| 文档 | 内容 |
|------|------|
| [docs/00-文档导航.md](./docs/00-文档导航.md) | 全文档索引与阅读路径 |
| [docs/02-项目概述/02-01-项目简介.md](./docs/02-项目概述/02-01-项目简介.md) | 项目背景、技术架构、核心功能 |
| [docs/03-开发指南/03-02-后端开发规范.md](./docs/03-开发指南/03-02-后端开发规范.md) | DDD 分层详解、命名规范、类型转换链、Entity 行为化 |
| [docs/03-开发指南/03-03-前端开发规范.md](./docs/03-开发指南/03-03-前端开发规范.md) | 页面清单、枚举规范、ISR 规范、文件上传对接 |
| [docs/03-开发指南/03-08-测试规范手册.md](./docs/03-开发指南/03-08-测试规范手册.md) | 各层测试策略、Mock 边界、ArchUnit |
| [docs/04-运维部署/04-01-环境配置.md](./docs/04-运维部署/04-01-环境配置.md) | 完整环境搭建、环境变量详解、常见问题 |
| [docs/04-运维部署/04-02-Docker部署.md](./docs/04-运维部署/04-02-Docker部署.md) | Docker Compose 部署 |
| [docs/05-参考手册/05-01-数据库设计.md](./docs/05-参考手册/05-01-数据库设计.md) | 表结构、字段定义、关系说明 |
| [docs/03-开发指南/03-05-层间约定.md](./docs/03-开发指南/03-05-层间约定.md) | 层间返回类型约定详细版 |
