## 1. 拆掉多层高度/滚动契约

- [x] 1.1 在 `globals.css` 中新增 `--question-drawer-tabnav-h` 变量（带注释说明它是抽屉内唯一的布局常数），并删除 `.statement-tabs` 的 4 条全局规则
- [x] 1.2 移除 `QuestionDrawer.tsx` 中 `Form` 的 `flex flex-col flex-1 min-h-0` className 与 `Tabs` 的 `statement-tabs flex flex-col flex-1 min-h-0` className
- [x] 1.3 删除判题配置、资源限制两个 tab 的 `<div className="h-full overflow-y-auto">` 外壳，子内容直接作为 `children`

## 2. 抽屉 body 成为唯一滚动容器

- [x] 2.1 `Drawer.styles.body` 保留 `overflowY: auto` / `overflowX: hidden`，删除不再需要的 flex 纵列声明（若 flex 仅为高度链服务）
- [x] 2.2 确认 header / footer 在 body 滚动时保持固定（不随内容滚出视野）
- [x] 2.3 确认 `Tabs` 导航条未施加 sticky，随 body 一起向上滚出视野（不遮挡上方表单）

## 3. 题面 tab 改为「左列流动 + 右列 sticky」

- [x] 3.1 `renderStatementWorkspace` 中把 `flex gap-6 flex-1 min-h-[620px]` 改为自然高度的 flex row，左列去掉 `overflow-y-auto`
- [x] 3.2 右列改为 `sticky top-[var(--question-drawer-tabnav-h)] self-start max-h-... overflow-y-auto`，`max-h` 由该变量与视口推导，并补 `vh` fallback
- [x] 3.3 实测 sticky 是否被祖先 `overflow: hidden` 破坏；若被破坏，按 design.md D2 降级为「右列 `max-h` + 自滚、不 sticky」并记录结论

## 4. 切 tab 复位滚动

- [x] 4.1 为 `Form` 增加 ref，`Tabs.onChange` 中通过 `formRef.current.closest('.ant-drawer-body')` 复位 `scrollTop` 为 0
- [x] 4.2 验证多抽屉/嵌套容器场景下不会复位到错误容器

## 5. 回归验收（4 形态 × 3 tab）

- [x] 5.1 桌面编辑态：题面 tab 长表单滚动、右列预览在滚动中保持可见且有独立滚动条
- [x] 5.2 桌面编辑态：判题配置 tab 滚动时无内层滚动条，Generator 与标准解 Monaco 完整可见、不被裁切
- [x] 5.3 桌面编辑态：资源限制 tab 滚动时无内层滚动条，表格横向滚动仍可用
- [x] 5.4 桌面编辑态：滚动后导航条随内容滚出视野（不遮盖输入框），回到顶部切 tab 后滚动位置复位到顶部
- [x] 5.5 桌面查看态与窄屏编辑态（Segmented）：滚动行为与改动前一致，无新滚动条
- [x] 5.6 `tsc --noEmit` 与 eslint 通过
- [x] 5.7 重建前端镜像并重启容器，用 Playwright 实际滚动截图核对（含 1280×768 矮屏）

## 6. 文档同步

- [x] 6.1 更新 `docs/05-参考手册/05-03-算法判题指南.md` 的「滚动与高度」段落，改为单一滚动容器 + sticky 预览 + 吸顶 tab 栏的描述
- [x] 6.2 检查 `README`/开发指南中是否还有其他"左右两列各自滚动"的旧描述需要一并修改
