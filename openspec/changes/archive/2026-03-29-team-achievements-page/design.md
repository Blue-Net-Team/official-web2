## Context

当前系统已有 `tb_achievement` 表和 `tb_competition` 表：

**现有 `tb_achievement` 表结构**：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | SERIAL | 主键 |
| title | VARCHAR(200) | 成就标题 |
| type | VARCHAR(50) | 类型（paper/patent/competition）|
| relate_to | VARCHAR(200) | 关联信息 |
| achieve_at | INTEGER | 获得年份 |

**`AchievementType` 枚举**：PAPER（论文）、PATENT（专利）、COMPETITION（竞赛）

设计稿展示的是"团队成就"页面，需要展示竞赛获奖信息，包括竞赛 Logo、获奖等级、获奖名称、获奖人数等。现有表结构缺少这些字段，需要扩展。

## Goals / Non-Goals

**Goals:**
- 扩展现有 `tb_achievement` 表，添加竞赛获奖所需字段
- 实现成就列表查询 API，支持按年份、奖项级别筛选（仅查询 type=COMPETITION）
- 实现成就统计数据 API
- 实现前端成就展示页面，响应式布局支持桌面端和移动端

**Non-Goals:**
- 成就管理后台（本次仅实现展示功能）
- 成就详情页（设计稿未包含）
- 论文/专利成就展示（本次仅实现竞赛成就）

## Decisions

### 1. 数据模型设计

**决定**: 扩展现有 `tb_achievement` 表，而非新建表

**理由**: 
- 系统已有 `tb_achievement` 表，且 `AchievementType` 已包含 `COMPETITION` 类型
- 扩展字段比新建表更合理，避免数据冗余
- 现有 `tb_user_achievement` 关联表可复用

**修改现有字段**:
- `achieve_at`: 类型从 `INTEGER` 改为 `DATE`，存储完整日期而非仅年份

**新增字段**:
```sql
ALTER TABLE tb_achievement ALTER COLUMN achieve_at TYPE DATE USING to_date(achieve_at::text, 'YYYY');
ALTER TABLE tb_achievement ADD COLUMN competition_id BIGINT;
ALTER TABLE tb_achievement ADD COLUMN award_level VARCHAR(20);
ALTER TABLE tb_achievement ADD COLUMN award_name VARCHAR(50);
ALTER TABLE tb_achievement ADD COLUMN winner_count INTEGER DEFAULT 0;
```

**字段说明**：
| 字段 | 类型 | 说明 |
|------|------|------|
| achieve_at | DATE | 获奖日期（原 INTEGER 改为 DATE）|
| competition_id | BIGINT | 关联竞赛ID（tb_competition.id），仅 type=COMPETITION 时有效 |
| award_level | VARCHAR(20) | 奖项级别：NATIONAL/PROVINCIAL/SCHOOL，仅 type=COMPETITION 时有效 |
| award_name | VARCHAR(50) | 奖项名称：一等奖/二等奖/三等奖，仅 type=COMPETITION 时有效 |
| winner_count | INTEGER | 获奖人数，仅 type=COMPETITION 时有效 |

**实体类修改**:
```java
// 原：private Integer achieveAt;
// 改为：
private LocalDate achieveAt;
```

**新增枚举**：
```java
public enum AwardLevel {
    NATIONAL("national", "国家级"),
    PROVINCIAL("provincial", "省级"),
    SCHOOL("school", "校级");
}
```

### 2. API 设计

**决定**: 采用 RESTful 风格，公开访问

**接口列表**:
- `GET /api/v1/achievements` - 获取成就列表（支持筛选，默认仅返回 type=COMPETITION）
- `GET /api/v1/achievements/stats` - 获取成就统计

### 3. 前端架构

**决定**: 新增独立页面 `/achievements`，复用现有组件风格

**组件结构**:
- `AchievementPage` - 页面容器
- `AchievementCard` - 成就卡片组件
- `AchievementFilter` - 筛选组件
- `AchievementStats` - 统计展示组件

## Risks / Trade-offs

- **数据初始化**: 需要手动录入历史获奖数据 → 提供管理接口或数据库脚本
- **性能**: 成就数据量可能较大 → 添加分页支持，前端虚拟滚动
- **兼容性**: 新增字段对现有论文/专利数据无影响 → 字段仅在 type=COMPETITION 时使用
