---
alwaysApply: false
description: 设计或修改 REST API 接口时使用
---

# API 设计规范

## URL 设计

- 小写 + 连字符：`/api/v1/enrollments`
- 资源复数：`/api/v1/colleges`
- 路径参数标识资源：`/api/v1/users/{id}`

## HTTP 方法

| 方法   | 用途     |
|--------|----------|
| GET    | 获取资源 |
| POST   | 创建资源 |
| PUT    | 完整更新 |
| PATCH  | 部分更新 |
| DELETE | 删除资源 |

## 响应格式

所有接口必须用 `ResponseMessage<T>` 包装：
- 单资源：`ResponseMessage<EnrollmentDTO>`
- 列表：`ResponseMessage<List<EnrollmentDTO>>`
- 分页：`ResponseMessage<PageDTO<EnrollmentDTO>>`

### 分页接口规范

**必须使用 `PageDTO<T>` 而非 Spring Data 的 `Page<T>`**

```java
// ✅ 正确
public ResponseMessage<PageDTO<AchievementDTO>> getAchievements(...) {
    PageDTO<AchievementDTO> result = achievementService.getAchievements(...);
    return ResponseMessage.success(result);
}

// ❌ 错误 - Spring Data Page 序列化格式与前端不兼容
public ResponseMessage<Page<AchievementDTO>> getAchievements(...) {
    Page<AchievementDTO> result = achievementService.getAchievements(...);
    return ResponseMessage.success(result);
}
```

**Service 层实现示例**：

```java
@Override
public PageDTO<AchievementDTO> getAchievements(Integer page, Integer size, ...) {
    Pageable pageable = PageRequest.of(page, size);
    Page<AchievementVO> voPage = repository.findWithFilter(..., pageable);
    Page<AchievementDTO> dtoPage = voPage.map(converter::convertToDTO);
    return PageDTO.from(dtoPage);  // 关键：转换为 PageDTO
}
```

**原因**：前端 `PageDTO<T>` 接口定义与后端 `PageDTO.java` 对应，使用 Spring Data 的 `Page<T>` 会导致字段名不匹配。

## 权限注解（必须）

```java
@RequiresPermission(value = "enrollment:approve", name = "审核报名", access = AccessLevel.PROTECTED)
```

访问级别：`PUBLIC` / `AUTHENTICATED` / `PROTECTED`

## API 文档

DTO 和接口必须添加 `@Schema`、`@Operation` 注解。
