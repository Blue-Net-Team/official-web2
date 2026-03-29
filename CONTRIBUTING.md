# 贡献指南

感谢您有兴趣为蓝网官方网站后端系统做出贡献！本文档将帮助您了解如何参与项目开发。

## 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
- [开发环境搭建](#开发环境搭建)
- [项目架构](#项目架构)
- [代码规范](#代码规范)
- [提交规范](#提交规范)
- [分支策略](#分支策略)
- [Pull Request 流程](#pull-request-流程)
- [问题报告](#问题报告)

## 行为准则

本项目采用 [行为准则](.github/CODE_OF_CONDUCT.md)，参与本项目的所有贡献者都应遵守。请阅读该文件了解详情。

## 如何贡献

### 贡献类型

我们欢迎以下类型的贡献：

- **Bug 修复** - 修复现有功能的问题
- **新功能** - 添加新功能或增强现有功能
- **文档改进** - 改进文档、注释或示例
- **代码重构** - 提高代码质量和可维护性
- **测试用例** - 增加测试覆盖率

### 贡献流程

1. **Fork 仓库** - 点击 GitHub 页面右上角的 Fork 按钮
2. **克隆仓库** - 将您 Fork 的仓库克隆到本地
3. **创建分支** - 为您的更改创建一个新分支
4. **进行更改** - 编写代码并确保通过测试
5. **提交更改** - 遵循提交规范提交您的更改
6. **推送分支** - 将分支推送到您的 Fork 仓库
7. **创建 Pull Request** - 向原仓库提交 Pull Request

## 开发环境搭建

### 环境要求

#### 后端环境

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 21+ | Java 开发环境 |
| Maven | 3.9+ | 项目构建工具 |
| PostgreSQL | 15+ / 17+ | 主数据库 |
| Redis | 7.x | 缓存服务（可选） |
| RabbitMQ | 3.x | 消息队列（可选） |
| MinIO | 最新版 | 对象存储 |

#### 前端环境

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| Node.js | 20+ | JavaScript 运行时 |
| pnpm | 9+ | 包管理器 |

### 克隆项目

```bash
git clone https://github.com/<your-username>/bluenet_web2.2.git
cd bluenet_web2.2
```

### 安装 Git Hooks

```bash
./scripts/hooks/install.sh
```

此脚本会安装提交信息校验的 Git Hooks。

### 配置上游仓库

```bash
git remote add upstream https://github.com/<original-owner>/bluenet_web2.2.git
```

### 同步上游更改

```bash
git fetch upstream
git checkout main
git merge upstream/main
```

### 环境变量配置

1. 复制环境变量模板：
   ```bash
   cd src/backend
   cp .env.example .env
   ```

2. 编辑 `.env` 文件，填写必要配置：
   ```bash
   # 数据库配置
   DATABASE_PASSWORD=your_database_password

   # MinIO 配置
   MINIO_SK=your_minio_secret_key

   # JWT 密钥（至少 32 字符）
   JWT_SECRET=your_jwt_secret_key_at_least_32_characters

   # 邮件服务（可选）
   MAIL_USERNAME=your_email@163.com
   MAIL_PASSWORD=your_smtp_password
   ```

### 初始化数据库

```bash
# 创建数据库
psql -U postgres
CREATE DATABASE db_blue_net;
\q
```

Flyway 会在应用启动时自动执行数据库迁移脚本。

### 构建项目

```bash
./mvnw clean install
```

### 运行项目

```bash
# 后端（开发环境）
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 前端（新终端）
cd src/frontend
pnpm install
pnpm dev
```

### 运行测试

```bash
./mvnw test
```

## 项目架构

本项目采用 **领域驱动设计（DDD）** 四层架构：

```
com.bluenet.web/
├── api/                    # 接口层 - REST API 控制器、DTO
├── application/            # 应用层 - 用例编排、事务控制
├── domain/                 # 领域层 - 核心业务逻辑、实体、值对象
└── infrastructure/         # 基础设施层 - 技术实现、数据库访问
```

### 架构规则

在贡献代码时，请遵循以下架构规则：

1. **接口层（API）**：对 DTO 操作，捕获预期异常
2. **应用层**：调用领域层获取 VO，负责 VO 与 DTO 的转换
3. **领域层**：仅对 VO 操作，不应感知 Entity 和 DTO
4. **仓库层**：调用 MyBatis-Plus 操作数据库，负责 VO 与 Entity 的转换

### 层级依赖规则

- 领域层服务不应相互调用
- 领域层服务只能通过仓库接口调用数据库
- 应用层服务可以调用多个领域服务，但不允许调用其他应用层服务
- 控制层可以根据参数动态调整调用的应用层服务

## 代码规范

### 代码格式化

项目使用 Eclipse 代码格式化配置（`.eclipseformatter.xml`），在提交前请运行：

```bash
./mvnw spotless:apply
```

### 编码规范

#### REST 接口规范

1. 所有 REST 接口必须返回 `ResponseMessage`
2. 所有 REST 接口必须在接口层捕获预期的异常
3. 所有接口必须使用 `@RequiredPermission` 注解指定权限

#### API 文档规范

项目使用 `springdoc-openapi-starter-webmvc-ui` 生成 API 文档，请确保：

- 所有对外暴露的 DTO 使用相关注解描述
- 接口参数、返回值、预期异常都有清晰的文档说明

#### 命名规范

- 类名：使用大驼峰命名法（PascalCase）
- 方法名：使用小驼峰命名法（camelCase）
- 常量：使用全大写下划线分隔（UPPER_SNAKE_CASE）
- 包名：使用全小写

### 数据库迁移

新增数据库变更时，请在 `src/main/resources/db/migration/` 目录下创建 Flyway 迁移脚本：

```
V{版本号}__{描述}.sql
```

例如：`V1.0.1__add_user_gender_field.sql`

## 提交规范

提交信息必须遵循以下格式：

```
<类型>: <简短描述>
```

或带作用域：

```
<类型>(<作用域>): <简短描述>
```

### 提交类型

| 类型       | 说明                         |
| ---------- | ---------------------------- |
| `feat`     | 新功能                       |
| `fix`      | Bug 修复                     |
| `docs`     | 文档更新                     |
| `style`    | 代码格式（不影响功能）       |
| `refactor` | 重构（不新增功能也不修复 Bug）|
| `test`     | 测试相关                     |
| `chore`    | 构建过程或辅助工具的变动     |
| `perf`     | 性能优化                     |
| `ci`       | CI/CD 配置变动               |
| `build`    | 构建系统变动                 |
| `revert`   | 回滚提交                     |

### 提交规则

1. 冒号后必须有一个空格
2. 描述至少 5 个字符
3. 描述不建议以句号结尾
4. 作用域可选，放在括号内

### 提交示例

**单行格式：**

```
feat: 添加用户头像上传功能
feat(UI): 添加登录表单组件
fix: 修复登录页面样式问题
fix(API): 修复用户认证异常
docs: 更新 README 文档
refactor: 重构文件上传逻辑
```

**多行格式：**

```
feat: 添加用户头像上传功能

- 新增 FileController 处理文件上传
- 实现头像文件存储到 MinIO
- 添加文件大小和类型校验

Closes #123
```

### Git Hooks 安装

项目使用 Git Hooks 进行提交信息校验。克隆仓库后请运行：

```bash
./scripts/hooks/install.sh
```

此脚本会将共享的 Git Hooks 复制到本地 `.git/hooks/` 目录。

## 分支策略

| 分支类型 | 说明 | 示例 |
|---------|------|------|
| `main` | 生产分支，稳定版本 | `main` |
| `develop` | 开发分支，集成最新功能 | `develop` |
| `feature/*` | 功能分支 | `feature/user-avatar` |
| `hotfix/*` | 紧急修复分支 | `hotfix/login-bug` |
| `bugfix/*` | Bug 修复分支 | `bugfix/validation-error` |

### 创建功能分支

```bash
git checkout -b feature/your-feature-name
```

### 保持分支更新

```bash
git fetch upstream
git rebase upstream/develop
```

## Pull Request 流程

### 提交前检查清单

- [ ] 代码已通过 `spotless:apply` 格式化
- [ ] 所有测试已通过
- [ ] 新功能已添加相应的测试用例
- [ ] 数据库变更已创建 Flyway 迁移脚本
- [ ] API 文档注解已添加
- [ ] 提交信息符合规范

### 创建 Pull Request

1. 确保您的分支已推送到 Fork 仓库
2. 在 GitHub 上创建 Pull Request
3. 填写 PR 模板中的所有必填项
4. 等待代码审查

### PR 标题格式

```
<类型>: <简短描述>
```

例如：`feat: 添加用户头像上传功能`

### 代码审查

- 维护者会审查您的代码
- 请及时响应审查意见并做出修改
- 审查通过后，维护者会合并您的 PR

## 问题报告

### 报告 Bug

如果您发现了 Bug，请使用 [Bug 报告模板](.github/ISSUE_TEMPLATE/bug_report.md) 创建 Issue，并包含以下信息：

- 环境信息（操作系统、JDK 版本等）
- 复现步骤
- 预期行为
- 实际行为
- 相关日志

### 功能请求

如果您有新功能建议，请使用 [功能请求模板](.github/ISSUE_TEMPLATE/feature_request.md) 创建 Issue。

### 问题讨论

在开始编写代码之前，建议先创建 Issue 讨论您的想法，确保与项目方向一致。

## 获取帮助

如果您有任何问题，可以：

- 创建 Issue 进行讨论
- 发送邮件至 gdou_bluenet@163.com

## 相关文档

- [环境配置指南](./docs/环境配置指南.md) - 详细的环境配置说明
- [自动部署配置指南](./docs/自动部署配置指南.md) - CI/CD 部署配置
- [数据库设计](./docs/数据库设计.md) - 数据库表结构说明
- [后端开发手册](./docs/后端开发手册.md) - 后端开发规范
- [前端开发手册](./docs/前端开发手册.md) - 前端开发规范

---

再次感谢您对蓝网项目的关注和贡献！
