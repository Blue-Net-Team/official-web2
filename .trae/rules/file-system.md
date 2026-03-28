---
alwaysApply: false
description: 涉及文件上传下载、存储权限时使用
---

# 文件存储规范

使用 MinIO 对象存储，按文件类型划分存储桶。

## 文件类型（FileType 枚举）

| 枚举值 | 存储桶 | 说明 |
|--------|--------|------|
| `AVATAR` | `avatar` | 用户头像 |
| `NORMAL_IMG` | `normal-img` | 普通图片 |
| `ASSESSMENT_ATTACHMENT` | `assessment-attachment` | 考题附件 |
| `WORK` | `work` | 考生作品 |
| `QRCODE` | `qrcode` | 二维码 |

## 文件命名

`{fileType}-{uuid}.{ext}`（如 `avatar-xxx.jpg`）

## 权限控制（动态判断）

| 文件类型 | 权限逻辑 |
|----------|----------|
| `work` | 本人 或 `ROLE_MEMBER+` |
| `assessment_attachment` | 同方向用户 |
| `avatar` | `ROLE_MEMBER+` 或本人头像 |
| `normal_img` / `qrcode` | 公开 |

## 接口

- 上传：`POST /api/v1/file/upload/{type}`
- 下载：`GET /api/v1/file/download/{fileId}`
