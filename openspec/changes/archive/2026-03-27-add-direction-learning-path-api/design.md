## Context

### 当前状态
- 前端 `data.ts` 硬编码了三个方向（cv/embed/struct）的学习路径数据
- 每个方向包含 4 个学习步骤，每个步骤有 `videoLink` 字段（当前为空）
- 后端没有方向学习路径相关的 API 和数据表

### 约束条件
- 遵循 DDD 四层架构规范
- 使用现有 `Direction` 枚举：`COMPUTER_VISION`、`EMBEDDED`、`STRUCTURAL_DESIGN`
- 前端 slug（cv/embed/struct）与后端枚举需要映射

## Goals / Non-Goals

**Goals:**
- 创建独立的数据表存储学习路径步骤
- 提供公开 API 供前端服务端组件获取数据
- 提供管理 API 支持后台维护
- 初始化默认学习路径数据

**Non-Goals:**
- 不实现管理后台前端界面（后续迭代）
- 不支持视频上传（使用外部链接）
- 不实现学习进度跟踪功能

## Decisions

### 1. 数据表设计

**决策**：创建独立的 `tb_direction_learning_step` 表

**理由**：
- 符合单一职责原则，学习路径与介绍图片职责不同
- 便于后续功能扩展（如学习资料、考核关联等）
- 支持独立权限控制

**表结构**：
```sql
CREATE TABLE tb_direction_learning_step (
    id BIGSERIAL PRIMARY KEY,
    direction VARCHAR(50) NOT NULL,
    step_number INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    video_url VARCHAR(500),
    CONSTRAINT uk_direction_step UNIQUE (direction, step_number)
);
```

### 2. API 路径设计

**决策**：使用 `/api/v1/directions/{slug}/learning-path` 作为公开接口

**理由**：
- RESTful 风格，资源层级清晰
- 使用 slug（cv/embed/struct）而非枚举值，与前端 URL 保持一致
- 后端内部进行 slug 到枚举的转换

### 3. 权限设计

**决策**：公开接口无需认证，管理接口需要管理员权限

**权限定义**：
- `direction-learning-path:view` - 公开访问
- `direction-learning-path:create` - 管理员
- `direction-learning-path:update` - 管理员
- `direction-learning-path:delete` - 管理员

## Risks / Trade-offs

**[风险] 方向标识映射错误**
- 前端 slug 与后端枚举不一致可能导致数据查询失败
- 缓解措施：创建 `DirectionSlugConverter` 工具类，统一处理映射逻辑，并编写单元测试验证

**[风险] 步骤序号冲突**
- 同一方向内步骤序号重复
- 缓解措施：数据库唯一约束 + 业务层校验

**[风险] 视频链接格式不合法**
- 用户输入无效 URL
- 缓解措施：后端添加 URL 格式校验注解

## Migration Plan

### 部署步骤
1. 执行数据库迁移脚本 `V15__add_direction_learning_step.sql`
2. 部署后端代码
3. 验证 API 接口可用性
4. 前端对接并部署

### 回滚策略
1. 删除新增的权限记录
2. 删除 `tb_direction_learning_step` 表
3. 回滚后端代码
