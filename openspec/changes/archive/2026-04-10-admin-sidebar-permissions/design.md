## Context

AdminSideBar 当前是纯静态组件，使用 Ant Design Menu 组件渲染固定的菜单项列表，无导航功能、无权限过滤。项目已有完整的角色体系（SUPER_ADMIN / DIRECTION_ADMIN / MEMBER / CANDIDATE），以及 `getRoleLevel()` 工具函数和 `authStore` 状态管理。需要利用这些已有基础设施实现权限感知的动态侧边栏。

## Goals / Non-Goals

**Goals:**
- 根据用户 roleLevel 动态过滤菜单项可见性
- 点击菜单项导航到对应 `/admin/*` 路由
- QA管理灰显标记为不可用
- 为后续 admin 子页面建立统一的菜单配置模式

**Non-Goals:**
- 不实现 admin 页面的权限守卫页面（无权限提示页不在本次范围）
- 不实现数据层面的权限过滤（如 DIRECTION_ADMIN 只看自己方向的数据）
- 不实现 QA 管理页面的实际功能

## Decisions

### 1. 菜单配置数据驱动

使用配置数组定义菜单项，每项包含 `key`、`label`、`path`、`minLevel`（最低可见 roleLevel）、`disabled` 等字段。通过 `filter` 过滤掉当前用户无权看到的项。

**替代方案**: 硬编码 `if/else` 分支 → 可维护性差，每增加菜单项需改动组件逻辑。

### 2. 权限级别映射

```
全员可见 (level >= 1):  回到首页、报名管理、考核评判、QA管理
方向管理员以上 (level >= 2):  考核时间、考核题目
仅超级管理员 (level >= 3):  竞赛管理、成就管理
```

使用 `getRoleLevel()` 获取当前用户等级，与菜单项 `minLevel` 比较即可。

### 3. 考核子菜单处理

MEMBER 只能看到"考核评判"一个子项时，考核父菜单仍然折叠显示（子项可能只有一个）。不做自动平铺优化，保持一致的视觉结构。

### 4. QA管理灰显

使用 Ant Design Menu 的 `disabled: true` 属性，配合 tooltip 提示"功能开发中"。

### 5. 导航方式

使用 Next.js `useRouter` 的 `router.push()` 进行客户端导航，菜单 `onClick` 回调触发路由跳转。

## Risks / Trade-offs

- **[风险]** 前端菜单过滤仅影响可见性，不替代后端权限校验 → 后端接口仍需独立的权限验证
- **[风险]** 成就管理权限可能后续调整 → 菜单配置独立维护，修改成本低
- **[权衡]** MEMBER 看到只有一个子项的折叠菜单 → 牺牲一点 UX 简洁性换取结构一致性
