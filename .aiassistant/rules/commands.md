---
apply: 按模型决策
指令: 查看常用命令时手动引用
---

# 常用命令

## 后端

```bash
cd src/backend

./mvnw spring-boot:run                    # 开发环境运行
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod  # 生产环境
./mvnw clean package                      # 构建
./mvnw spotless:apply                     # 代码格式化
./mvnw test                               # 运行测试
./mvnw flyway:info                        # 迁移状态
```

## 前端

```bash
cd src/frontend

pnpm install     # 安装依赖
pnpm dev         # 开发运行
pnpm build       # 构建
pnpm lint        # 代码检查
```

## Git 提交规范

`feat:` 新功能 | `fix:` 修复 | `docs:` 文档 | `refactor:` 重构 | `test:` 测试

## 引用方式

```
#commands 我想运行后端测试
```
