# 招新流程组件技术规格

## 组件接口定义

### RecruitmentProcess (主组件)

**文件路径**: `src/components/Home/RecruitmentProcess/index.tsx`

**Props 接口**:
```typescript
interface RecruitmentProcessProps {
  // 暂无外部传入props，所有数据内部定义
}
```

**功能**:
- 渲染招新流程区块整体布局
- 管理三个流程卡片的数据配置
- 处理卡片间的箭头连接指示器渲染

---

### ProcessCard (流程卡片子组件)

**文件路径**: `src/components/Home/RecruitmentProcess/ProcessCard/index.tsx`

**Props 接口**:
```typescript
interface ProcessCardProps {
  /** 卡片图标路径 */
  icon: string;
  /** 卡片标题 */
  title: string;
  /** 卡片描述文本 */
  description: string;
  /** 是否显示"立即加入"按钮 */
  showJoinButton?: boolean;
  /** 按钮点击回调 */
  onJoinClick?: () => void;
}
```

**功能**:
- 渲染单个流程卡片
- 根据配置条件渲染"立即加入"按钮
- 处理按钮点击事件

---

## 数据结构

### 流程卡片数据

```typescript
interface ProcessStep {
  id: string;
  icon: string;
  title: string;
  description: string;
  showJoinButton?: boolean;
}

const processSteps: ProcessStep[] = [
  {
    id: 'register',
    icon: '/assets/HomeRecruitmentProcess/register_icon.png',
    title: '报名加入',
    description: '报名现已启动，每学年第一学期开始招新，不限制专业，仅对大一和大二的同学开放。',
    showJoinButton: true,
  },
  {
    id: 'assessment',
    icon: '/assets/HomeRecruitmentProcess/assessment_icon.png',
    title: '参加考核',
    description: '考核流程将会在报名结束后的两周内陆续启动，每个方向的考核时间和轮次都有差异，难度大体相同',
  },
  {
    id: 'admission',
    icon: '/assets/HomeRecruitmentProcess/admission_icon.png',
    title: '正式录用',
    description: '考核流程将会在报名结束后的两周内陆续启动，每个方向的考核时间和轮次都有差异，难度大体相同',
  },
];
```

---

## 样式规格

### RecruitmentProcess 样式

**文件路径**: `src/components/Home/RecruitmentProcess/styles.module.css`

```css
.container {
  width: 1270px;
  height: 780px;
  padding: 70.5px 112px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.title {
  /* 标题样式 */
}

.cardsContainer {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 23px 0 60px;
}

.arrowConnector {
  width: 64px;
  height: 64px;
  /* 双箭头样式 */
}
```

### ProcessCard 样式

**文件路径**: `src/components/Home/RecruitmentProcess/ProcessCard/styles.module.css`

```css
.card {
  width: 260px;
  height: 340px;
  border-radius: 36px;
  border: 1px solid #E86835;
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.header {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 13px;
}

.icon {
  width: 32px;
  height: 32px;
}

.title {
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: #FFFFFF;
}

.description {
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 16px;
  font-weight: 400;
  color: #FFFFFF;
  line-height: 1.5;
}

.joinButton {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 10px;
  background: #FFFFFF;
  border-radius: 64px;
  font-family: 'Microsoft YaHei', sans-serif;
  font-size: 14px;
  font-weight: 700;
  color: #000000;
  cursor: pointer;
  border: none;
  margin-top: auto;
}
```

---

## 文件结构

```
src/components/Home/RecruitmentProcess/
├── index.tsx                    # 主组件实现
├── styles.module.css            # 主组件样式
└── ProcessCard/
    ├── index.tsx                # 流程卡片组件
    └── styles.module.css        # 卡片样式

src/app/(public)/(home)/page.tsx  # 修改：添加 RecruitmentProcess 组件
```

---

## 依赖项

### 外部依赖
- React 18+
- Next.js 14+
- CSS Modules

### 内部依赖
- 项目现有全局样式
- 图标资源（需准备或复用现有图标）

---

## 路由/导航

- **立即加入按钮**: 点击后跳转至 `/recruitment` 或 `/join` 页面（根据实际路由配置）

---

## 响应式行为

| 断点 | 行为 |
|------|------|
| >= 1270px | 完整布局，三个卡片水平排列 |
| < 1270px | 考虑堆叠或横向滚动 |

---

## 可访问性

- 按钮需有明确的 `aria-label`
- 卡片内容需有良好的语义化结构
- 颜色对比度符合 WCAG 2.1 标准
