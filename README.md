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
│   ├── Dockerfile.backend # 后端镜像构建
│   ├── Dockerfile.frontend# 前端镜像构建
│   └── docker-compose.yml # 容器编排
├── openspec/              # OpenSpec 变更管理
│   ├── changes/
│   │   ├── archive/       # 已归档变更
│   │   └── direction-detail-page/  # 活跃变更
│   └── specs/             # 规范文件
├── docs/                  # 项目文档
│   ├── backend/           # 后端文档
│   └── frontend/          # 前端文档
├── .claude/               # Claude Code 配置
├── .cursor/               # Cursor IDE 配置
├── .github/               # GitHub 配置
├── CLAUDE.md              # 开发指南
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
# 开发环境（仅启动依赖服务：数据库、MinIO、Kafka）
cd docker
docker-compose up -d database oss kafka

# 完整部署（包含前后端）
cd docker
docker-compose up -d --build
```

## 技术栈

### 后端
- Spring Boot 3.5.10
- Spring Security + JWT
- MyBatis-Plus
- PostgreSQL
- Redis
- MinIO

### 前端
- Next.js 15 (App Router)
- React 19
- TypeScript
- Ant Design 6
- Zustand

## 文档

- [CLAUDE.md](CLAUDE.md) - 完整开发指南
- [后端开发手册](docs/backend/) - 后端文档
- [前端开发手册](docs/frontend/) - 前端文档

## 迁移说明

本项目从 `bluenet_web2.1` 迁移而来，主要变更：

1. **目录结构**：从分离的前后端目录变为统一的 monorepo 结构
2. **OpenSpec**：3个独立的 OpenSpec 目录合并为1个
3. **Docker**：Docker 配置统一放在 `docker/` 目录

## License

MIT
