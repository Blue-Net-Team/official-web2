## Why

当前系统缺少介绍图片上传接口，导致官网首页无法通过后台管理上传实验室介绍、方向介绍、竞赛介绍（合照和Logo）、团队合照等图片。前端无法获取这些图片的 fileId，无法完成图片关联操作。

## What Changes

- 新增 `POST /api/v1/file/upload/introduce-image` 接口，支持上传实验室介绍、设备介绍、团队合照、方向介绍、专利介绍、论文介绍等图片
- 新增 `POST /api/v1/file/upload/competition/image` 接口，支持上传竞赛合照
- 新增 `POST /api/v1/file/upload/competition/logo` 接口，支持上传竞赛Logo
- 在 `IntroduceImageDomainService` 新增 `addIntroduceImage()` 方法
- 在 `CompetitionDomainService` 新增 `updateLogo()` 方法

## Capabilities

### New Capabilities

- `introduce-image-upload`: 介绍图片上传功能，支持实验室介绍、设备介绍、团队合照、方向介绍、专利介绍、论文介绍等图片的上传

### Modified Capabilities

- `competition-management`: 新增竞赛Logo上传接口，竞赛合照上传接口

## Impact

- **控制层**: `FileUploadController.java` 新增 3 个接口
- **应用层**: `FileService.java` / `FileServiceImpl.java` 新增 3 个方法
- **领域层**: `IntroduceImageDomainService.java` 新增方法，`CompetitionDomainService.java` 新增方法
- **权限**: 新增 3 个权限配置
