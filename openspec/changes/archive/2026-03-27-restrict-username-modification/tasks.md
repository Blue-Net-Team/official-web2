## 1. 后端实现

- [x] 1.1 修改 `UserInfoServiceImpl.validateProfileUpdatePermission()` 方法，添加 `username` 字段的权限检查
- [x] 1.2 更新错误消息，包含"用户名"字段

## 2. 测试验证

- [x] 2.1 添加单元测试：CANDIDATE 修改 username 返回 403
- [x] 2.2 添加单元测试：MEMBER 修改 username 返回 200
- [x] 2.3 运行现有测试确保无回归
