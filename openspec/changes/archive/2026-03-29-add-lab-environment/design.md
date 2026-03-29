## Context

### 当前状态
- `IntroduceImage` 表用于存储介绍图片，但仅有 `type`、`description`、`file_id` 字段
- `ImageType` 枚举包含 `LABORATORY`、`EQUIPMENT`、`COMPETITION` 等类型
- 设计稿要求场地卡片有标题、副标题、描述三个文本字段
- 设计稿要求设备卡片有标题、品牌、描述三个文本字段

### 约束
- 必须遵循 DDD 四层架构
- 所有 API 需要权限控制（公开接口使用 `AccessLevel.PUBLIC`）
- 图片存储使用 MinIO，通过 `FileService` 上传
- 数据库迁移使用 Flyway

## Goals / Non-Goals

**Goals:**
- 创建独立的 `tb_venue` 和 `tb_equipment` 表存储结构化数据
- 实现完整的 CRUD 管理接口
- 实现公开的列表查询接口
- 前端实现 `/lab-environment` 页面

**Non-Goals:**
- 不实现场地/设备的分类或标签功能
- 不实现批量操作
- 不实现图片裁剪或编辑功能
- 不处理现有 `IntroduceImage` 中 `LABORATORY`/`EQUIPMENT` 类型数据的迁移（手动处理）

## Decisions

### Decision 1: 独立表设计 vs 扩展 IntroduceImage

**选择**: 创建独立的 `tb_venue` 和 `tb_equipment` 表

**理由**:
- 场地和设备的字段结构不同（副标题 vs 品牌）
- 独立表提供更好的类型安全和查询性能
- 语义更清晰，便于后续扩展
- 避免在 `IntroduceImage` 表中添加大量可空字段

**备选方案**: 在 `IntroduceImage` 表添加 `title`、`subtitle`、`brand` 等字段
- 优点: 不需要新表
- 缺点: 字段语义混乱（brand 对场地无意义），查询需要额外过滤

### Decision 2: 数据模型设计

**场地表 (`tb_venue`)**:
```sql
CREATE TABLE tb_venue (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,           -- 标题：如"办公区域"
    subtitle VARCHAR(100),                -- 副标题：如"团队协作空间"
    description TEXT,                     -- 描述
    image_file_id BIGINT,                 -- 图片文件ID
    sort_order INTEGER DEFAULT 0          -- 排序权重
);
```

**设备表 (`tb_equipment`)**:
```sql
CREATE TABLE tb_equipment (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,           -- 标题：如"3D打印机"
    brand VARCHAR(100),                   -- 品牌：如"泰尔时代"
    description TEXT,                     -- 描述
    image_file_id BIGINT,                 -- 图片文件ID
    sort_order INTEGER DEFAULT 0          -- 排序权重
);
```

### Decision 3: API 设计

**公开接口**（无需认证）:
- `GET /api/v1/venues` - 获取场地列表，按 sort_order DESC 排序
- `GET /api/v1/equipments` - 获取设备列表，按 sort_order DESC 排序

**管理接口**（需要管理员权限）:
- `POST /api/v1/admin/venues` - 创建场地
- `PUT /api/v1/admin/venues/{id}` - 更新场地
- `DELETE /api/v1/admin/venues/{id}` - 删除场地
- `POST /api/v1/admin/venues/{id}/image` - 上传场地图片
- `POST /api/v1/admin/equipments` - 创建设备
- `PUT /api/v1/admin/equipments/{id}` - 更新设备
- `DELETE /api/v1/admin/equipments/{id}` - 删除设备
- `POST /api/v1/admin/equipments/{id}/image` - 上传设备图片

### Decision 4: ImageType 枚举变更

**选择**: 删除 `LABORATORY` 和 `EQUIPMENT` 枚举值

**理由**:
- 这两个类型现在由独立的场地和设备表处理
- 保留会造成混淆
- `COMPETITION` 保留，因为竞赛图片仍使用 `IntroduceImage` 表

**影响**:
- 需要更新 `ImageType` 枚举类
- 如有现有数据需要手动迁移

### Decision 5: DDD 分层结构

遵循现有项目架构:

```
domain/
├── model/
│   ├── entity/
│   │   ├── Venue.java
│   │   └── Equipment.java
│   ├── vo/
│   │   ├── VenueVO.java
│   │   └── EquipmentVO.java
│   └── enumerate/
│       └── ImageType.java (修改)
├── repository/
│   ├── VenueRepository.java
│   └── EquipmentRepository.java
└── service/
    ├── VenueDomainService.java
    ├── EquipmentDomainService.java
    └── impl/

application/
├── service/
│   ├── VenueService.java
│   ├── EquipmentService.java
│   └── impl/
└── converter/
    ├── VenueConverter.java
    └── EquipmentConverter.java

infrastructure/
├── repository/
│   ├── impl/
│   └── mapper/
│       ├── VenueMapper.java
│       └── EquipmentMapper.java

api/
├── controller/v1/
│   ├── VenueController.java (公开)
│   ├── EquipmentController.java (公开)
│   └── admin/
│       ├── AdminVenueController.java
│       └── AdminEquipmentController.java
└── dto/
    ├── venue/
    └── equipment/
```

## Risks / Trade-offs

### Risk 1: 现有数据丢失
- **风险**: 如果 `IntroduceImage` 表中已有 `LABORATORY`/`EQUIPMENT` 类型的数据，删除枚举值会导致问题
- **缓解**: 迁移脚本只创建新表，不删除现有数据；管理员手动迁移数据后再清理枚举

### Risk 2: 图片上传流程复杂
- **风险**: 管理员需要先上传图片获取 file_id，再创建场地/设备
- **缓解**: 提供两种方式：1) 分步上传；2) 在创建接口中直接接收 MultipartFile

## Migration Plan

1. **部署阶段 1**: 执行 Flyway 迁移，创建 `tb_venue` 和 `tb_equipment` 表
2. **部署阶段 2**: 部署后端代码（新增实体、服务、控制器）
3. **部署阶段 3**: 部署前端页面
4. **数据迁移**: 管理员通过管理平台手动添加场地和设备数据
5. **清理阶段**: 确认无问题后，移除 `ImageType` 中的 `LABORATORY` 和 `EQUIPMENT`

## Open Questions

- 是否需要实现图片上传和创建实体的原子操作？（当前设计为分开的 API）
- 场地和设备的排序是否需要支持拖拽调整？（当前使用 sort_order 字段手动设置）
