# BlueNet Official Website

蓝网官方网站 - 高校科技创新团队综合管理平台。

## 简介

BlueNet 是蓝网实验室的官方门户系统，用于展示实验室信息、管理招新报名、进行考核评估、维护成员资料等。项目采用 Monorepo 结构，包含后端（Spring Boot + Java 21）和前端（Next.js + React 19）。

## Monorepo 结构

```
bluenet_web2.2/
├── src/
│   ├── backend/           # Spring Boot 3.5.10 + Java 21
│   └── frontend/          # Next.js 15 + React 19
├── docker/                # Docker 配置
├── docs/                  # 项目文档
├── openspec/              # OpenSpec 变更管理
├── .github/               # GitHub 配置
├── CLAUDE.md              # AI 助手指南
└── README.md              # 本文件
```

## 快速开始

15 分钟运行项目：见 [docs/01-快速开始](docs/01-快速开始.md)。

## 文档导航

完整文档索引见 [docs/00-文档导航](docs/00-文档导航.md)。

主要文档：

- [01-快速开始](docs/01-快速开始.md) — 15 分钟运行项目
- [02-01-项目简介](docs/02-项目概述/02-01-项目简介.md) — 项目背景与技术栈
- [03-02-后端开发规范](docs/03-开发指南/03-02-后端开发规范.md) — DDD 分层、命名、转换链
- [03-03-前端开发规范](docs/03-开发指南/03-03-前端开发规范.md) — 页面、ISR、枚举、上传
- [04-01-环境配置](docs/04-运维部署/04-01-环境配置.md) — 依赖安装与变量说明
- [04-03-CI-CD自动部署](docs/04-运维部署/04-03-CI-CD自动部署.md) — GitHub Actions 与部署
- [05-01-数据库设计](docs/05-参考手册/05-01-数据库设计.md) — 表结构与字段说明

## AI 助手

AI 助手/智能体开发指南见 [CLAUDE.md](CLAUDE.md)。

## 贡献

欢迎贡献！请先阅读 [06-02-贡献指南](docs/06-项目管理/06-02-贡献指南.md)。

## 安全

发现安全漏洞请按 [SECURITY.md](SECURITY.md) 中的流程私下报告。

## License

[GPL-3.0](LICENSE)
