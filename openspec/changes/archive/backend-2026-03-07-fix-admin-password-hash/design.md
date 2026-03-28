## Context

系统用户初始化功能已在 `add-system-user-config` 变更中实现，通过 `SystemUserInitializer` 在应用启动时自动创建系统管理员账号。当前实现使用 BCrypt 直接加密配置文件中的明文密码。

**当前密码处理流程：**
```
配置文件密码 (明文) → BCrypt.encode() → 数据库存储
```

**前端登录流程（预期）：**
```
用户输入密码 → SHA-256 哈希 → 发送到后端 → BCrypt.matches(sha256Hash, storedPassword)
```

**问题根源：**
- 后端存储的是 `BCrypt(明文密码)`
- 前端发送的是 `SHA-256(明文密码)`
- 后端验证时 `BCrypt.matches(SHA-256(明文密码), BCrypt(明文密码))` 永远返回 false

**约束条件：**
- 必须与前端登录流程保持一致
- 不能影响其他用户账号的密码验证逻辑
- 遵循项目 DDD 四层架构

## Goals / Non-Goals

**Goals:**
- 修改系统用户初始化逻辑，对配置密码先进行 SHA-256 哈希，再进行 BCrypt 加密
- 确保管理员账号可以正常登录

**Non-Goals:**
- 不修改其他用户的密码处理逻辑
- 不修改前端代码
- 不修改 BCrypt 密码编码器配置

## Decisions

### 1. 密码预哈希位置

**决定：** 在 `SystemUserInitializer` 中添加 SHA-256 预哈希逻辑。

**理由：**
- 问题仅存在于系统用户初始化场景
- 其他用户通过报名审核创建，密码由系统随机生成，处理逻辑不同
- 最小化修改范围，降低风险

**替代方案：**
- 修改 `PasswordEncoder` 配置：影响所有用户，风险太大
- 创建新的密码编码器：增加复杂度，当前场景不需要

### 2. SHA-256 实现方式

**决定：** 使用 Java 标准 `MessageDigest` 实现 SHA-256 哈希。

**实现代码：**
```java
private String sha256Hash(String input) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("SHA-256 algorithm not available", e);
    }
}
```

**理由：**
- JDK 标准库实现，无需引入额外依赖
- 与前端 SHA-256 实现兼容（标准算法）

### 3. 密码处理流程

**修改后的流程：**
```
配置文件密码 (明文)
    → SHA-256 哈希
    → BCrypt.encode()
    → 数据库存储
```

**验证流程：**
```
前端发送: SHA-256(明文密码)
后端存储: BCrypt(SHA-256(明文密码))
验证结果: BCrypt.matches(SHA-256(明文密码), BCrypt(SHA-256(明文密码))) = true
```

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 已存在的系统用户密码格式不兼容 | 提供数据库迁移脚本或手动删除后重新初始化 |
| SHA-256 实现与前端不一致 | 使用标准 SHA-256 算法，输出小写十六进制字符串 |
| 配置文件中的明文密码泄露风险 | 建议生产环境使用环境变量覆盖，且使用强密码 |

## Migration Plan

1. **部署前**: 无需数据库迁移，建议删除已有的系统用户记录
2. **部署**: 滚动更新后端服务，系统用户将使用新格式重新创建
3. **回滚**: 直接回滚代码即可，但需注意密码格式变化
