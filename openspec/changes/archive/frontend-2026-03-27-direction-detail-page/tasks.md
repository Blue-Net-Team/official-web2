## 1. 数据层与路由

- [x] 1.1 创建方向数据类型定义 `src/components/Direction/types.ts`
- [x] 1.2 创建方向数据文件 `src/components/Direction/data.ts`（包含三个方向的数据）
- [x] 1.3 创建动态路由页面 `src/app/(public)/(other)/direction/[slug]/page.tsx`

## 2. Hero Section 组件

- [x] 2.1 创建 HeroSection 组件 `src/components/Direction/HeroSection/index.tsx`
- [x] 2.2 实现 HeroSection 样式 `src/components/Direction/HeroSection/styles.module.css`
- [x] 2.3 实现桌面端背景装饰效果（渐变圆形、网格线、六边形、点等）
- [x] 2.4 实现移动端背景装饰效果

## 3. Tech Stack 组件

- [x] 3.1 创建 TechStack 组件 `src/components/Direction/TechStack/index.tsx`
- [x] 3.2 实现 TechStack 样式 `src/components/Direction/TechStack/styles.module.css`
- [x] 3.3 实现桌面端 4 列水平布局
- [x] 3.4 实现移动端 2x2 网格布局

## 4. Learning Path 组件

- [x] 4.1 创建 LearningPath 组件 `src/components/Direction/LearningPath/index.tsx`
- [x] 4.2 实现 LearningPath 样式 `src/components/Direction/LearningPath/styles.module.css`
- [x] 4.3 实现桌面端水平布局带箭头连接
- [x] 4.4 实现移动端垂直布局

## 5. Career Section 组件

- [x] 5.1 创建 CareerSection 组件 `src/components/Direction/CareerSection/index.tsx`
- [x] 5.2 实现 CareerSection 样式 `src/components/Direction/CareerSection/styles.module.css`
- [x] 5.3 实现桌面端左右布局（文字+图片）
- [x] 5.4 实现移动端上下布局

## 6. Recruitment Info 组件

- [x] 6.1 创建 RecruitmentInfo 组件 `src/components/Direction/RecruitmentInfo/index.tsx`
- [x] 6.2 实现 RecruitmentInfo 样式 `src/components/Direction/RecruitmentInfo/styles.module.css`
- [x] 6.3 实现渐变背景卡片
- [x] 6.4 实现"立即申请"按钮点击跳转到 `/enroll`

## 7. 主题色系统

- [x] 7.1 定义三个方向的主题色 CSS 变量
- [x] 7.2 在页面级别注入对应方向的主题色变量
- [x] 7.3 确保所有组件使用主题色变量

## 8. 图片资源

- [x] 8.1 导出计算机视觉方向相关图片到 `src/assets/direction/cv/`
- [x] 8.2 导出嵌入式开发方向相关图片到 `src/assets/direction/embed/`
- [x] 8.3 导出结构设计方向相关图片到 `src/assets/direction/struct/`

## 9. 集成与测试

- [x] 9.1 创建组件统一导出文件 `src/components/Direction/index.ts`
- [x] 9.2 在动态路由页面组装所有组件
- [x] 9.3 实现 404 处理（无效 slug）
- [x] 9.4 测试三个方向的页面渲染
- [x] 9.5 测试响应式布局（桌面端、平板、移动端）
- [x] 9.6 验证从主页 DirectionIntroduce 点击跳转功能
