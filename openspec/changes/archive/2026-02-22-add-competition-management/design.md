## Context

项目采用DDD四层架构，需要新增竞赛管理功能。前端需要两种不同粒度的接口：
1. 简要信息接口：用于首页展示，返回前N个竞赛的logo、名称、简称、简介
2. 详细信息接口：返回单个竞赛的完整信息，包括相关照片

现有基础设施：
- MinIO文件存储，通过FileType枚举管理存储桶
- MyBatis-Plus ORM，使用Flyway进行数据库迁移
- @RequiresPermission注解控制权限，支持PUBLIC、AUTHENTICATED、角色级别访问
- **`ImageType.COMPETITION` 已存在**，用于 `tb_introduce_image` 表的竞赛图片分类

## Goals / Non-Goals

**Goals:**
- 新增竞赛表存储竞赛基本信息
- 复用 `tb_introduce_image` 表存储竞赛照片，利用已存在的 `ImageType.COMPETITION`
- 提供公开接口获取竞赛列表和详情
- 提供管理接口进行CRUD操作和排序管理

**Non-Goals:**
- 不支持竞赛分类或标签功能
- 不支持竞赛时间线或历史记录
- 不支持多语言
- 不新增 FileType 枚举值（复用 NORMAL_IMG）

## Decisions

### 1. 数据库设计

**决策：** 创建 `tb_competition` 表，复用 `tb_introduce_image` 表存储竞赛照片

**理由：**
- `ImageType.COMPETITION` 已存在，表示系统设计时就考虑了竞赛介绍图片
- 复用现有表结构，保持设计一致性
- 减少代码和表结构冗余

**表结构：**

```sql
-- 竞赛表
CREATE TABLE tb_competition (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,           -- 竞赛名称
    short_name VARCHAR(50),               -- 竞赛简称
    logo_file_id BIGINT,                  -- logo文件ID（正方形），关联 tb_file.id
    summary VARCHAR(500),                 -- 竞赛简介（简短）
    detail TEXT,                          -- 竞赛详细介绍
    sort_order INTEGER DEFAULT 0,         -- 排序权重（越大越靠前）
    enabled BOOLEAN DEFAULT TRUE,         -- 是否启用
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 修改 tb_introduce_image 表，添加竞赛关联字段
ALTER TABLE tb_introduce_image
ADD COLUMN competition_id BIGINT,         -- 竞赛ID，仅在 type=competition 时有效
ADD COLUMN sort_order INTEGER DEFAULT 0;  -- 排序权重

-- 索引
CREATE INDEX idx_competition_enabled_sort ON tb_competition(enabled, sort_order DESC);
CREATE INDEX idx_introduce_image_competition ON tb_introduce_image(competition_id);
```

**备选方案：** 新建 `tb_competition_image` 表
- 优点：竞赛图片独立管理
- 缺点：与 `ImageType.COMPETITION` 存在语义重复，需要维护两套图片管理逻辑

### 2. 文件类型设计

**决策：** 不新增 FileType 枚举值，竞赛 Logo 和照片都使用 `FileType.NORMAL_IMG`

**理由：**
- FileType 直接映射 MinIO bucket，过多 bucket 增加运维复杂度
- 竞赛图片与普通介绍图片在存储层面没有本质区别
- 访问控制通过业务层实现，不需要存储层隔离
- Logo 和照片通过 `tb_file` 表的记录关联，业务层区分用途

### 3. API设计

**决策：** 分离公开接口和管理接口到不同的Controller

**公开接口（/api/v1/competitions）：**
- `GET /api/v1/competitions?limit=N` - 获取前N个竞赛简要信息
- `GET /api/v1/competitions/{id}` - 获取单个竞赛详细信息

**管理接口（/api/v1/admin/competitions）：**
- `POST /api/v1/admin/competitions` - 创建竞赛
- `PUT /api/v1/admin/competitions/{id}` - 更新竞赛
- `DELETE /api/v1/admin/competitions/{id}` - 删除竞赛
- `PUT /api/v1/admin/competitions/{id}/sort` - 调整排序
- `POST /api/v1/admin/competitions/{id}/images` - 添加照片
- `DELETE /api/v1/admin/competitions/{id}/images/{imageId}` - 删除照片

**理由：** 遵循现有项目结构，公开接口和管理接口分离更清晰

### 4. 架构设计

**决策：** 应用层协调两个领域服务

```
CompetitionService (应用层)
    │
    ├── CompetitionDomainService (竞赛基本信息)
    │       └── CompetitionRepository
    │
    └── IntroduceImageDomainService (竞赛照片，复用现有服务)
            └── IntroduceImageRepository
                    └── findByTypeAndCompetitionId(ImageType.COMPETITION, competitionId)
```

**理由：**
- 遵循 DDD 规范：领域层服务不应相互调用
- 复用现有基础设施，减少代码冗余

### 5. 权限设计

**决策：**
- 公开接口：`AccessLevel.PUBLIC`
- 管理接口：需要管理员权限

**权限值：**
- `competition:list` - 获取竞赛列表
- `competition:detail` - 获取竞赛详情
- `competition:create` - 创建竞赛
- `competition:update` - 更新竞赛
- `competition:delete` - 删除竞赛
- `competition:sort` - 调整排序

## Risks / Trade-offs

**风险1：照片数量过多影响性能**
→ 限制每个竞赛最多关联20张照片，前端分页加载

**风险2：排序权重冲突**
→ 使用时间戳作为辅助排序，确保稳定性

**风险3：删除竞赛时照片文件残留**
→ 使用事务确保数据一致性，照片文件保留（可复用）

**风险4：tb_introduce_image 表结构变更**
→ 通过 Flyway 迁移脚本添加字段，不影响现有数据

## Migration Plan

1. 执行 Flyway 迁移脚本：
   - 创建 `tb_competition` 表
   - 为 `tb_introduce_image` 添加 `competition_id` 和 `sort_order` 字段
2. 扩展 `IntroduceImageRepository` 添加按竞赛ID查询方法
3. 部署新代码
4. 无需回滚数据（新功能，无历史数据）

## Open Questions

无
