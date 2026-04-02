## Context

当前系统已有 `tb_qrcode` 表用于存储二维码信息，但设计简单（仅 id、fileId、type），无法满足：
1. 咨询群动态管理需求（数量可变，需后端管理）
2. 后续考核群扩展需求（按轮次、方向划分，权限控制）

现有 `QrcodeType` 枚举为 `USER/GROUP`，需要扩展以区分咨询群和考核群。

## Goals / Non-Goals

**Goals:**
- 扩展 `tb_qrcode` 表结构，支持咨询群和考核群
- 实现咨询群公开获取接口
- 实现咨询群管理接口（上传、删除）
- 前端 enroll 页面展示咨询群二维码

**Non-Goals:**
- 考核群权限控制逻辑（后续迭代）
- 考核群邮件自动发送（后续迭代）
- 管理后台 UI（本次仅实现 API）

## Decisions

### 1. 单表设计 + 可空字段（推荐）

**方案**：在 `tb_qrcode` 表添加可空字段，通过 `type` 区分类型。

```sql
ALTER TABLE tb_qrcode ADD COLUMN epoch INT;
ALTER TABLE tb_qrcode ADD COLUMN direction VARCHAR(50);
ALTER TABLE tb_qrcode ADD COLUMN is_shared BOOLEAN DEFAULT FALSE;
```

**字段说明**：
- `epoch`：考核轮次（仅 ASSESSMENT 类型使用）
- `direction`：方向（仅 ASSESSMENT 非共用时使用）
- `is_shared`：是否三方向共用（仅 ASSESSMENT 最后一轮使用）

**类型约束**：
- `USER`：epoch/direction/is_shared 均为 NULL
- `CONSULTATION`：epoch/direction/is_shared 均为 NULL
- `ASSESSMENT`：epoch 必填，direction/is_shared 根据实际情况

**理由**：
1. 符合三范式（每个字段都直接依赖于主键）
2. 避免空表问题（咨询群数量可能为 0）
3. 扩展性好，后续迭代无需改表结构
4. 查询简单，单表即可获取所有信息

**替代方案**：分表设计（tb_consultation_qrcode / tb_assessment_qrcode）
- 缺点：咨询群数量少时产生空表，JOIN 查询复杂

### 2. QrcodeType 枚举扩展

```java
public enum QrcodeType {
    USER("user", "用户微信二维码"),
    CONSULTATION("consultation", "咨询群二维码"),
    ASSESSMENT("assessment", "考核群二维码");
}
```

**理由**：
- 保持向后兼容，保留 USER 类型
- 语义清晰，CONSULTATION/ASSESSMENT 职责分明
- 便于后续权限控制（考核群需要鉴权）

### 3. API 设计

**公开接口**：
- `GET /api/v1/qrcodes/consultation` → 返回咨询群列表（含 fileId）

**管理接口**（需 ADMIN 权限）：
- `POST /api/v1/admin/qrcodes/consultation` → 上传咨询群二维码
- `DELETE /api/v1/admin/qrcodes/consultation/{id}` → 删除咨询群二维码

**理由**：
- 公开接口无需认证，便于报名页面调用
- 管理接口独立路径 `/admin/`，便于权限控制

### 4. 前端组件设计

**组件名**：`ConsultationQrcode`

**交互方式**：列表展示 + 悬浮预览
- 列表展示群名（如"咨询群1"、"咨询群2"）
- 鼠标悬浮 item 时右侧弹出二维码

**理由**：
- 不占用过多页面空间
- 交互体验友好
- 支持多个咨询群展示

## Risks / Trade-offs

### 风险：单表设计可能有大量 NULL 值
→ **缓解**：NULL 值不占用额外存储空间（PostgreSQL），且查询时可按 type 过滤

### 风险：前端组件可能影响页面加载性能
→ **缓解**：二维码图片使用 Next.js Image 组件，支持懒加载

### 风险：管理接口缺乏 UI，操作不便
→ **缓解**：后续可接入管理后台，当前使用 API 工具（Postman）管理

## Migration Plan

1. **数据库迁移**：Flyway 脚本添加新字段（可空，无数据迁移）
2. **枚举更新**：修改 QrcodeType，保留 GROUP 作为过渡（标记 @Deprecated）
3. **后端部署**：先部署后端接口
4. **前端部署**：验证接口正常后部署前端组件

**回滚策略**：
- 数据库字段为可空，回滚不影响现有数据
- 枚举回滚需处理新增类型数据（先删除新类型记录）

## Open Questions

1. 咨询群管理接口是否需要批量上传？（当前设计为单个上传）
2. 咨询群是否需要排序字段？（当前按 id 排序）
3. 考核群权限控制是否需要更细粒度（如按轮次授权）？
