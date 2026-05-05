## 1. DTO 类型扩展

- [x] 1.1 在 `type.ts` 中新增 `CreateCollegeRequestDTO` 类型
- [x] 1.2 在 `type.ts` 中新增 `UpdateCollegeRequestDTO` 类型

## 2. 管理端 API 服务

- [x] 2.1 创建 `admin-college.service.ts`
- [x] 2.2 实现 `create(data)` 方法（POST /api/v1/admin/colleges）
- [x] 2.3 实现 `update(id, data)` 方法（PUT /api/v1/admin/colleges/{id}）
- [x] 2.4 实现 `delete(id)` 方法（DELETE /api/v1/admin/colleges/{id}）
- [x] 2.5 复用 `collegeService.getColleges()` 获取列表

## 3. 管理后台菜单

- [x] 3.1 在 `AdminNav/index.tsx` 的 `menuConfig` 中添加学院管理项
- [x] 3.2 配置路径 `/admin/college`，图标 `BankOutlined`，`minLevel: 3`

## 4. 学院管理主页面

- [x] 4.1 创建 `/admin/college/page.tsx` 页面框架
- [x] 4.2 实现学院列表表格（列：ID、学院名称）
- [x] 4.3 实现新增按钮，打开 Drawer 创建模式
- [x] 4.4 实现点击行查看/编辑
- [x] 4.5 实现删除确认 Modal

## 5. 学院编辑抽屉

- [x] 5.1 创建 `CollegeDrawer.tsx` 基础结构（Drawer + Form，三种模式）
- [x] 5.2 实现学院名称表单字段（必填，最大 100 字符）
- [x] 5.3 实现 view 模式只读展示 + 编辑/删除按钮
- [x] 5.4 实现表单提交逻辑（区分 create/update）

## 6. 集成验证

- [ ] 6.1 验证学院列表加载
- [ ] 6.2 验证创建学院流程
- [ ] 6.3 验证编辑学院流程
- [ ] 6.4 验证删除学院（含关联数据时的错误提示）
- [ ] 6.5 验证权限控制（普通用户不可访问）
