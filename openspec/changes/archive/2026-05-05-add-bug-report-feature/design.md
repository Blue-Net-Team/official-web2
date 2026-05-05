## Context

蓝网官网目前无任何用户反馈渠道。当页面出现 Bug 时，用户只能通过外部社交工具联系团队，信息传递低效且容易遗漏。本项目需要在所有页面引入一个轻量级的悬浮反馈入口，允许用户一键提交问题描述、上传截图，并自动附加当前页面 URL 和浏览器环境信息，帮助开发团队快速复现和修复问题。

## Goals / Non-Goals

**Goals:**
- 全局可访问的 Bug 报告入口（FloatButton），交互轻量化
- 报告内容包含：用户描述、多张截图、当前页面 URL、浏览器环境（UA、分辨率、视口大小）
- 后端完整支持 Bug 报告的提交、查询、状态流转
- Admin 后台提供 Bug 报告列表页和详情页，支持按状态筛选
- ROLE_MEMBER 及以上角色可查看和处理报告

**Non-Goals:**
- 不实现实时通知（邮件/站内信推送）
- 不实现 Bug 报告的评论/回复功能
- 不实现用户侧的历史报告查询
- 不集成外部 Bug 追踪系统（如 GitHub Issues、Jira）

## Decisions

**1. 图片存储复用 MinIO + tb_file 机制**
- 截图先通过 `POST /api/v1/file/upload` 上传至 MinIO，获取 fileId
- 提交 Bug 报告时携带 fileId 列表，后端通过 `tb_bug_report_image` 关联表建立多对多关系
- 理由：复用现有基础设施，保证文件权限和存储一致性；避免 Base64 传输导致请求体过大

**2. 环境信息在前端组装为 JSON 字符串存入后端**
- 前端提取 `window.location.href`、`navigator.userAgent`、`window.screen` 等信息，序列化为 JSON
- 后端以 `TEXT` 类型整存整取，不单独拆分为多个字段
- 理由：环境字段可能随需求变化，JSON 存储灵活且无需修改表结构；当前无需按环境字段筛选查询

**3. 状态枚举设计为 PENDING / IN_PROGRESS / RESOLVED**
- 不引入 CLOSED/IGNORED 状态，简化状态机；已解决即为终态
- 理由：满足最小可用需求，避免过度设计；如需扩展后续可新增状态

**4. 公开提交接口不设用户关联**
- 提交 Bug 报告时记录 `reporter_email`（选填）作为联系途径，不强制要求登录
- 理由：未登录访客也可能发现问题，降低反馈门槛；邮箱选填保护用户隐私

**5. Admin 管理页复用现有 Admin 布局与权限体系**
- 新增菜单项挂载在 Admin 侧边栏，复用 `AdminSideBar` 组件
- 权限控制使用 `@RequiresPermission` 注解，配合 `ROLE_MEMBER` 的权限分配
- 理由：与现有后台管理风格一致，减少 UI 开发成本

## Risks / Trade-offs

- **[Risk]** 公开提交接口可能被滥用（垃圾报告/恶意上传）
  → **Mitigation**：未做频率限制（当前版本），后续可通过 IP 限流或增加验证码防范；图片文件大小和类型通过 MinIO 上传接口已有校验
- **[Risk]** 大量截图占用 MinIO 存储空间
  → **Mitigation**：当前版本不实现自动清理，依赖运维定期清理；后续可增加定时任务清理已解决报告的关联图片
- **[Trade-off]** 环境信息 JSON 存储导致无法按浏览器/分辨率等维度统计
  → **Accept**：当前需求不需要此类统计，若后续需要可改为单独字段或解析 JSON 建立物化视图
