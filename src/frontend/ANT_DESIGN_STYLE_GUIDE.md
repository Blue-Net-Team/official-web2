# Ant Design 样式使用规范

## 优先级（从高到低）

### 1. ThemeProvider 全局主题（首选）
所有 Ant Design 组件的样式应优先在 `ThemeProvider.tsx` 中通过 `ConfigProvider` 的 `theme` 属性配置。

```tsx
// ✅ 正确 - 样式由 ThemeProvider 统一管理
<Button type="primary">提交</Button>

// ❌ 错误 - 不应为每个按钮写颜色
<Button type="primary" style={{ backgroundColor: '#fa8c16' }}>提交</Button>
```

### 2. 组件级 ConfigProvider
当某个区域需要特殊的主题时，使用局部 `ConfigProvider`：

```tsx
// ✅ 正确 - 局部主题覆盖
<ConfigProvider theme={{ components: { Button: { borderRadius: 20 } } }}>
  <Button>圆角按钮</Button>
</ConfigProvider>

// ❌ 错误 - 直接写 style
<Button style={{ borderRadius: 20 }}>圆角按钮</Button>
```

### 3. Tailwind CSS className
布局、间距、尺寸等使用 Tailwind：

```tsx
// ✅ 正确 - 布局用 Tailwind
<div className="flex items-center gap-4">
  <Button>按钮</Button>
</div>

// ❌ 错误 - 布局用 style
<div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
  <Button>按钮</Button>
</div>
```

### 4. style 属性（仅作为最后手段）
仅在以下情况使用 `style`：
- 动态计算的值（如 `style={{ width: `${progress}%` }}`）
- 无法通过主题和 className 实现的特殊效果

```tsx
// ✅ 正确 - 动态值必须用 style
<div className="h-2 bg-primary rounded" style={{ width: `${progress}%` }} />

// ❌ 错误 - 静态样式用 style
<div style={{ width: '100%', height: '100vh', backgroundColor: '#000' }} />
```

## 图标样式

Ant Design 图标不接受主题 token，应使用 Tailwind className：

```tsx
// ✅ 正确
<UserOutlined className="text-2xl text-white" />
<TrophyOutlined className="text-2xl text-[#1890ff]" />

// ❌ 错误
<UserOutlined style={{ fontSize: 24, color: '#fff' }} />
```

## 常见替换对照表

| ❌ 内联 style | ✅ 推荐方式 |
|--------------|-----------|
| `style={{ width: '100%' }}` | `className="w-full"` |
| `style={{ display: 'flex' }}` | `className="flex"` |
| `style={{ padding: 24 }}` | `className="p-6"` |
| `style={{ marginBottom: 12 }}` | `className="mb-3"` |
| `style={{ color: 'rgba(255,255,255,0.4)' }}` | `className="text-white/40"` |
| `style={{ fontSize: 16 }}` | `className="text-base"` |
| `style={{ minHeight: '100vh' }}` | `className="min-h-screen"` |

## ThemeProvider 已配置组件

查看 `ThemeProvider.tsx` 中 `components` 字段，包含：Menu, Layout, Button, Tabs, Input, InputNumber, Select, Table, Tag, Upload, Tree, Pagination, Card, Form, Modal, Drawer, Spin, Badge, Descriptions, Radio, Checkbox, Result, Empty, Popconfirm, Alert, Dropdown, Timeline, Steps, Switch

如需新增组件主题配置，请添加到 ThemeProvider。
