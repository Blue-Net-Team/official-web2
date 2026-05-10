# BlueNet Official Website

蓝网官方网站 - 高校科技创新团队综合管理平台

## 项目结构（Monorepo）

```
bluenet_web2.2/
├── src/
│   ├── backend/           # Spring Boot 3.5.10 + Java 21
│   │   ├── main/          # 源代码
│   │   ├── test/          # 测试代码
│   │   └── pom.xml        # Maven 配置
│   └── frontend/          # Next.js 15 + React 19
│       ├── src/           # 源代码
│       ├── public/        # 静态资源
│       └── package.json   # npm 配置
├── docker/                # Docker 配置
│   ├── api-service.Dockerfile  # 后端镜像构建
│   ├── frontend.Dockerfile     # 前端镜像构建
│   ├── judge-service.Dockerfile# 评测服务镜像构建
│   └── docker-compose.yml      # 容器编排
├── openspec/              # OpenSpec 变更管理
│   ├── changes/           # 变更记录
│   └── specs/             # 规范文件
├── docs/                  # 项目文档
├── .claude/               # Claude Code 配置
├── .cursor/               # Cursor IDE 配置
├── .github/               # GitHub 配置
├── CLAUDE.md              # AI 开发指南
└── README.md              # 项目说明（本文件）
```

## 快速开始

### 前置要求

- Java 21+
- Node.js 18+
- pnpm 9+
- PostgreSQL 15+
- Redis 7+
- MinIO

### 后端启动

```bash
cd src/backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 前端启动

```bash
cd src/frontend
pnpm install
pnpm dev
```

### Docker 部署

```bash
# 开发环境（仅启动依赖服务：数据库、Redis、RabbitMQ、MinIO）
cd docker
docker compose --profile infra up -d

# 完整部署（包含前后端）
cd docker
docker compose --profile full up -d --build
```

## 技术栈

### 后端
- Spring Boot 3.5.10
- Spring Security + JWT
- MyBatis-Plus
- PostgreSQL
- Redis
- RabbitMQ
- MinIO / 阿里云 OSS

### 前端
- Next.js 15 (App Router)
- React 19
- TypeScript
- Ant Design 6
- Zustand

## 文档

- [CLAUDE.md](CLAUDE.md) - AI 开发指南（端到端验证与常见问题）
- [项目概述](docs/项目概述.md) - 项目背景与架构
- [后端开发手册](docs/后端开发手册.md) - DDD 分层、命名规范、测试规范
- [前端开发手册](docs/前端开发手册.md) - 页面清单、ISR 规范、文件上传对接
- [环境配置指南](docs/环境配置指南.md) - 环境搭建、Docker 部署、变量说明
- [数据库设计](docs/数据库设计.md) - 表结构与关系说明

## 迁移说明

本项目从 `bluenet_web2.1` 迁移而来，主要变更：

1. **目录结构**：从分离的前后端目录变为统一的 monorepo 结构
2. **OpenSpec**：3个独立的 OpenSpec 目录合并为1个
3. **Docker**：Docker 配置统一放在 `docker/` 目录

## License

[GPL-3.0](LICENSE)
