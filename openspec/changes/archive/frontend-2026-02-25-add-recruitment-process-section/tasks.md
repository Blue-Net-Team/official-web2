# 招新流程组件实现任务清单

## 任务概览

基于OpenSpec设计文档，实现首页招新流程区块组件。

---

## 任务列表

### 1. 创建组件目录结构

**状态**: 已完成

**描述**: 创建 `RecruitmentProcess` 组件目录及子目录

**文件变更**:
```
创建 src/components/Home/RecruitmentProcess/
创建 src/components/Home/RecruitmentProcess/ProcessCard/
```

**验收标准**:
- [x] 目录结构符合项目规范
- [x] 参考 `DirectionIntroduce` 组件的目录组织方式

---

### 2. 实现 ProcessCard 子组件

**状态**: 已完成

**描述**: 实现流程卡片组件，支持图标、标题、描述和可选按钮

**文件变更**:
```
创建 src/components/Home/RecruitmentProcess/ProcessCard/index.tsx
创建 src/components/Home/RecruitmentProcess/ProcessCard/styles.module.css
```

**实现细节**:
- Props接口: `icon`, `title`, `description`, `showJoinButton?`, `onJoinClick?`
- 卡片尺寸: 260px × 340px
- 圆角: 36px
- 边框: 1px solid #E86835
- 内边距: 22px 24px
- 图标尺寸: 32px × 32px
- 标题: 20px Bold, Microsoft YaHei, 白色
- 描述: 16px Regular, Microsoft YaHei, 白色
- 按钮: 仅在 `showJoinButton=true` 时显示

**验收标准**:
- [x] 组件接受所有定义的Props
- [x] 样式符合设计稿规范
- [x] 按钮点击事件正确触发
- [x] TypeScript类型定义完整

---

### 3. 实现 RecruitmentProcess 主组件

**状态**: 已完成

**描述**: 实现招新流程主组件，管理三个流程步骤的数据和布局

**文件变更**:
```
创建 src/components/Home/RecruitmentProcess/index.tsx
创建 src/components/Home/RecruitmentProcess/styles.module.css
```

**实现细节**:
- 容器尺寸: 1270px × 780px
- 内边距: 70.5px 112px
- 定义三个流程步骤数据:
  1. 报名加入 - 带"立即加入"按钮
  2. 参加考核
  3. 正式录用
- 卡片间使用双箭头连接指示器 (使用 AntD DoubleRightOutlined)
- 箭头尺寸: 64px × 64px

**验收标准**:
- [x] 三个流程卡片正确渲染
- [x] 卡片间箭头指示器正确显示
- [x] 布局符合设计稿规范
- [x] "立即加入"按钮点击跳转正确

---

### 4. 准备图标资源

**状态**: 已完成

**描述**: 准备或复用流程卡片所需的图标资源

**文件变更**:
```
已存在 src/assets/HomeRecruitmentProcess/
├── register_icon.png      # 报名图标
├── assessment_icon.png    # 考核图标
└── admission_icon.png     # 录用图标
```

**验收标准**:
- [x] 图标尺寸为 32px × 32px
- [x] 图标清晰可识别
- [x] 路径引用正确

---

### 5. 集成到首页

**状态**: 已完成

**描述**: 将 RecruitmentProcess 组件集成到首页 page.tsx

**文件变更**:
```
修改 src/app/(public)/(home)/page.tsx
```

**实现细节**:
- 在 `<DirectionIntroduce />` 组件下方添加 `<RecruitmentProcess />`
- 添加必要的 import 语句

**代码示例**:
```tsx
import RecruitmentProcess from '@/components/Home/RecruitmentProcess';

// ... 其他代码 ...
<DirectionIntroduce />
<RecruitmentProcess />
```

**验收标准**:
- [x] 组件正确导入
- [x] 组件在正确位置渲染
- [x] 页面无编译错误

---

### 6. 样式优化与调整

**状态**: 已完成

**描述**: 确保样式与设计稿一致，处理响应式适配

**文件变更**:
```
修改 src/components/Home/RecruitmentProcess/styles.module.css
修改 src/components/Home/RecruitmentProcess/ProcessCard/styles.module.css
```

**验收标准**:
- [x] 颜色值与设计稿一致 (#E86835 等)
- [x] 字体大小和字重正确
- [x] 间距和边距符合规范
- [x] 圆角和边框正确

---

### 7. 功能测试

**状态**: 已完成

**描述**: 验证组件功能完整性

**测试项**:
- [x] 三个流程卡片正确显示
- [x] 卡片内容（标题、描述）正确
- [x] "立即加入"按钮点击跳转正常
- [x] 组件在首页正确位置显示
- [x] 无控制台错误

---

### 8. 代码审查

**状态**: 已完成

**描述**: 代码质量和规范检查

**检查项**:
- [x] TypeScript 类型定义完整
- [x] 代码风格符合项目规范
- [x] 命名规范（小驼峰）
- [x] 无未使用的导入
- [x] CSS Modules 使用正确

---

## 依赖关系

```
任务1 (创建目录)
    ↓
任务4 (准备图标) ←→ 任务2 (ProcessCard)
    ↓
任务3 (主组件)
    ↓
任务5 (集成到首页)
    ↓
任务6 (样式优化)
    ↓
任务7 (功能测试) → 任务8 (代码审查)
```

---

## 备注

- 参考组件: `src/components/Home/DirectionIntroduce/`
- 设计稿: Pixso item-id=29:22 (主容器), 29:25 (流程卡片)
- 放置位置: `src/app/(public)/(home)/page.tsx` 中 `<DirectionIntroduce />` 下方
- 箭头图标: 使用 Ant Design 的 `<DoubleRightOutlined />`
