## ADDED Requirements

### Requirement: 组件位置迁移
AdminSideBar 组件 SHALL 从 `components/Admin/AdminSideBar/` 迁移至 `components/Admin/AdminNav/AdminSideBar.tsx`。AdminHeadBar 组件 SHALL 从 `components/Admin/AdminHeadBar/` 迁移至 `components/Admin/AdminNav/AdminHeadBar.tsx`。

#### Scenario: 导入路径更新
- **WHEN** 其他组件需要使用导航组件
- **THEN** 从 `@/components/Admin/AdminNav` 导入，而非原来的独立路径

#### Scenario: 旧目录删除
- **WHEN** 迁移完成后
- **THEN** `components/Admin/AdminSideBar/` 和 `components/Admin/AdminHeadBar/` 目录被删除
