## Context

当前 IntroduceImage 实体仅包含 `id`、`type` 和 `description` 字段，缺少与 File 表的关联。这导致无法通过介绍图片 ID 获取对应的文件 ID，进而无法下载介绍图片文件。官网功能需要展示各类介绍图片（实验室、设备、团队、方向、竞赛、专利、论文），前端需要根据类型获取对应的介绍图片列表。当类型为方向时，还需要按方向（Direction）枚举值过滤。同时，前端需要通过 UserVO 中的 url 字段拼接接口前缀获取头像和二维码。

项目采用 DDD 四层架构：
- 控制层：处理 HTTP 请求，使用 DTO
- 应用层：调用领域服务，将 VO 转换为 DTO
- 领域层：仅操作 VO，通过仓库接口访问数据
- 仓库层：调用 MyBatis-Plus，负责 VO 与 Entity 转换

## Goals / Non-Goals

**Goals:**
- 为 IntroduceImage 实体添加 `fileId` 字段，建立与 File 表的关联
- 提供按类型（type）查询介绍图片列表的功能，支持所有类型：laboratory, equipment, team_photo, direction, competition, patent, paper
- 当 type=direction 时，支持按方向（Direction）枚举值过滤（计算机视觉、结构设计、嵌入式开发）
- 确保 UserVO 中的 url 字段可用于前端拼接获取头像和二维码
- 遵循 DDD 四层架构规范，确保各层职责清晰

**Non-Goals:**
- 不修改现有的文件上传和下载接口
- 不涉及介绍图片的创建和删除功能（本次变更仅支持查询）
- 不涉及考核相关文件下载
- 不涉及介绍图片的批量操作功能
- 不涉及介绍图片的排序和分页优化（使用 MyBatis-Plus 默认分页）

## Decisions

### 1. 数据模型变更
**决策：** 在 `tb_introduce_image` 表添加 `file_id` 列，作为外键关联 `tb_file.id`

**理由：**
- 保持数据一致性，确保每张介绍图片都有对应的文件记录
- 符合关系型数据库设计规范

**替代方案考虑：**
- 在 File 表添加 `introduce_image_id` 列：不合适，因为 File 表是通用的，不应包含特定业务字段
- 使用中间关联表：过度设计，当前是一对一关系，直接外键即可

### 2. 领域层设计
**决策：** 创建 IntroduceImageVO、IntroduceImageRepository 接口、IntroduceImageDomainService

**理由：**
- 遵循 DDD 规范，领域层只操作 VO，不感知 Entity 和 DTO
- Repository 接口定义领域层的数据访问契约，由仓库层实现
- DomainService 封装领域业务逻辑，提供高层次的领域操作

**替代方案考虑：**
- 直接在 Controller 调用 Mapper：违反 DDD 架构规范
- 在 DomainService 中直接操作 Entity：违反领域层不应感知 Entity 的原则

### 3. API 设计
**决策：** 使用单一接口 + 参数过滤的方案

**理由：**
- 符合 RESTful 设计原则，使用查询参数进行过滤是标准做法
- 接口统一易维护，一个接口处理所有类型，减少代码重复
- 扩展性好，未来新增类型无需新增接口
- 前端调用便利，统一的接口调用方式

**接口设计：**
```
GET /api/v1/introduce-images
```

**查询参数：**
- `type`（必填）：图片类型，可选值：laboratory, equipment, team_photo, direction, competition, patent, paper
- `direction`（可选）：方向枚举值，仅在 type=direction 时有效，可选值：COMPUTER_VISION, STRUCTURAL_DESIGN, EMBEDDED

**使用示例：**
```bash
# 获取实验室介绍图片
GET /api/v1/introduce-images?type=laboratory

# 获取竞赛介绍图片
GET /api/v1/introduce-images?type=competition

# 获取计算机视觉方向介绍图片
GET /api/v1/introduce-images?type=direction&direction=COMPUTER_VISION
```

**响应格式：**
```json
{
  "code": 200,
  "msg": "Success",
  "data": [
    {
      "id": 1,
      "type": "laboratory",
      "description": "实验室环境展示",
      "fileId": 100,
      "fileUrl": "https://minio.example.com/bluenet/xxx.jpg"
    }
  ]
}
```

**UserVO url 字段说明：**
- avatarUrl：头像文件完整 URL，前端可直接使用
- wechatQrCodeUrl：微信二维码完整 URL，前端可直接使用
- 这些字段已在 UserRepositoryImpl 中通过关联查询 File 表获取

### 4. 数据库查询优化
**决策：** 在 Mapper 中使用自定义 SQL 进行关联查询

**理由：**
- 避免多次查询，提高性能
- 一次查询获取 IntroduceImage 和关联的 File 信息
- 使用 MyBatis 的 resultMap 进行结果映射

**SQL 示例：**
```xml
<select id="selectByTypeAndDirection" resultMap="IntroduceImageWithFileResultMap">
    SELECT
        ii.id,
        ii.type,
        ii.description,
        f.id as file_id,
        f.url as file_url
    FROM tb_introduce_image ii
    LEFT JOIN tb_file f ON ii.file_id = f.id
    WHERE ii.type = #{type}
    <if test="type == 'DIRECTION' and direction != null">
        AND ii.description = #{directionDescription}
    </if>
</select>
```

## Risks / Trade-offs

### 风险 1：数据迁移风险
**风险：** 现有数据库中可能已有 IntroduceImage 记录，但没有对应的 File ID

**缓解措施：**
- 数据库迁移脚本将 `file_id` 列设置为可空
- 对于现有记录，`file_id` 为 NULL，需要后续手动补充或提供数据修复脚本

### 风险 2：权限控制风险
**风险：** 介绍图片文件类型为 `normal_img`，根据现有规范应该是公开可见的

**缓解措施：**
- 确保文件上传时使用正确的文件类型（`FileType.NORMAL_IMG`）
- 在文件下载接口中正确应用访问控制规则

### 权衡 1：性能 vs 简洁性
**权衡：** 查询介绍图片列表时，是否需要级联查询文件信息

**决策：** 列表查询返回 IntroduceImage 信息，同时包含文件 URL

**理由：**
- 前端需要直接使用图片 URL 进行展示，避免额外的请求
- 列表查询返回的记录数量有限，性能影响可控
- 使用 LEFT JOIN 一次查询完成，性能优化良好

## Migration Plan

### 部署步骤

1. **数据库迁移**
   - 执行 Flyway 迁移脚本，添加 `file_id` 列
   - 验证迁移成功，检查表结构

2. **代码部署**
   - 部署后端代码，包含新增的 VO、Repository、Service、Controller
   - 验证 API 接口正常工作

3. **数据修复（可选）**
   - 如果现有数据需要补充 File ID，执行数据修复脚本

### 回滚策略

1. 如果代码部署失败，回滚到上一版本
2. 如果数据库迁移失败，使用 Flyway 的回滚机制或手动删除 `file_id` 列
3. 如果数据修复失败，保持 `file_id` 为 NULL，不影响现有功能

## Open Questions

1. 是否需要提供批量上传介绍图片的功能？
2. 删除介绍图片时，是否需要级联删除关联的 File 记录？
3. 是否需要为介绍图片添加排序字段，支持自定义展示顺序？
4. 是否需要为介绍图片添加状态字段（如启用/禁用）？
