## Why

当前网站缺少用户反馈渠道，团队成员和访客在遇到页面问题时无法便捷地报告 Bug。增加一个全局悬浮按钮，可以让任何用户随时提交问题反馈（含截图、环境信息），帮助开发团队快速定位并修复线上问题，提升产品质量和用户体验。

## What Changes

- 前端全局增加 Ant Design `FloatButton` 悬浮按钮，点击弹出 Bug 报告 Modal 表单
- Bug 报告表单支持：问题描述、截图上传（最多 3 张）、自动捕获当前页面 URL 与浏览器环境信息
- 后端新增 `tb_bug_report` 主表和 `tb_bug_report_image` 关联表，支持多图片存储
- 新增后端 API：公开提交 Bug 报告、管理员分页查询列表、查看详情、更新处理状态
- Admin 后台新增「Bug 报告」管理页面，ROLE_MEMBER 及以上角色可查看和处理报告
- 新增 `BugReportStatus` 枚举：未解决(PENDING)、处理中(IN_PROGRESS)、已解决(RESOLVED)

## Capabilities

### New Capabilities

- `bug-report`: 全局 Bug 报告功能，包含前端 FloatButton 表单、文件上传集成、后端 CRUD 与状态管理
- `admin-bug-report-management`: Admin 后台 Bug 报告列表查看、详情、状态更新

### Modified Capabilities

- （无）

## Impact

- 前端：`src/frontend/src/app/layout.tsx` 引入全局 BugReportFloatButton 组件
- 前端：新增 `src/frontend/src/components/BugReport/` 目录及相关组件
- 前端：Admin 侧边栏新增「Bug 报告」菜单项
- 后端：新增 `tb_bug_report`、`tb_bug_report_image` 表（Flyway 迁移脚本）
- 后端：新增 Domain / Application / API 层代码（Entity、Repository、Service、Controller）
- 后端：新增权限标识 `bug-report:list`、`bug-report:detail`、`bug-report:update`
- 文件存储：复用现有 MinIO 上传机制，图片类型复用或新增 `FileType`
