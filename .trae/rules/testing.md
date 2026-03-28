---
alwaysApply: false
description: 编写或调试测试代码时使用
---

# 测试规范

## 测试框架

- Testcontainers（集成测试）
- Spring Security Test
- MyBatis-Plus Test
- JUnit 5 + Mockito

## 测试位置

```
src/backend/src/test/java/com/bluenet/web/
├── api/            # 控制器测试
├── application/    # 应用服务测试
├── domain/         # 领域服务测试
└── infrastructure/ # 基础设施测试
```

## 常用命令

```bash
./mvnw test                           # 运行所有测试
./mvnw test -Dtest=XxxTest            # 运行特定测试
./mvnw test -Dtest=*IntegrationTest   # 集成测试
```

## 测试原则

- AAA 模式：Arrange → Act → Assert
- 单一职责：每个测试验证一个行为
- 命名：`shouldXxxWhenYyy`

## 覆盖率要求

核心业务 ≥ 80%，领域服务 ≥ 70%，应用服务 ≥ 60%
