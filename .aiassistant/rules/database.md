---
apply: 按模型决策
模式: 涉及数据库设计、表结构、Flyway 迁移时使用
指令: 涉及数据库设计、表结构、Flyway 迁移时使用
---

# 数据库设计规范

## 核心约束

- 表以 `tb_` 开头，含 `deleted` 软删除字段
- 不使用物理外键，应用层维护关系
- 使用 Flyway 10.x 迁移

## 通用字段

```sql
id BIGSERIAL PRIMARY KEY,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
deleted BOOLEAN NOT NULL DEFAULT FALSE
```

## 核心表

| 表 | 说明 |
|----|------|
| `tb_user` | 用户（学号唯一） |
| `tb_enroll` | 报名 |
| `tb_assessment_*` | 考核相关 |
| `tb_file` | 文件元信息 |
| `tb_college` | 学院 |
| `tb_competition` | 竞赛 |

## Flyway 迁移

文件命名：`V{version}__{description}.sql`
位置：`src/backend/src/main/resources/db/migration/`

```bash
./mvnw flyway:info     # 检查状态
./mvnw flyway:migrate  # 执行迁移
```
