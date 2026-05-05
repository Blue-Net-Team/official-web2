## Context

当前系统已有 `tb_qrcode` 表，支持咨询群（CONSULTATION）和考核群（ASSESSMENT）两种类型。咨询群二维码的公开获取 API 已实现，但管理后台 UI 缺失。考核群二维码的管理功能完全缺失。

数据库字段已预留：
- `epoch` - 考核轮次（仅 ASSESSMENT）
- `direction` - 方向（仅 ASSESSMENT）
- `is_shared` - 是否三方向共用（仅 ASSESSMENT）

## Goals / Non-Goals

**Goals:**
- 实现管理后台二维码管理页面
- 实现咨询群二维码的完整管理（列表、上传、编辑、删除）
- 实现考核群二维码的完整管理（列表、上传、编辑、删除）
- 支持按方向、轮次筛选考核群二维码

**Non-Goals:**
- 考核群二维码自动发送邮件功能（后续迭代）
- 考核群二维码权限控制（方向管理员只能管理自己方向）（后续迭代）

## Decisions

### 1. API 设计

**咨询群二维码管理接口：**
- `GET /api/v1/admin/qrcodes/consultation` - 获取咨询群二维码列表
- `POST /api/v1/admin/qrcodes/consultation` - 上传咨询群二维码（已实现）
- `PUT /api/v1/admin/qrcodes/consultation/{id}` - 更新咨询群二维码
- `DELETE /api/v1/admin/qrcodes/consultation/{id}` - 删除咨询群二维码（已实现）

**考核群二维码管理接口：**
- `GET /api/v1/admin/qrcodes/assessment` - 获取考核群二维码列表（支持 direction、epoch 筛选）
- `POST /api/v1/admin/qrcodes/assessment` - 上传考核群二维码
- `PUT /api/v1/admin/qrcodes/assessment/{id}` - 更新考核群二维码
- `DELETE /api/v1/admin/qrcodes/assessment/{id}` - 删除考核群二维码

### 2. 前端页面设计

**页面路径：** `/admin/qrcode`

**页面结构：**

```mermaid
graph TB
    subgraph 二维码管理页面
        TAB[Tab 切换]
        
        TAB --> T1[咨询群二维码]
        TAB --> T2[考核群二维码]
        
        subgraph 咨询群Tab
            T1 --> L1[列表: ID | 预览 | 操作]
            L1 --> B1[+ 上传二维码]
        end
        
        subgraph 考核群Tab
            T2 --> F[筛选: 方向 | 轮次<br/>可选，不筛选显示全部]
            F --> L2[列表: ID | 方向 | 轮次 | 共用 | 预览 | 操作]
            L2 --> B2[+ 上传二维码]
        end
    end
```

**咨询群二维码列表：**

| ID | 预览 | 操作 |
|----|------|-----|
| 1 | 🖼️ | [编辑] [删除] |
| 2 | 🖼️ | [编辑] [删除] |

**考核群二维码列表：**

| ID | 方向 | 轮次 | 共用 | 预览 | 操作 |
|----|-----|------|-----|------|-----|
| 1 | 计算机视觉 | 1 | - | 🖼️ | [编辑] [删除] |
| 2 | 结构设计 | 1 | - | 🖼️ | [编辑] [删除] |
| 3 | 嵌入式 | 1 | - | 🖼️ | [编辑] [删除] |
| 7 | - | 3 | ✓ | 🖼️ | [编辑] [删除] |

### 3. 编辑功能设计

**咨询群编辑 Drawer：**
- 上传新图片（替换原图片）

**考核群编辑 Drawer：**
- 方向：下拉选择（计算机视觉/结构设计/嵌入式）
- 轮次：数字输入
- 三方向共用：开关（开启时方向字段置空）
- 上传新图片

### 4. DTO 设计

**AssessmentQrcodeDTO：**
```java
public class AssessmentQrcodeDTO {
    private Long id;
    private Long fileId;
    private Direction direction;  // 可为空（共用时）
    private Integer epoch;
    private Boolean isShared;
}
```

**CreateAssessmentQrcodeRequestDTO：**
```java
public class CreateAssessmentQrcodeRequestDTO {
    @NotNull
    private Long fileId;
    private Direction direction;  // 可为空
    @NotNull
    private Integer epoch;
    private Boolean isShared;  // 默认 false
}
```

**UpdateAssessmentQrcodeRequestDTO：**
```java
public class UpdateAssessmentQrcodeRequestDTO {
    private Long fileId;  // 更新图片
    private Direction direction;
    private Integer epoch;
    private Boolean isShared;
}
```

### 5. 业务规则

**考核群二维码上传规则：**
- `isShared = true` 时，`direction` 必须为空
- `isShared = false` 时，`direction` 必须有值
- 同一方向同一轮次只能有一个二维码（除非共用）

**权限控制：**
- 所有管理接口需要 `DIRECTION_ADMIN` 或更高权限
- 后续可扩展方向管理员只能管理自己方向的二维码

## Risks / Trade-offs

### 风险：考核群二维码数量可能较多
→ **缓解**：支持分页和筛选，按方向、轮次过滤

### 风险：编辑时更换图片需要删除旧文件
→ **缓解**：后端处理文件更新时自动清理旧文件

### 风险：共用二维码的 direction 为空可能引起混淆
→ **缓解**：前端展示时明确标记"共用"，后端通过 `isShared` 字段区分

## Migration Plan

1. **后端开发**：先实现 API 接口
2. **前端开发**：基于 API 实现管理页面
3. **测试验证**：手动测试所有 CRUD 操作

**回滚策略：**
- 新增接口和页面，不影响现有功能
- 可直接删除新增代码回滚
