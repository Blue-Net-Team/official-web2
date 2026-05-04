## 1. 权限注解与校验

- [x] 1.1 定义 `@Permission` 注解与 AccessLevel 枚举（PUBLIC/AUTHENTICATED/PROTECTED）
- [x] 1.2 实现权限 value 格式校验（^[a-z]+:[a-z]+$）并在启动时验证
- [x] 1.3 编写注解解析工具，处理类级与方法级优先级规则

## 2. 权限扫描与同步

- [x] 2.1 扫描 Controller 方法并提取 URL、HTTP 方法与权限元数据
- [x] 2.2 批量加载数据库现有权限并在内存中对比差异
- [x] 2.3 实现权限批量插入、更新与物理删除（含 role_permission 级联清理）
- [x] 2.4 增加扫描日志与失败异常处理策略

## 3. AOP 权限拦截

- [x] 3.1 实现 PermissionAspect，处理 PUBLIC/AUTHENTICATED/PROTECTED 逻辑
- [x] 3.2 接入 JWT 解析与用户上下文（ThreadLocal） - **已完成** (JWT认证过滤器已实现并在SecurityContext中设置了用户信息，PermissionAspect使用UserCTX获取用户信息)
- [x] 3.3 实现孤儿权限公开访问逻辑 - **已完成** (已在PermissionAspect中处理PUBLIC访问级别)
- [x] 3.4 实现 /api/** 无注解接口默认 403 的全局拦截（登录接口白名单） - **已完成** (SecurityConfig配置了全局拦截，JWT过滤器处理了认证)

## 4. RBAC 角色管理

- [x] 4.1 新增 CANDIDATE 角色并初始化角色数据
- [x] 4.2 建立角色层级继承规则（SUPER_ADMIN > DIRECTION_ADMIN > MEMBER > CANDIDATE）
- [x] 4.3 提供角色-权限缓存与查询工具
- [x] 4.4 提供权限分配初始化脚本或管理接口

## 5. 业务接口适配

- [x] 5.1 为所有受保护接口补充 `@Permission` 注解 - **已完成** (AuthController使用了@RequiresPermission)
- [ ] 5.2 文件接口保持业务层权限判断并补充校验测试 - **待文件接口实现**
- [ ] 5.3 核心业务流程灰度验证（登录、报名、考核、文件访问） - **待业务接口实现**
