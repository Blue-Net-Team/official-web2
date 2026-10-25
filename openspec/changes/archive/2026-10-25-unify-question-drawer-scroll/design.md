## Context

`QuestionDrawer` 是管理端唯一承载"题面 + 判题配置 + 资源限制"三类表单的抽屉，1550 行，内部有 4 种渲染形态：桌面编辑（左右分栏）、桌面查看（只渲染预览）、窄屏编辑（Segmented 切换）、窄屏查看。

当前的高度/滚动来源有三种，互相叠加：

| 区域 | 高度来源 | 滚动容器 |
|---|---|---|
| 题面 tab 左右列 | Form/Tabs 的 `flex-1 min-h-0` 链 + `min-h-[620px]` | 两列各自 `overflow-y-auto` |
| 判题配置 tab | `.statement-tabs` 全局 CSS 灌入的 `h-full` | 面板自身 `overflow-y-auto` |
| 资源限制 tab | 同上 | 面板自身 `overflow-y-auto` |
| 抽屉 body | antd 默认 `flex: 1` | 最近改为 `overflow-y: auto` |

`.statement-tabs` 那 4 条全局 CSS 规则的作用只有一个：把确定高度从 Drawer body 一路灌到 `.ant-tabs-tabpane`，因为 antd Tabs 的内容区默认不参与 flex 伸缩。这套补丁 + flex 链 + 最近加的 body 滚动同时活着，才出现"滚轮在 2~3 个区域间接力丢失"的体感。

约束：

- 滚动容器必须唯一且层级浅，否则嵌在面板里的 Monaco（固定 `height: 300/260`）与 antd Select 弹层会被内层 `overflow` 裁切
- 右列预览的价值在于"编辑时一直看得见"，因此它必须有一个与视口相关的高度上限，否则一旦随 body 流动就失去意义
- 不能引入新的全局 CSS 高度补丁；现有补丁应被删除

## Goals / Non-Goals

**Goals:**

- 抽屉 body 成为唯一纵向滚动容器，删掉所有内层 `overflow-y-auto` 面板与 flex 高度链
- 长表单（判题配置）在任意滚动位置都能切换 tab
- 题面 tab 的预览列在左列很长时仍然留在视野内
- 把"高度/滚动由谁负责"收敛成一条可陈述的规则，写进 spec，避免下一个 tab 又长出内层滚动

**Non-Goals:**

- 不重写 `QuestionDrawer` 的整体结构，不抽 `AdminDrawer` / `ScrollRegion` 抽象层（留待后续如果有第 4 个 tab 或第二个抽屉复用时再做）
- 不改 `buildPreviewStatement` 与共享题面组件 `QuestionStatement/*`
- 不改窄屏（`<lg`）的 Segmented 切换形态与查看态布局，只让它们继续吃 body 滚动
- 不改后端、接口、表单字段

## Decisions

### D1：唯一的垂直滚动容器 = Drawer body

**选择**：移除 `Form` 的 `flex flex-col flex-1 min-h-0`、`Tabs` 的 `statement-tabs flex flex-col flex-1 min-h-0`、`workspace` 的 `min-h-[620px]`、以及判题配置/资源限制两个 `h-full overflow-y-auto` 外壳；body 保持 `overflowY: auto`。

**为什么**：body 是唯一天然知道"可用高度"的容器，也是唯一不会裁切子元素浮层的层级。把滚动上移到最外层，内层就只需要表达内容，不再需要表达高度。

**替代方案**：

- *全部回到 tab 面板内滚（pinned 布局）*：没有魔法数字，但元信息块永远占用 ~250px，矮屏（1280×768）可编辑区只剩 400px 左右，且叠加 `.statement-tabs` 补丁。
- *用 `calc(100dvh - N)` 给两列固定高度*：N 必须同时覆盖 header、footer、body padding 与会随宽度换行的元信息块，任一变化都会错位。放弃。
- *保留内层滚动但统一成同一个外壳组件*：仍是两层滚动，只是把重复写法抽掉，体感问题没解决。

### D2：预览列用 sticky 保活，不用固定高度

**选择**：右列 `sticky` + `self-start` + `max-h` 视口上限 + `overflow-y-auto`；左列完全自然流动。

**为什么**：这样仍需的"确定高度"只剩一处，而且这一处的语义天然成立——预览列"不该比屏幕还高"。左列想多长就多长（题干 autoSize 到 40 行），不再被任何下限挤扁。

**替代方案**：给两列都 `calc(100dvh - N)`（回到 D1 已否掉的魔法数字）；或用 `IntersectionObserver` 手动实现吸附（多一份状态，收益相同）。

**边界**：左列内容比右列短时 sticky 不产生位移，等同于普通布局，无副作用。

### D3：Tabs 导航条不吸顶，随 body 一起滚出去

**选择**：Tabs 导航条不施加 sticky，与元信息、左列编辑控件一样随抽屉 body 向上滚出视野。预览列 sticky 偏移固定为 `top: 1rem`，`max-h` 用 `calc(100dvh - 3rem)`，不再引入任何导航条高度常数。

**为什么**：第一版实现把导航条做成了 `sticky top-0`，实际渲染时它悬停在元信息块上方，把「标题」输入框盖住了（截图确认）。抽屉顶部空间本来就被 header 与元信息占用，再钉一个横条只会遮内容。导航条滚走之后，预览列的 sticky 偏移也不再需要对齐任何东西，那个 `--question-drawer-tabnav-h` 常数随之取消 —— 整条链上不再有任何布局魔数。

**替代方案**：
- *给导航条加背景色 + z-index 并用 `box-shadow` 补齐下边距*：能让钉子不透明，但遮挡问题依然存在（内容被覆而不是被推），治标不治本。
- *把导航条移进 Drawer.header 并保留固定*：会让三题型共用一套 header 语义，且查看态无 tab，反而把两种形态的条件分支提前。

**代价**：长表单滚到深处要切 tab 得先滚回顶部。这个代价被 D4（切 tab 自动复位）与“单页滚动”的可预期性抵掉了。

### D4：切 tab 复位 body 滚动到顶部

**选择**：`Tabs.onChange` 中把 body 的 `scrollTop` 复位为 0。滚动容器通过 `Form` 的 ref 向上找最近的 `.ant-drawer-body`（`formRef.current.closest('.ant-drawer-body')`），不用全局 `document.querySelector`，避免多抽屉并存时打错容器。

**为什么**：body 变成唯一滚动容器后，从一个 2000px 长的判题配置 tab 切到题面 tab，会停在这段内容的中段，用户会以为内容缺失。

**替代方案**：保留每个 tab 的滚动位置（每个 tab 记住自己的 `scrollTop`）。

**权衡**：记忆位置对"来回对照两个 tab"更友好，但把三个 tab 的滚动状态变成受控状态；先选简单可预期的一种，spec 里写成明确场景，后续要改就改这条场景。

### D5：不改窄屏与查看态

**选择**：窄屏编辑（Segmented）与查看态继续依赖 body 滚动，不做 sticky、不做分栏。

**为什么**：这两条路径本来就没有内层滚动，也不是当前的痛点；把它们一起改会把回归面从 2 种形态扩大到 4 种。

## Risks / Trade-offs

- **[sticky 被祖先 `overflow: hidden` 破坏]** → antd Drawer 的 content wrapper 与 Drawer 内层若存在非 `visible` 的 overflow，sticky 会退化为静态定位。验收时必须实际滚动确认；若被破坏，退路是给预览列改回 `max-h + overflow-y-auto` 但不 sticky（D2 的降级版），并在 spec 里同步。
- **[长表单变成 2000px 长卷]** → 判题配置把 N 个标准解（每个 260px Monaco）竖着排，滚动手感变差。缓解：只有一层滚动，滚动条位置本身是个信号；若实际使用中标准解数量常超过 3 个，再考虑给标准解列表加折叠或虚拟化（非本次范围）。注意导航条不吸顶，滚到深处切 tab 需先回顶部（D3 已接受该代价）。
- **[metadata 滚走后再也看不到题号/标题]** → 抽屉标题仍在 header 固定，题号与标题在 header 里已能部分体现；若不足，后续可把标题做成 header 副标题，而不是回退滚动方案。
- **[`100dvh` 兼容性]** → 移动端 Safari 15.4 以下不支持 `dvh`；项目目标浏览器可接受，降级时用 `vh` 作为 fallback 声明。
- **[回归面]** → 需覆盖 4 种形态 × 3 个 tab 的滚动行为，验收清单写进 tasks.md，避免只测了桌面编辑态。

## Migration Plan

纯前端改动，随 `trigger/frontend` 版本号提交触发 CD 构建，无数据迁移。

回滚策略：`unify-question-drawer-scroll` 的改动集中在 `QuestionDrawer.tsx` 与 `globals.css` 两个文件，回滚这两个文件即恢复上一套契约（含已提交的 `min-h-[620px]` 版本）。

## Open Questions

- 预览列的 `max-h` 是"到视口底部"还是"留出 footer 高度"？两者视觉差别在矮屏上可见，实施时按实际截图定，spec 只约束"不超出视口"。
- 判题配置 tab 是否需要二级锚点（如「标准解」跳转）？依赖标准解实际平均数量，本次不做。
- 是否顺带把抽屉从 `width min(1120px, calc(100vw - 48px))` 提到更宽以缓解三栏表单拥挤？属独立议题，不在本变更内。
