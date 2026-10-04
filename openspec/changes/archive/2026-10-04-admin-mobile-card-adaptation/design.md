# Design: admin-mobile-card-adaptation

## Context

管理平台 8 个页面均为 `'use client'` 组件 + antd Table + Tailwind 烟色玻璃主题。仓库已有移动端适配先例（`assessment/time`、`achievement`、`venue` 等），模式为 `const screens = Grid.useBreakpoint(); const isMobile = !screens.md`，但只做过"删列 / 横向滚动"，从未做过"表格→卡片"双渲染。设计基准稿：`docs/UI/mobile-mockups.html` + `docs/UI/imgs/mobile-mockups-full.png`（已获用户确认；图标一律 `@ant-design/icons`，禁 emoji）。

## Goals / Non-Goals

**Goals:**
- 8 个页面在 `<768px` 视口下以卡片/纵向堆叠呈现，无横向滚动、无内容挤压
- 桌面端（≥768px）视觉与行为零变化
- 确立可复用的移动端卡片规范，后续页面照此执行

**Non-Goals:**
- 不改任何 API、权限、数据流、路由
- 不改造公开（public）页面
- judge/score 的排行榜、筛选区、详情 Drawer 不做卡片化（仅保证不溢出）
- 不引入新依赖

## Decisions

### D1: 双渲染 `isMobile ? <Cards/> : <Table/>`，而非 antd Table responsive/column hiding
- 现有"删列"策略被 Issue 明确否定（信息缺失、日期换行难看），卡片才是需求
- antd Table 没有内置 row→card 变换能力
- 备选：CSS 强制 Table 转块级（display:block hack）——破坏 antd 布局与可访问性，放弃

### D2: 轻量内联卡片 + 统一样式约定，不抽象 `MobileCardList` 组件
- 8 页卡片结构差异大（用户卡/邀请卡/队伍成员卡/时间卡/报告卡/资源卡），强行抽象 props 复杂度高于收益
- 约定写在 `admin-mobile-card-list` spec：玻璃底/描边/圆角/色阶/三层结构（主行→详情→操作区细分隔线）
- 备选：抽 `<MobileCardList items renderItem/>` —— 若第 3 个页面出现完全同构卡片时再升级，YAGNI

### D3: 断点统一 `!screens.md`，与现有页面一致
- 与 `assessment/time`、`achievement` 等保持一致；不使用 `sm`，避免平板（md~lg）也走卡片

### D4: 复用现有状态与回调，卡片只是渲染层
- 所有卡片操作直接调用页面已有的 handler（handleEdit / handleDeleteClick / handleInvite...），Modal/Drawer 全部复用，不复制逻辑
- 分页、筛选、选中状态沿用现有 state，断点切换天然保持（React 状态不随渲染分支丢失）

### D5: 批量能力在移动端保留（users checkbox、github-invitations checkbox）
- 已在设计稿中与用户确认保留；实现为卡片主行 Checkbox + 顶部批量按钮，与 `selectedRowKeys` 共用

### D6: resources 移动端禁拖拽、用 ↑↓ 调序按钮
- dnd-kit 的 PointerSensor 在触屏上与页面滚动冲突，体验差
- 上移/下移复用同一排序更新 API；只改交互入口，后端与乐观更新逻辑不变

### D7: ai-traces 详情页移动端用横向滚动 chips 替代左栏列表
- 左栏 264px 固定 + 右栏 flex 是挤压根因；移动端改为 `overflow-x-auto` 的胶囊条（保留 seq + 时间 + 激活高亮）
- statistics 页移动端 flex-col 堆叠 + 卡片全宽 + 图表高度缩减，不改图表组件本身

## Risks / Trade-offs

- [judge/score 1650 行，内嵌表格与队伍头行逻辑复杂] → 只替换 submissionColumns/submissionMemberColumns 渲染处；桌面分支保持原代码路径不动；改动后用桌面视口回归队伍展开/评分流程
- [双渲染增加 JSX 体积与维护成本] → 卡片分支集中在各文件的一个 render 函数中；桌面 columns 定义不动
- [断点切换瞬间闪动] → antd useBreakpoint 在挂载后有短暂 undefined（全 false→isMobile=true），SSR 与客户端一致性需以 CSS 兜底非关键；本批页面均为 client 组件，影响仅限首帧，接受
- [antd v6 Table/Card 暗色 token] → 沿用各页现有 glass 写法（Tailwind 类），不新增 ConfigProvider

## Migration Plan

纯前端增量改动，按页面逐个提交（每页一个 commit），任意页面可独立回退（git revert 单页 commit）。无数据迁移。

## Open Questions

- 资源库移动端"上移/下移"按钮的具体图标与放置（操作区左侧 vs 独立调序区）——实现时按设计稿（操作区 `ArrowUpOutlined`/`ArrowDownOutlined` 文字按钮）执行
