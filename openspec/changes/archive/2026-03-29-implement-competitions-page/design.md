## Context

设计稿位于 `docs/UI/competitions.pen`，包含完整的竞赛列表页面设计：
- 桌面端版本：1440px 宽度，黑色背景 (#000000)
- 移动端版本：375px 宽度，适配小屏幕

页面展示竞赛卡片列表，每个卡片包含竞赛名称、级别标签、举办时间（可为空）、主办单位（可为空）和简介。第一张卡片可使用介绍图片（introduceImage）作为背景。

### 后端现状
- 已有竞赛基础信息：名称、简称、简介、详细介绍、Logo
- 已有竞赛图片功能：支持为竞赛添加多张图片（通过 `tb_competition_image` 表）
- 已有介绍图片功能：支持上传 `ImageType.COMPETITION` 类型图片（通过 `tb_introduce_image` 表）
- 需要扩展字段：`level`（级别）、`month`（月份）、`organizer`（主办单位）

## Goals / Non-Goals

**Goals:**
- 后端：扩展竞赛模块，支持级别、时间（可选）、主办单位（可选）
- 后端：支持通过 `tb_introduce_image` 关联介绍图片（可选）
- 前端：精确还原设计稿视觉效果
- 前端：实现响应式布局，在桌面端和移动端都有良好展示
- 前端：使用 CSS Modules（不使用 Tailwind CSS）
- 前后端：通过接口对接，动态获取竞赛数据

**Non-Goals:**
- 不需要复杂的交互动画
- 不需要服务端渲染（SSG 即可）

## Decisions

### 1. 后端数据库设计

**新增字段：**
```sql
-- 竞赛级别，默认省级
ALTER TABLE tb_competition ADD COLUMN level VARCHAR(20) NOT NULL DEFAULT '省级';

-- 举办月份，可为空
ALTER TABLE tb_competition ADD COLUMN month VARCHAR(10);

-- 主办单位，可为空
ALTER TABLE tb_competition ADD COLUMN organizer VARCHAR(200);
```

**字段说明：**
| 字段名 | 类型 | 可空 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `level` | VARCHAR(20) | NO | '省级' | 竞赛级别，枚举值：'国家级' \| '省级' |
| `month` | VARCHAR(10) | YES | NULL | 举办月份，如 '5月'、'8月'，可为空 |
| `organizer` | VARCHAR(200) | YES | NULL | 主办单位，可为空，前端为空时不显示 |

**字段约束说明：**
- `level`: 使用 CHECK 约束或应用层校验，只允许 '国家级' 或 '省级'
- `month`: 无格式约束，建议格式为 "X月"（如 "5月"）
- `organizer`: 最大长度 200 字符

### 2. 介绍图片（introduceImage）设计方案

**设计原则：**
- 介绍图片是可选的，不上传时卡片使用默认深色背景
- 使用现有的 `tb_introduce_image` 表存储关联关系
- 文件实际存储为 `FileType.NORMAL_IMG` 类型
- 图片类型为 `ImageType.COMPETITION`

**数据结构：**
```
tb_introduce_image
├── id: BIGINT PK
├── type: VARCHAR(50) - 固定值 'competition' (ImageType.COMPETITION)
├── competition_id: BIGINT FK - 关联 tb_competition.id
├── file_id: BIGINT FK - 关联 tb_file.id
├── description: VARCHAR(500) - 图片描述（可选）
├── sort_order: INT - 排序权重，默认 0
├── created_at: TIMESTAMP
└── updated_at: TIMESTAMP
```

**获取介绍图片流程：**
1. 查询 `tb_introduce_image` 表
2. 条件：`type = 'competition' AND competition_id = ?`
3. 按 `sort_order ASC` 排序，取第一条记录
4. 返回该记录的 `file_id` 作为 `introduceImageFileId`
5. 如无记录，返回 `null`

**介绍图片规格：**
- 形状：长方形横图
- 尺寸：无具体限制，推荐使用 1200x600 或类似比例
- 格式：JPG/PNG
- 用途：仅第一张卡片使用作为背景
- 多张处理：暂不支持轮播，只取 sort_order 最小的一张

**上传介绍图片流程：**
1. 使用文件上传接口上传图片，获取 `fileId`
2. 调用 `POST /api/v1/admin/introduce-images` 创建关联
3. 参数：
   - `type`: "competition"
   - `competitionId`: 竞赛ID
   - `fileId`: 文件ID
   - `description`: 可选描述
   - `sortOrder`: 排序权重

### 3. 后端 DTO 变更

**CompetitionBriefDTO 新增字段：**
```java
@Schema(description = "竞赛级别", example = "国家级")
private String level;

@Schema(description = "举办月份", example = "5月")
private String month;

@Schema(description = "主办单位")
private String organizer;

@Schema(description = "介绍图片文件ID，无图片时为null")
private Long introduceImageFileId;
```

**CompetitionDetailDTO 新增字段：**
与 CompetitionBriefDTO 相同，添加以上四个字段。

**CreateCompetitionRequestDTO 新增字段：**
```java
@NotBlank(message = "竞赛级别不能为空")
@Pattern(regexp = "^(国家级|省级)$", message = "竞赛级别只能是国家级或省级")
@Schema(description = "竞赛级别", required = true, example = "省级")
private String level;

@Size(max = 10, message = "举办月份最多10个字符")
@Schema(description = "举办月份", example = "5月")
private String month;

@Size(max = 200, message = "主办单位最多200个字符")
@Schema(description = "主办单位")
private String organizer;

// 注意：介绍图片不通过此 DTO 创建
// 使用 AdminIntroduceImageController 单独上传
```

**UpdateCompetitionRequestDTO 新增字段：**
```java
@Pattern(regexp = "^(国家级|省级)$", message = "竞赛级别只能是国家级或省级")
@Schema(description = "竞赛级别", example = "省级")
private String level;

@Size(max = 10, message = "举办月份最多10个字符")
@Schema(description = "举办月份", example = "5月")
private String month;

@Size(max = 200, message = "主办单位最多200个字符")
@Schema(description = "主办单位")
private String organizer;

// 注意：介绍图片不通过此 DTO 更新
```

### 4. 后端业务逻辑变更

**CompetitionServiceImpl 变更：**

1. **创建竞赛 (`createCompetition`)：**
   - 设置默认 level 为 "省级"（如果未提供）
   - month 和 organizer 可为 null

2. **更新竞赛 (`updateCompetition`)：**
   - 允许更新 level、month、organizer 字段
   - 字段为 null 时表示不更新该字段

3. **获取竞赛列表 (`getCompetitionList`)：**
   - 查询竞赛基础信息
   - 对每个竞赛，关联查询介绍图片（取 sort_order 最小的）
   - 组装 DTO 返回

4. **获取竞赛详情 (`getCompetitionDetail`)：**
   - 查询竞赛详细信息
   - 关联查询介绍图片
   - 组装 DTO 返回

**CompetitionConverter 变更：**
- 添加 level、month、organizer 字段的映射
- 添加 introduceImageFileId 的映射（从 IntroduceImage 查询结果获取）

### 5. 前端技术栈

**样式方案：**
- 使用 CSS Modules（不使用 Tailwind CSS）
- 样式文件命名：`*.module.css`
- 类名使用 camelCase

**组件结构：**
```
src/app/competitions/
├── page.tsx                 # 页面组件（Server Component）
├── page.module.css          # 页面样式
├── components/
│   ├── CompetitionCard.tsx      # 竞赛卡片组件
│   └── CompetitionCard.module.css # 卡片样式
├── types.ts                 # TypeScript 类型定义
└── api.ts                   # API 调用函数
```

**数据获取：**
- 使用 Next.js App Router 的 Server Component
- 直接调用后端 API 获取数据
- 接口：`GET /api/v1/competitions?limit=10`

### 6. 前端类型定义

```typescript
// types.ts
export interface Competition {
  id: number;
  name: string;
  shortName: string;
  summary: string;
  level: '国家级' | '省级';
  month?: string;           // 可选
  organizer?: string;       // 可选
  introduceImageFileId?: number; // 可选，无介绍图片时为 undefined
}
```

### 7. 前端样式规范

**桌面端（>= 768px）：**
| 元素 | 样式 |
|------|------|
| 页面背景 | #000000 |
| 页面内边距 | 80px 垂直 / 147px 水平 |
| 页面标题 | 48px, 白色 (#ffffff), font-weight: 700 |
| 页面副标题 | 20px, #ffffff99 |
| 标题与副标题间距 | 48px |
| 卡片列表间距 | 48px |
| 卡片高度 | 200px |
| 卡片圆角 | 24px |
| 卡片内边距 | 32px 垂直 / 40px 水平 |
| 卡片背景 | #1a1a1a（无介绍图片时）|
| 卡片内部间距 | 12px |
| 竞赛名称 | 28px, 白色, font-weight: 700 |
| 级别标签尺寸 | 80x28px |
| 级别标签圆角 | 14px |
| 级别标签文字 | 12px, 白色, font-weight: 700 |
| 举办时间 | 24px, #ffffff66 |
| 主办单位标签 | 16px, #ffffff99 |
| 主办单位值 | 16px, 白色 |
| 竞赛简介 | 16px, #ffffffcc, line-height: 1.5 |

**移动端（< 768px）：**
| 元素 | 样式 |
|------|------|
| 页面内边距 | 40px 垂直 / 24px 水平 |
| 页面标题 | 28px, 白色, font-weight: 700 |
| 页面副标题 | 14px, #ffffff99 |
| 标题与副标题间距 | 24px |
| 卡片列表间距 | 24px |
| 卡片高度 | 自适应（auto）|
| 卡片圆角 | 20px |
| 卡片内边距 | 24px 垂直 / 20px 水平 |
| 竞赛名称 | 20px, 白色, font-weight: 700 |
| 级别标签尺寸 | 64x24px |
| 级别标签圆角 | 12px |
| 级别标签文字 | 12px, 白色, font-weight: 700 |
| 举办时间 | 隐藏（不显示）|
| 主办单位标签 | 14px, #ffffff99 |
| 主办单位值 | 14px, 白色 |
| 竞赛简介 | 14px, #ffffffcc, line-height: 1.5 |

**颜色规范：**
| 用途 | 颜色值 |
|------|--------|
| 国家级标签背景 | #E86835（橙色）|
| 省级标签背景 | #4A90E2（蓝色）|
| 页面背景 | #000000 |
| 卡片背景（默认）| #1a1a1a |
| 标题文字 | #ffffff |
| 副标题/标签文字 | #ffffff99 |
| 简介文字 | #ffffffcc |
| 举办时间文字 | #ffffff66 |

### 8. 第一张卡片特殊处理

**有介绍图片时：**
- 使用介绍图片作为背景（通过文件下载接口获取 URL）
- 背景图片覆盖整个卡片
- 叠加线性渐变遮罩：
  - 方向：270度（从上到下）
  - 起点（45%位置）：#1a1a1a
  - 终点（100%位置）：#1a1a1aaa（带透明度）
- 遮罩覆盖整个卡片，确保文字可读

**无介绍图片时：**
- 使用默认深色背景 #1a1a1a
- 与其他卡片样式一致

**其他卡片（第2-N张）：**
- 始终使用深色背景 #1a1a1a
- 不使用背景图片

### 9. 字段显示逻辑

**month（举办月份）：**
- 有值时显示
- 为空/null 时不显示举办时间行
- 移动端始终不显示（无论是否有值）

**organizer（主办单位）：**
- 有值时显示："主办单位：{值}"
- 为空/null 时不显示主办单位行
- 桌面端和移动端逻辑一致

**introduceImageFileId（介绍图片）：**
- 仅第一张卡片使用
- 有值时作为背景图片
- 为 null/undefined 时使用默认背景

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 数据库迁移影响现有数据 | level 使用 DEFAULT '省级'，month 和 organizer 可为空，确保兼容性 |
| 现有竞赛数据缺少新字段 | 迁移后需要手动补充或提供默认值 |
| 前端样式不使用 Tailwind 增加工作量 | 使用 CSS Modules 保持样式隔离和可维护性 |
| 介绍图片与现有 Logo 区分不清 | 明确文档说明：Logo 用于列表图标，介绍图片用于卡片背景 |
| 介绍图片查询影响性能 | 使用索引优化查询，或考虑缓存 |

## Migration Plan

### 后端迁移步骤

1. **数据库迁移**
   - 创建 Flyway 脚本 `V18__add_competition_fields.sql`
   - 添加 level、month、organizer 字段

2. **代码更新**
   - 更新 Competition 实体类
   - 更新 CompetitionBriefDTO、CompetitionDetailDTO
   - 更新 CreateCompetitionRequestDTO、UpdateCompetitionRequestDTO
   - 更新 CompetitionConverter
   - 更新 CompetitionServiceImpl（添加介绍图片查询逻辑）

3. **数据补充**
   - 为现有竞赛数据补充 level 字段（默认省级）
   - 根据业务需要补充 month 和 organizer

4. **测试验证**
   - 运行单元测试
   - 运行集成测试
   - 验证 API 响应格式

### 前端迁移步骤

1. **项目结构**
   - 创建 `src/app/competitions/` 目录结构
   - 创建类型定义文件
   - 创建 API 调用文件

2. **样式实现**
   - 实现 page.module.css
   - 实现 CompetitionCard.module.css
   - 确保响应式适配

3. **组件实现**
   - 实现 CompetitionCard 组件
   - 实现 page.tsx 页面组件
   - 对接后端 API

4. **测试验证**
   - 桌面端视觉走查
   - 移动端视觉走查
   - 功能测试（各种字段组合）

## Open Questions

- ~~是否需要为现有竞赛数据批量补充 month 和 organizer 字段？~~ **已确认：不需要**
- ~~介绍图片是否有推荐尺寸（如 1200x400）？~~ **已确认：长方形横图即可**
- ~~是否需要支持多张介绍图片轮播？~~ **已确认：暂不支持，只取第一张**

## 已确认的设计决策

1. **现有数据**：不需要为现有竞赛数据补充 month 和 organizer 字段
2. **介绍图片尺寸**：推荐长方形横图，无具体尺寸限制
3. **介绍图片数量**：暂不支持轮播，只取 sort_order 最小的一张作为背景
