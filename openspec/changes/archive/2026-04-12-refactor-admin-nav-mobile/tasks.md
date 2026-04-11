## 1. 目录结构创建

- [x] 1.1 创建 `src/frontend/src/components/Admin/AdminNav/` 目录
- [x] 1.2 创建 `AdminNav/index.tsx` 容器组件文件
- [x] 1.3 创建 `AdminNav/AdminHeadBar.tsx` 组件文件
- [x] 1.4 创建 `AdminNav/AdminSideBar.tsx` 组件文件

## 2. 容器组件实现

- [x] 2.1 实现 `AdminNav/index.tsx` 容器组件，管理 `isMobile` 和 `drawerVisible` 状态
- [x] 2.2 添加 `useEffect` 监听窗口大小变化，更新 `isMobile` 状态
- [x] 2.3 实现 `openDrawer` 和 `closeDrawer` 回调函数
- [x] 2.4 渲染 `AdminHeadBar` 和 `AdminSideBar` 子组件，传递 props

## 3. AdminHeadBar 组件实现

- [x] 3.1 从原 `AdminHeadBar` 迁移 Logo 和标题渲染逻辑
- [x] 3.2 添加移动端菜单按钮（透明背景，汉堡图标）
- [x] 3.3 菜单按钮仅在移动端显示，位于 Header 右侧
- [x] 3.4 点击菜单按钮调用 `onMenuClick` 回调

## 4. AdminSideBar 组件实现

- [x] 4.1 从原 `AdminSideBar` 迁移菜单配置和权限过滤逻辑
- [x] 4.2 桌面端：渲染 Ant Design Sider 组件，保持原有折叠行为
- [x] 4.3 移动端：渲染 Ant Design Drawer 组件，从右侧弹出
- [x] 4.4 Drawer 配置：`placement="right"`、`width={200}`、隐藏 header
- [x] 4.5 Drawer body 背景色设置为 `#001529`
- [x] 4.6 点击菜单项后自动关闭 Drawer

## 5. 布局文件更新

- [x] 5.1 更新 `src/frontend/src/app/admin/layout.tsx` 的导入路径
- [x] 5.2 将 `AdminHeadBar` 和 `AdminSideBar` 替换为 `AdminNav` 组件

## 6. 清理旧文件

- [x] 6.1 删除 `src/frontend/src/components/Admin/AdminHeadBar/` 目录
- [x] 6.2 删除 `src/frontend/src/components/Admin/AdminSideBar/` 目录

## 7. 测试验证

- [x] 7.1 桌面端测试：Sider 正常显示，折叠/展开功能正常
- [x] 7.2 移动端测试：菜单按钮显示在 Header 右侧
- [x] 7.3 移动端测试：点击菜单按钮，Drawer 从右侧弹出
- [x] 7.4 移动端测试：Drawer 不挤压右侧内容
- [x] 7.5 移动端测试：点击菜单项或遮罩关闭 Drawer
- [x] 7.6 响应式测试：调整窗口大小，自动切换显示模式
- [x] 7.7 权限测试：菜单项权限过滤功能正常
