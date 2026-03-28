## Why

IntroduceImage 表目前缺少与 File 表的关联字段，导致无法通过介绍图片 ID 获取对应的文件 ID，进而无法下载介绍图片文件。前端需要获取多种类型的介绍图片列表（实验室、竞赛、方向等），用于官网展示。这限制了官网介绍功能的完整性。

## What Changes

- 在 IntroduceImage 实体中添加 `fileId` 字段，用于关联 File 表
- 添加数据库迁移脚本，为 `tb_introduce_image` 表添加 `file_id` 列
- 创建 IntroduceImageVO、IntroduceImageRepository、IntroduceImageDomainService
- 创建 IntroduceImageDTO 和 IntroduceImageController，提供获取介绍图片列表的接口
- 实现按类型（type）查询介绍图片列表的功能，支持所有类型：laboratory, equipment, team_photo, direction, competition, patent, paper
- 当 type=direction 时，支持按方向（Direction）枚举值过滤（计算机视觉、结构设计、嵌入式开发）
- 前端通过 UserVO 中的 url 字段拼接接口前缀获取头像和二维码
- 考核相关文件下载暂不在本次变更范围内

## Capabilities

### New Capabilities
- `introduce-image-management`: 介绍图片管理能力，包括按类型查询介绍图片列表，支持多种类型和方向过滤

### Modified Capabilities
- `file-storage`: 文件存储能力需要支持介绍图片类型的文件关联

## Impact

- 数据库：`tb_introduce_image` 表需要添加 `file_id` 外键列
- 领域层：新增 IntroduceImageVO、IntroduceImageRepository 接口及实现、IntroduceImageDomainService
- 应用层：新增 IntroduceImageService 接口及实现
- 控制层：新增 IntroduceImageController 提供介绍图片相关 REST 接口
- API：新增 `/api/v1/introduce-images` 接口，支持按类型和方向查询介绍图片列表
- UserVO：确保 url 字段可用于前端拼接获取头像和二维码
