## Why

前端首页需要展示竞赛列表，目前没有竞赛管理功能。需要一个动态的竞赛管理系统，支持管理界面修改参与的竞赛、排序重要程度，并提供两种不同粒度的接口供前端使用。

## What Changes

- 新增竞赛表 `tb_competition`，存储竞赛基本信息（logo、名称、简称、简介、详细介绍、排序权重）
- 修改介绍图片表 `tb_introduce_image`，添加 `competition_id` 和 `sort_order` 字段，用于关联竞赛照片
- 复用已存在的 `ImageType.COMPETITION` 枚举值进行竞赛照片分类
- 新增竞赛管理API接口，包括：
  - 公开接口：获取竞赛列表（简要信息）、获取竞赛详情（完整信息）
  - 管理接口：创建竞赛、更新竞赛、删除竞赛、调整排序、照片管理
- 竞赛 Logo 和照片使用 `FileType.NORMAL_IMG` 类型存储（不新增 FileType 枚举值）

## Capabilities

### New Capabilities

- `competition-management`: 竞赛管理功能，包括竞赛的CRUD操作、排序管理、照片关联管理

### Modified Capabilities

- `introduce-image-management`: 扩展 IntroduceImage 实体支持竞赛关联，添加 `competition_id` 和 `sort_order` 字段

## Impact

- 数据库：新增 `tb_competition` 表，修改 `tb_introduce_image` 表（添加字段）
- API：新增公开接口和管理接口
- 权限：新增竞赛管理相关权限
- 无需新增 MinIO 存储桶（复用 normal-img 桶）
