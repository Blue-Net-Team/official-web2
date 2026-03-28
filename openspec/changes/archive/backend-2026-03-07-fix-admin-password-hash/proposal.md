## Why

系统管理员初始化时，密码直接使用 BCrypt 加密明文密码存储，但前端登录时会对密码进行 SHA-256 哈希后再发送请求。这导致前端发送的 SHA-256 哈希值与后端存储的 BCrypt(明文密码) 无法匹配，管理员无法登录系统。

## What Changes

- 修改 `SystemUserInitializer` 初始化逻辑，对配置的密码先进行 SHA-256 哈希，再进行 BCrypt 加密存储
- 确保初始化的密码存储格式与前端登录流程一致

## Capabilities

### Modified Capabilities

- `system-user-initialization`: 修改系统用户初始化的密码处理逻辑，增加 SHA-256 预哈希步骤

## Impact

- **修改类**: `SystemUserInitializer` - 增加密码 SHA-256 预哈希逻辑
- **数据库**: 已有的系统用户密码需要手动更新或删除后重新初始化
- **兼容性**: 不影响其他用户账号，仅影响系统管理员初始化流程
