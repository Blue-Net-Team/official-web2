# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

蓝网官方网站（BlueNet Official Website）是一个面向高校科技创新团队的综合管理平台，采用 **Monorepo** 结构。

### 技术栈

**后端**：Spring Boot 3.5.10 + Java 21 + DDD 架构
**前端**：Next.js 15 + React 19 + TypeScript + Ant Design 6 + Zustand

### 核心功能

- 成员招募与报名系统
- 多轮考核与评分系统
- 团队知识库问答
- 文件存储与权限控制
- 用户认证与授权

### 项目结构

```
bluenet_web2.2/
├── src/
│   ├── backend/           # 后端代码（Spring Boot）
│   │   ├── main/java/     # 源代码
│   │   ├── main/resources/# 配置文件、数据库迁移
│   │   └── test/java/     # 测试代码
│   └── frontend/          # 前端代码（Next.js）
│       ├── src/           # 源代码
│       └── public/        # 静态资源
├── scripts/               # 脚本文件
│   └── hooks/             # Git Hooks（可推送到远端）
├── docker/                # Docker 配置
├── openspec/              # OpenSpec 变更管理
│   ├── changes/archive/   # 已归档变更
│   └── specs/             # 规范文件
└── docs/                  # 项目文档
```

## 开发命令

### 后端命令

```bash
# 进入后端目录
cd src/backend

# 开发环境运行（默认）
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 生产环境运行
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# 构建项目
./mvnw clean package

# 代码格式化（使用 Eclipse 格式化配置）
./mvnw spotless:apply

# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest=EnrollServiceImplTest

# 运行集成测试
./mvnw test -Dtest=*IntegrationTest

# 跳过测试构建
./mvnw clean package -DskipTests

# 检查 Flyway 迁移状态
./mvnw flyway:info
```

### 前端命令

```bash
# 进入前端目录
cd src/frontend

# 安装依赖
pnpm install

# 开发环境运行
pnpm dev

# 构建生产版本
pnpm build

# 运行生产版本
pnpm start

# 代码检查
pnpm lint
```

## 架构与开发规范

### DDD 分层架构

项目严格遵循领域驱动设计四层架构：

1. **接口层 (api/)** - REST API 控制器、DTO、装配器
   - 路径：`src/backend/main/java/com/bluenet/web/api/`
   - 职责：HTTP 请求处理、参数校验、响应返回
   - 必须使用 `@RequiresPermission` 注解指定权限和访问级别
2. **应用层 (application/)** - 用例编排、应用服务
   - 路径：`src/backend/main/java/com/bluenet/web/application/`
   - 职责：协调多个领域服务完成业务用例、事务控制
   - 负责 VO 与 DTO 转换
3. **领域层 (domain/)** - 核心业务逻辑
   - 路径：`src/backend/main/java/com/bluenet/web/domain/`
   - 包含：实体 (entity/)、值对象 (vo/)、枚举 (enumerate/)、领域服务 (service/)
   - 不应感知 Entity 和 DTO，仅操作 VO
4. **基础设施层 (infrastructure/)** - 技术实现
   - 路径：`src/backend/main/java/com/bluenet/web/infrastructure/`
   - 包含：仓储实现 (repository/)、配置 (config/)、安全 (security/)、外部服务集成

### 关键约束

- **权限控制**：所有接口必须使用 `@RequiresPermission` 注解，指定 `value`（权限标识）、`name`（显示名称）、`access`（访问级别）
- **访问级别**：使用 `AccessLevel` 枚举指定接口访问级别
  - `PUBLIC` - 公开访问，无需认证
  - `AUTHENTICATED` - 需要登录，无需特定权限
  - `PROTECTED` - 需要特定权限（默认）
- **响应包装**：所有 REST 接口返回值必须使用 `ResponseMessage<T>` 类型包裹
- **异常处理**：接口层捕获预期异常，AOP 异常转交 `GlobalExceptionHandler` 处理
- **数据库**：不使用物理外键，在应用层维护关系
- **文件权限**：File 表仅存储元信息，权限通过关联业务表动态控制
- **API 文档**：使用 SpringDoc OpenAPI 生成文档，所有对外暴露的 DTO、接口必须添加 `@Schema`、`@Operation` 等注解描述
- **前端 ISR**：所有从后端获取数据的 Server Component 页面必须设置 `export const revalidate = <秒数>`（静态数字字面量，不能是变量引用），默认 3600 秒

### 文件权限设计

文件访问权限根据文件类型动态判断：

- `work`（考生答案）：`currentUser.id == answer.user_id` OR `currentUser.role >= ROLE_MEMBER`
- `assessment_attachment`（考题附件）：`currentUser.direction == question.direction`
- `avatar`（头像）：`currentUser.role >= ROLE_MEMBER` 或 `currentUser.avatarId == fileId`
- `normal_img`/`qrcode`：公开可见

## OpenSpec 工作流

项目使用 OpenSpec 进行变更管理，所有功能开发通过 `openspec/` 目录进行：

### 变更目录结构

```
openspec/
├── changes/
│   └── archive/           # 已归档变更（按日期命名）
└── specs/                 # 规范文件（功能模块定义）
```

### 常用命令

```bash
# 查看变更状态
/opsx:status <change-name>

# 应用变更（实现任务）
/opsx:apply <change-name>

# 继续变更（创建下一个工件）
/opsx:continue <change-name>

# 验证变更实现
/opsx:verify <change-name>

# 归档已完成变更
/opsx:archive <change-name>
```

### 当前状态

所有变更已完成并归档到 `openspec/changes/archive/` 目录。新功能开发应使用 `/opsx:propose` 创建新的变更提案。

## 技术栈详情

### 核心依赖

- **Spring Boot 3.5.10** + **Java 21**
- **Spring Security 6.x** + **JWT** 认证
- **MyBatis-Plus 3.5.7** ORM
- **PostgreSQL 15+** 数据库
- **Flyway 10.x** 数据库迁移
- **Redis 7.x** 缓存
- **RabbitMQ 3.x** 消息队列
- **MinIO 8.5.4** 对象存储
- **SpringDoc OpenAPI 2.7.0** API 文档

### 测试框架

- **Testcontainers** 用于集成测试（PostgreSQL、MinIO）
- **Spring Security Test** 安全测试
- **MyBatis-Plus Test** 数据访问测试

## 数据库设计

### 核心表前缀

所有表以 `tb_` 开头，不使用软删除字段，数据直接物理删除。

### 关键实体关系

- `tb_user` - 用户表（学号唯一）
- `tb_enroll` - 报名表（关联头像、学院）
- `tb_assessment_*` - 考核相关表（时间、题目、答案、评论）
- `tb_file` - 文件表（动态权限控制）
- `tb_audit` - 审计日志表（记录 IP、User-Agent）
- `tb_college` - 学院表
- `tb_competition` - 竞赛表
- `tb_direction_learning_step` - 方向学习路径表

## 开发注意事项

### 报名流程特殊处理

1. **学号唯一性**：报名时检测学号是否已存在，支持 `forceUpdate` 参数更新
2. **头像关联**：报名时上传的头像在账号发放时自动应用到用户账号
3. **内推码**：团队成员拥有唯一 8 位内推码，报名时填写用于追踪
4. **审核流程**：待审核 → 已通过（创建用户）/已拒绝

### 考核系统特性

- 各方向考核时间独立配置
- 支持多种题型：单选、多选、文件上传、算法题
- 权限分级：考生只可见对应方向和年级的考核
- 限时控制：每轮考核可独立设置是否限时

### 文件存储结构

系统使用 MinIO 对象存储，按文件类型划分存储桶，支持动态权限控制：

#### 文件类型（FileType 枚举）

| 枚举值                     | 存储值                     | 描述         | 对应 MinIO 存储桶            |
| ----------------------- | ----------------------- | ---------- | ----------------------- |
| `AVATAR`                | `avatar`                | 用户头像       | `avatar`                |
| `NORMAL_IMG`            | `normal-img`            | 普通图片（介绍图片） | `normal-img`            |
| `ASSESSMENT_ATTACHMENT` | `assessment-attachment` | 考题附件       | `assessment-attachment` |
| `WORK`                  | `work`                  | 考生作品文件     | `work`                  |
| `QRCODE`                | `qrcode`                | 二维码图片      | `qrcode`                |

**注意**：MinIO 存储桶名称与 FileType 枚举的存储值（value）完全一致。`MinioConfig` 在应用启动时自动创建这些存储桶。`application.yml` 中的 `bucket-names` 配置项仅供参考，实际代码中使用 `FileType.getValue()` 作为存储桶名称。

#### 介绍图片

介绍图片（`FileType.NORMAL_IMG`）通过 `tb_introduce_image` 表管理，包含 `file_id` 和 `description` 字段。竞赛封面通过 `tb_competition.cover_file_id` 直接关联。

#### 文件命名规则

1. **自动生成**：`{fileType}-{uuid}.{ext}`（如 `avatar-123e4567-e89b-12d3-a456-426614174000.jpg`）
   - `fileType` 使用枚举名称的小写形式（如 `avatar`、`normal_img`、`assessment_attachment`）
   - 注意：文件类型在文件名中使用下划线（`normal_img`），而存储桶名称使用连字符（`normal-img`）
2. **MinIO 存储路径**：`{bucket}/{filename}`
   - `bucket` = `FileType.getValue()`（如 `avatar`、`normal-img`、`work`）
   - `filename` = 完整文件名（如 `avatar-xxx.jpg`）
3. **元数据存储**：`tb_file` 表存储文件 ID、文件名、类型，不存储实际文件内容
   - `url` 字段已废弃，文件下载通过 `GET /api/v1/file/download/{fileId}` 接口实现
   - 文件类型字段存储的是 `FileType` 枚举值（如 `NORMAL_IMG`），而非存储值

#### 文件权限控制（动态判断）

文件访问权限根据关联的业务实体动态控制：

| 文件类型                          | 权限控制表                    | 判断逻辑                                                                    |
| ----------------------------- | ------------------------ | ----------------------------------------------------------------------- |
| `work`（考生答案）                  | `tb_assessment_answer`   | `currentUser.id == answer.user_id` OR `currentUser.role >= ROLE_MEMBER` |
| `assessment_attachment`（考题附件） | `tb_assessment_question` | `currentUser.direction == question.direction`                           |
| `avatar`（头像）                  | `tb_user` / `tb_enroll`  | `currentUser.role >= ROLE_MEMBER` 或 `currentUser.avatarId == fileId`    |
| `normal_img`（普通图片）            | `tb_introduce_image`     | 公开可见                                                                    |
| `qrcode`（二维码）                 | -                        | 公开可见                                                                    |

#### 文件上传接口

- `POST /api/v1/file/upload` - 统一文件上传，接受 `file` + `type`（FileType 枚举），纯文件存储，不涉及业务逻辑
- 业务关联通过独立接口完成：
  - `PUT /api/v1/user/avatar` - 更新用户头像（传 fileId）
  - `PUT /api/v1/admin/assessment-questions/{id}/attachment` - 更新考题附件（传 fileId）
  - `POST /api/v1/admin/qrcodes` - 创建二维码记录（传 fileId + type）
  - `POST /api/v1/admin/introduce-images` - 创建介绍图片（传 fileId + description）
  - `PUT /api/v1/admin/competitions/{id}/logo` - 更新竞赛 Logo（传 fileId）
  - `PUT /api/v1/admin/competitions/{id}/cover` - 更新竞赛封面（传 fileId）

#### 文件下载接口

- `GET /api/v1/file/download/{fileId}` - 统一下载接口，自动根据文件类型进行权限验证

### 环境配置

#### 配置文件

| 环境 | 配置文件 | 激活方式 |
|------|---------|---------|
| 开发环境 | `application-dev.yml` | 默认激活 |
| 生产环境 | `application-prod.yml` | `-Dspring-boot.run.profiles=prod` |
| 测试环境 | `application-test.yml` | 测试时自动激活 |

#### 环境变量

项目使用 `.env` 文件管理环境变量，从 `.env.example` 复制并填写：

```bash
cd src/backend
cp .env.example .env
```

**关键配置项：**

| 类别 | 变量 | 说明 |
|------|------|------|
| 数据库 | `DATABASE_*` | PostgreSQL 连接配置 |
| 对象存储 | `MINIO_*` | MinIO 连接配置 |
| 认证 | `JWT_SECRET` | JWT 签名密钥（至少 32 字符） |
| 邮件 | `MAIL_*` | SMTP 邮件服务配置 |
| Cookie | `COOKIE_*` | Cookie 安全配置 |
| CORS | `CORS_ALLOWED_ORIGINS` | 跨域配置 |

#### 安全要求

**开发环境：**
- `COOKIE_SECURE=false`（允许 HTTP）
- `CORS_ALLOWED_ORIGINS=*`（允许所有来源）

**生产环境：**
- `COOKIE_SECURE=true`（必须 HTTPS）
- `CORS_ALLOWED_ORIGINS` 必须配置具体域名
- `JWT_SECRET` 必须使用强密钥

> 📖 详细配置请参考 [环境配置指南](./docs/环境配置指南.md)

## 认证机制

### 概述

系统使用 **HttpOnly Cookie + CSRF Token** 认证方案，JWT Token 存储在 HttpOnly Cookie 中防止 XSS 攻击，CSRF Token 用于防止跨站请求伪造攻击。

### Cookie 配置

| Cookie 名称    | 属性                                 | 用途           |
| ------------ | ---------------------------------- | ------------ |
| `auth_token` | HttpOnly, Secure(生产), SameSite=Lax | JWT Token 存储 |
| `csrf_token` | Secure(生产), SameSite=Lax           | CSRF 防护      |

### CSRF 防护机制

- **验证范围**：POST/PUT/DELETE/PATCH 请求（已登录用户）
- **验证方式**：Double Submit Cookie（Cookie vs X-CSRF-Token Header）
- **白名单接口**：
  - `/api/v1/auth/login/**` - 登录接口
  - `/api/v1/auth/logout` - 登出接口
  - `/api/v1/enrollments` - 公开报名
  - `/api/v1/file/upload` - 统一文件上传（报名时使用）

### 认证流程

1. **登录**：`POST /api/v1/auth/login/student-id`
   - 验证学号和密码
   - 设置 `auth_token` Cookie（HttpOnly）
   - 设置 `csrf_token` Cookie
   - 响应体返回 `{ csrfToken, userInfo }`
2. **获取登录状态**：`GET /api/v1/auth/me`
   - 从 Cookie 读取 JWT Token 验证
   - 刷新 CSRF Token
   - 返回 `{ authenticated, userInfo, csrfToken }`
3. **登出**：`POST /api/v1/auth/logout`
   - 清除 Cookie（Max-Age=0）
   - 吊销 JWT Token

### 前端对接要求

1. **凭证携带**：所有请求需要 `withCredentials: true`（axios）或 `credentials: 'include'`（fetch）
2. **CSRF Token**：状态修改请求需在 Header 中携带 `X-CSRF-Token`
3. **CORS 配置**：后端 `cors.allowed-origins` 需配置前端域名

### 环境变量

```bash
# Cookie 配置
COOKIE_DOMAIN=.example.com    # 跨子域时设置，本地开发留空
COOKIE_SECURE=true            # 生产环境必须 true（HTTPS）
COOKIE_SAME_SITE=Lax          # 推荐 Lax

# CORS 配置
CORS_ALLOWED_ORIGINS=https://example.com,https://www.example.com
```

### 相关文件

```
infrastructure/
├── config/
│   ├── CookieProperties.java      # Cookie 配置属性
│   └── SecurityConfig.java        # 安全配置
├── security/
│   ├── cookie/
│   │   ├── CookieService.java     # Cookie 操作接口
│   │   └── CookieServiceImpl.java
│   ├── csrf/
│   │   ├── CsrfTokenService.java  # CSRF Token 服务接口
│   │   ├── CsrfTokenServiceImpl.java
│   │   └── CsrfTokenFilter.java   # CSRF 验证过滤器
│   └── jwt/
│       └── JwtAuthenticationFilter.java  # JWT 认证过滤器（Cookie 提取）
└── api/dto/auth/
    ├── UserAuthResponseDTO.java   # 登录响应（csrfToken + userInfo）
    └── AuthMeResponseDTO.java     # 登录状态响应
```

## 代码质量

### 提交规范

提交信息必须遵循以下格式：

```
type: description
```

或带作用域：

```
type(scope): description
```

**支持的类型：**

| 类型       | 说明                |
| ---------- | ------------------- |
| `feat`     | 新功能              |
| `fix`      | 修复 Bug            |
| `docs`     | 文档更新            |
| `style`    | 代码格式（不影响功能）|
| `refactor` | 重构                |
| `test`     | 测试相关            |
| `chore`    | 构建/工具变动       |
| `perf`     | 性能优化            |
| `ci`       | CI/CD 配置变动      |
| `build`    | 构建系统变动        |
| `revert`   | 回滚提交            |

**示例：**

```
feat: 添加用户登录功能
feat(UI): 添加登录表单组件
fix: 修复登录页面样式问题
fix(API): 修复用户认证异常
docs: 更新 README 文档
refactor: 重构文件上传逻辑
```

**多行提交格式：**

```
feat: 添加用户登录功能

- 实现登录表单组件
- 添加表单验证逻辑
- 集成 JWT 认证
```

**规则：**

1. 冒号后必须有一个空格
2. 描述至少 5 个字符
3. 描述不建议以句号结尾
4. 作用域可选，放在括号内

### Git Hooks 安装

项目使用 Git Hooks 进行提交信息校验。克隆仓库后需安装：

```bash
# 进入项目根目录
cd bluenet_web2.2

# 安装 Git Hooks
./scripts/hooks/install.sh
```

**注意**：`.git/hooks/` 目录不会被推送到远端，需要手动安装。

### 分支策略

- `main`: 生产分支
- `develop`: 开发分支
- `feature/*`: 功能分支
- `hotfix/*`: 紧急修复分支

### 代码检查

- Eclipse 代码格式化配置（`.eclipseformatter.xml`）
- Spotless Maven 插件自动格式化
- 提交前运行 `mvn spotless:apply` 确保格式一致

