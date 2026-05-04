## 1. 密码预哈希实现

- [x] 1.1 在 `SystemUserInitializer` 中添加 SHA-256 哈希方法
- [x] 1.2 修改系统用户创建逻辑，对密码先进行 SHA-256 哈希再 BCrypt 加密

## 2. 测试

- [x] 2.1 更新 `SystemUserInitializerTest` 测试用例，验证 SHA-256 预哈希逻辑
- [x] 2.2 添加集成测试验证管理员登录流程

## 3. 数据迁移

- [x] 3.1 提供删除现有系统用户的 SQL 脚本（可选）
