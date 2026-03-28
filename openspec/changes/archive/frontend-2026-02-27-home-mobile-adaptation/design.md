## Context

当前主页采用固定宽度布局（1270px），所有组件使用固定像素值定义尺寸和间距。这种布局方式在桌面端显示正常，但在移动设备上会出现以下问题：

1. **UI设计文件问题**：`docs/UI/home/Index.html` 宽度固定为1270px，未占满视口
2. **内容溢出**：固定宽度导致横向滚动
3. **字体过大**：移动端字体未缩放，阅读体验差
4. **卡片布局问题**：多列卡片在小屏幕上挤压变形
5. **导航栏问题**：桌面导航栏在移动端占用过多空间

项目已集成 Ant Design 组件库，可利用其 Grid 系统实现响应式布局。

## Goals / Non-Goals

**Goals:**
- 修正UI设计文件宽度，使其占满浏览器视口
- 实现三个断点的响应式适配：移动端（<768px）、平板（768px-1024px）、桌面（>1024px）
- 保持现有功能和视觉风格不变
- 优化移动端用户体验，减少横向滚动
- 实现移动端导航栏折叠功能

**Non-Goals:**
- 不改变现有业务逻辑和数据流
- 不添加新的功能特性
- 不重构现有组件的内部实现逻辑
- 不支持IE浏览器

## Decisions

### 1. 响应式断点策略

**决定**：采用三断点设计
- 移动端：< 768px
- 平板：768px - 1024px  
- 桌面：> 1024px

**理由**：
- 与Ant Design的断点系统保持一致
- 覆盖主流设备尺寸
- 便于维护和测试

**替代方案**：使用更多断点（如480px、640px等）
- **未采纳原因**：增加复杂度，维护成本高，三断点已能满足需求

### 2. 布局适配方案

**决定**：使用CSS媒体查询 + Ant Design Grid系统

**理由**：
- 项目已集成Ant Design，Grid系统成熟稳定
- CSS媒体查询提供细粒度控制
- 两者结合可快速实现响应式布局

**实现方式**：
```css
/* 移动端 */
@media (max-width: 767px) {
  .container { padding: 20px; }
}

/* 平板 */
@media (min-width: 768px) and (max-width: 1023px) {
  .container { padding: 40px; }
}

/* 桌面 */
@media (min-width: 1024px) {
  .container { padding: 93px; }
}
```

### 3. 卡片布局策略

**决定**：移动端单列，平板双列，桌面三列

**理由**：
- 移动端屏幕宽度有限，单列布局保证内容可读性
- 平板双列平衡内容密度和可读性
- 桌面三列保持现有设计

**实现方式**：使用Ant Design的Col组件配合响应式属性
```tsx
<Col xs={24} sm={12} md={8}>
  <Card />
</Col>
```

### 4. 导航栏移动端方案

**决定**：使用Ant Design Drawer实现抽屉式导航 ✅ **已完成**

**理由**：
- Drawer组件已集成，无需引入新依赖
- 抽屉式导航是移动端主流方案
- 支持手势关闭，用户体验好

**实现状态**：
- ✅ 已实现汉堡菜单按钮
- ✅ 已实现抽屉导航
- ✅ 已实现移动端/桌面端切换逻辑
- ✅ 已修复isMobile判断逻辑（从`window.innerHeight > window.innerWidth`改为`window.innerWidth < 768`）

**替代方案**：使用Dropdown下拉菜单
- **未采纳原因**：不适合导航项较多的情况，交互体验不如抽屉

### 5. Ant Design组件适配策略

**决定**：优先使用ConfigProvider进行主题配置，而非直接修改CSS

**理由**：
- ConfigProvider是Ant Design官方推荐的主题定制方式
- 可以统一控制所有Ant Design组件的样式
- 避免CSS优先级冲突
- 代码更易维护

**实现方式**：
```tsx
import { ConfigProvider } from 'antd';

const theme = {
  token: {
    fontSize: 16,
    fontSizeHeading1: 32,
    fontSizeHeading2: 24,
    padding: 20,
    margin: 16,
  },
  components: {
    Button: {
      controlHeight: 44,
    },
    Menu: {
      itemSize: 48,
    },
  },
};

<ConfigProvider theme={theme}>
  <App />
</ConfigProvider>
```

**适用场景**：
- 字体大小调整
- 组件间距调整
- 组件尺寸调整
- 颜色主题调整

### 6. 自定义组件字体缩放策略

**决定**：对于非Ant Design组件，使用CSS clamp()函数实现流体字体

**理由**：
- clamp()可在最小值和最大值之间平滑过渡
- 避免媒体查询中的字体突变
- 代码简洁易维护

**实现方式**：
```css
.title {
  font-size: clamp(24px, 5vw, 48px);
}
```

## Risks / Trade-offs

**风险1：旧浏览器兼容性**
- **风险**：clamp()等CSS新特性在旧浏览器不支持
- **缓解**：提供降级方案，使用媒体查询作为fallback

**风险2：测试覆盖不足**
- **风险**：设备碎片化导致部分设备显示异常
- **缓解**：使用Chrome DevTools设备模拟器测试主流设备，并收集真实设备反馈

**风险3：性能影响**
- **风险**：媒体查询和响应式图片可能影响性能
- **缓解**：使用CSS变量减少重复计算，图片使用响应式srcset

**风险4：设计一致性**
- **风险**：响应式调整可能破坏原有设计风格
- **缓解**：保持关键视觉元素（颜色、圆角、阴影）不变，仅调整尺寸和布局

## Migration Plan

### 阶段1：UI设计文件修正
1. 修改 `docs/UI/home/Index.html` 的容器宽度为100%
2. 调整内部元素使用相对单位
3. 用户确认设计文件合理性

### 阶段2：基础响应式框架
1. 创建全局响应式CSS变量
2. 修改主页容器样式
3. 测试基础布局

### 阶段3：组件逐个适配
1. TopContent组件适配
2. Competitions组件适配
3. DirectionIntroduce组件适配
4. RecruitmentProcess组件适配
5. 其他组件适配

### 阶段4：导航栏适配 ✅ **已完成**
1. ✅ 实现移动端抽屉导航
2. ✅ 添加汉堡菜单按钮
3. ✅ 测试导航交互
4. ✅ 修复isMobile判断逻辑

### 阶段5：测试与优化
1. 多设备测试
2. 性能优化
3. 用户反馈收集

**回滚策略**：每个阶段独立提交，如有问题可快速回滚到上一版本
