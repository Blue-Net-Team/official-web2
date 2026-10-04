# Proposal: admin-mobile-card-adaptation

## Why

管理平台 8 个后台页面在手机视口下体验差：表格超出视口、固定操作列遮挡内容、日期换行不美观、双栏布局挤压导致"每行只有一个字"。Issue 要求这些页面在移动端改为卡片式展示。现有代码已有 `Grid.useBreakpoint()` + `isMobile = !screens.md` 的适配模式，但全部是"删列/横向滚动"策略，尚无"表格→卡片"先例，需要确立统一的移动端卡片规范。

## What Changes

- 确立统一的移动端卡片列表规范（新增能力 `admin-mobile-card-list`）：
  - 断点约定 `isMobile = !screens.md`（与现有页面一致）
  - 移动端 `isMobile ? <卡片列表> : <Table>` 双渲染，桌面端视觉零变化
  - 卡片视觉沿用烟色玻璃体系（`rgba(255,255,255,.045)` 底、`white/8%` 描边、12px 圆角、`#fa8c16` 强调色）
  - 所有图标使用 `@ant-design/icons`，禁止 emoji
- 以下页面移动端改为卡片展示（详见各 spec delta）：
  1. 用户管理 `admin/users`：操作列固定导致遮挡 → 用户卡片（姓名+角色 / 学号·方向·学院·年级 / 底部操作条）
  2. GitHub 组织邀请 `admin/github-invitations`：表格超视口 → 邀请卡片，保留 checkbox 批量邀请
  3. 考核时间管理 `admin/assessment/time`：日期换行 → 卡片内时间范围单行展示
  4. 题目评分 `admin/assessment/judge/score`：学号列挤压 → 提交列表卡片化（队伍头行 + 成员卡片），范围仅提交列表
  5. Bug 报告 `admin/bug-report`：表格超视口 → 报告卡片，详情 Drawer 宽度自适应
  6. AI 对话详情 `admin/ai-traces/conversations/[id]`：双栏挤压 → 提问导航改横向滚动 chips + 详情全宽
  7. AI 对话分析 `admin/ai-traces/statistics`：固定宽度分栏溢出 → 指标卡 2×2、图表纵向堆叠全宽
  8. 资源库管理 `admin/resources`：表格超视口 → 资源卡片，移动端拖拽排序禁用、改为 ↑↓ 调序按钮
- 交互决策（已与用户确认）：
  - GitHub 邀请移动端保留 checkbox 多选以支持批量邀请
  - 资源库移动端禁用 dnd-kit 拖拽，提供上移/下移按钮调序

## Capabilities

### New Capabilities

- `admin-mobile-card-list`: 管理平台移动端卡片列表的统一规范——断点判定、双渲染策略、卡片视觉约定、图标约束（antd 图标、禁 emoji）
- `admin-user-management`: 用户管理页移动端卡片展示（信息层级、操作条、与现有 6 个 Modal/批量操作兼容）
- `admin-software-resource-management`: 资源库管理页移动端卡片展示（启用开关、调序按钮替代拖拽）

### Modified Capabilities

- `github-org-invitation`: 邀请页移动端由表格改为卡片，保留单条邀请与 checkbox 批量邀请
- `assessment-time-admin-ui`: 考核时间页移动端由"删列后的表格"改为卡片，时间范围单行展示
- `assessment-judgement`: 题目评分页提交列表（含队伍头行与成员行）移动端卡片化
- `admin-bug-report-management`: Bug 报告页移动端由表格改为卡片，详情 Drawer 移动端全宽
- `ai-trace-admin`: AI 对话详情页移动端改上下布局（横向滚动提问导航 + 全宽详情）；分析面板移动端纵向堆叠、卡片全宽

## Impact

- **代码**：纯前端展示层，涉及
  - `src/frontend/src/app/admin/users/page.tsx`
  - `src/frontend/src/app/admin/github-invitations/page.tsx`
  - `src/frontend/src/app/admin/assessment/time/page.tsx`
  - `src/frontend/src/app/admin/assessment/judge/score/page.tsx`
  - `src/frontend/src/app/admin/bug-report/page.tsx`
  - `src/frontend/src/app/admin/ai-traces/conversations/[id]/page.tsx`
  - `src/frontend/src/app/admin/ai-traces/statistics/page.tsx`
  - `src/frontend/src/app/admin/resources/page.tsx`
  - 可能新增共享组件 `src/frontend/src/components/Admin/MobileCardList/`（视实现权衡，见 design.md）
- **设计稿**：`docs/UI/mobile-mockups.html` + `docs/UI/imgs/mobile-mockups-full.png`（已产出，作为视觉基准）
- **API / 数据流 / 权限**：无改动
- **依赖**：无新增依赖（antd v6 / @ant-design/icons 已有）
- **风险**：低-中。纯视觉层改动，桌面端零变化；最大复杂点在 judge/score（1650 行，队伍头行 + 内嵌表格）与 resources（dnd-kit 双渲染分支）
- **验证**：Playwright 移动视口（375px）E2E 走查 8 个页面 + 桌面视口回归
