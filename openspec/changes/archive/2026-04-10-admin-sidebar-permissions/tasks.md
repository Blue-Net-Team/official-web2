## 1. 菜单配置

- [x] 1.1 在 AdminSideBar 组件中定义菜单配置数组，包含所有菜单项的 key、label、path、minLevel、disabled、icon 字段
- [x] 1.2 定义考核子菜单结构（考核时间 minLevel=2、考核题目 minLevel=2、考核评判 minLevel=1）

## 2. 权限过滤逻辑

- [x] 2.1 从 authStore 获取 userInfo，使用 getRoleLevel() 计算当前用户权限等级
- [x] 2.2 实现菜单项过滤函数：递归过滤配置数组，移除 minLevel > 当前用户 roleLevel 的项
- [x] 2.3 处理考核子菜单：过滤后如果子项全部不可见则隐藏父级"考核"菜单

## 3. 导航功能

- [x] 3.1 引入 Next.js useRouter，在 Menu 的 onClick 回调中调用 router.push(path)
- [x] 3.2 使用 pathname 同步菜单选中状态（selectedKeys 根据当前路由高亮）

## 4. QA管理灰显

- [x] 4.1 在菜单配置中为 QA管理 设置 disabled: true
- [x] 4.2 确认灰显样式在 dark theme 下视觉正确

## 5. 验证

- [x] 5.1 验证 MEMBER 角色只看到：回到首页、报名管理、考核（仅考核评判）、QA管理（灰显）
- [x] 5.2 验证 DIRECTION_ADMIN 角色看到全部考核子菜单 + 无竞赛/成就
- [x] 5.3 验证 SUPER_ADMIN 角色看到完整菜单
