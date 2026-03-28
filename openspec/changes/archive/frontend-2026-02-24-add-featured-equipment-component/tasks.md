## 1. 组件目录创建

- [x] 1.1 创建 `src/components/Home/FeaturedEquipment/` 目录
- [x] 1.2 创建 `src/components/Home/FeaturedEquipment/index.tsx` 组件文件
- [x] 1.3 创建 `src/components/Home/FeaturedEquipment/styles.module.css` 样式文件

## 2. 静态资源准备

- [x] 2.1 从 Pixso 导出设备图标图片，保存到 `src/assets/` 目录（使用SVG实现）
- [x] 2.2 从 Pixso 导出装饰背景图片，保存到 `src/assets/` 目录（已存在 equipment_bg.png）

## 3. 组件实现

- [x] 3.1 实现组件基础结构（Flex 布局）
- [x] 3.2 实现设备图标展示区域
- [x] 3.3 实现标题文字样式（35px 粗体白色）
- [x] 3.4 实现描述文字样式（20px 白色）
- [x] 3.5 实现"浏览更多团队装备"按钮（白色背景，圆角20px）
- [x] 3.6 实现容器CSS背景图片装饰（右侧装饰图，参考主页bg1/bg2实现方式）

## 4. 样式实现

- [x] 4.1 实现容器渐变背景（透明到紫蓝色）
- [x] 4.2 实现内容容器样式（圆角72px，紫色边框）
- [x] 4.3 实现按钮悬停效果
- [x] 4.4 实现响应式布局适配

## 5. 主页集成

- [x] 5.1 在 `src/app/(public)/(home)/page.tsx` 中导入 FeaturedEquipment 组件
- [x] 5.2 在页面适当位置调用组件

## 6. 验证

- [x] 6.1 运行 TypeScript 类型检查，确保无类型错误
- [x] 6.2 运行 ESLint 检查，确保无 lint 错误
- [x] 6.3 在浏览器中验证组件显示效果与设计稿一致
