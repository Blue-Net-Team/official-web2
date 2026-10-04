# Tasks: admin-mobile-card-adaptation

## 1. 准备

- [x] 1.1 确认 3000 端口占用情况；未占用则启动前端 dev 服务用于验证
- [x] 1.2 对照设计稿 `docs/UI/mobile-mockups.html` 核对 8 页卡片结构与文案层级

## 2. 用户管理（users/page.tsx）

- [x] 2.1 引入 `Grid.useBreakpoint()` 判定 `isMobile`
- [x] 2.2 实现移动端用户卡片列表（主行：姓名+角色+状态；详情：学号/方向/学院/年级双列；操作区：查看/编辑/重置密码/禁用），复用现有 Modal/Drawer handler
- [x] 2.3 卡片主行加入多选 checkbox，与 `selectedRowKeys` 共用，保留批量角色变更入口与新建按钮；移动端补充角色/方向/学院筛选 Select
- [x] 2.4 ~~Playwright 375px 视口验证~~（用户决定跳过浏览器验证；以 tsc + lint 代替）

## 3. GitHub 组织邀请（github-invitations/page.tsx）

- [x] 3.1 实现移动端邀请卡片（姓名+角色、方向/邮箱、GitHub 绑定标签、右侧邀请按钮），复用 handleInvite
- [x] 3.2 卡片加 checkbox 多选，保留批量邀请按钮与结果弹窗
- [x] 3.3 ~~Playwright 双视口验证~~（用户决定跳过）

## 4. 考核时间管理（assessment/time/page.tsx）

- [x] 4.1 将移动端"删列后表格"替换为卡片列表：方向/轮次/年级主行、时间范围单行（ClockCircleOutlined + `YYYY-MM-DD HH:mm ~ HH:mm`）、限时/组队说明、状态标签、编辑/删除
- [x] 4.2 卡片点击/操作复用现有 Drawer 与删除确认，权限控制不变
- [x] 4.3 ~~Playwright 双视口验证~~（用户决定跳过）

## 5. 题目评分（assessment/judge/score/page.tsx）

- [x] 5.1 移动端提交列表卡片化：队伍头行通栏卡片（TeamOutlined）、成员缩进卡片（姓名/队长 CrownOutlined/学号独立行/得分+状态/评分按钮），替换 submissionColumns 渲染处
- [x] 5.2 评分/改分按钮复用现有评分 Modal（宽度已有 `screens.md` 自适应）
- [x] 5.3 ~~验证排行榜移动端不溢出与桌面回归~~（用户决定跳过）；桌面端列内 👑 emoji 已替换为 CrownOutlined

## 6. Bug 报告（bug-report/page.tsx）

- [x] 6.1 实现移动端报告卡片（状态+Issue 链接/标题、描述两行截断/底行 ID+页面+查看详情），图标用 GithubOutlined/EyeOutlined
- [x] 6.2 详情 Drawer 宽度改为 `screens.md ? 560 : '100%'`
- [x] 6.3 ~~Playwright 双视口验证~~（用户决定跳过）

## 7. AI 对话详情（ai-traces/conversations/[id]/page.tsx）

- [x] 7.1 移动端改为上下布局：提问导航渲染为横向滚动胶囊条（seq+时间+激活高亮），摘要行（意图/动作/耗时）置于胶囊条下
- [x] 7.2 事件流详情卡片移动端全宽
- [x] 7.3 ~~Playwright 双视口验证~~（用户决定跳过）

## 8. AI 对话分析（ai-traces/statistics/page.tsx）

- [x] 8.1 移动端统计卡 2×2 栅格、趋势图与工具使用卡全宽纵向堆叠；意图/工具条形图由固定像素宽度（480/280）改为百分比自适应
- [x] 8.2 ~~Playwright 双视口验证~~（用户决定跳过）

## 9. 资源库管理（resources/page.tsx）

- [x] 9.1 实现移动端资源卡片（名称+启用开关/状态文字、方向/分类标签、描述截断、链接+编辑/删除），复用现有 handler
- [x] 9.2 移动端禁用 dnd 渲染，提供上移/下移按钮（边界禁用）调用同一排序 API，失败回滚
- [x] 9.3 ~~Playwright 双视口验证~~（用户决定跳过）

## 10. 收尾

- [x] 10.1 全局检查 8 页无 emoji 图标，统一 `@ant-design/icons`（含桌面端遗留 👑 替换）
- [x] 10.2 `tsc --noEmit` 通过、`next lint` 无新增告警（dev 服务运行中未跑 `pnpm build`，tsc 覆盖类型编译检查）
- [x] 10.3 设计稿 `docs/UI/mobile-mockups.html` 与实现一致，无需更新
- [x] 10.4 按页面分组提交 commit
