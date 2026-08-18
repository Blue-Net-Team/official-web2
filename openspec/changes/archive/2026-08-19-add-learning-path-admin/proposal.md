# Proposal: add-learning-path-admin

## Why

学习路径功能已有完整的后端管理 API（增删改学习步骤），但前端没有对应的管理页面，管理员只能通过 Swagger/Apifox 直接调接口维护数据。同时，`videoLink`（视频链接）的语义需要扩展为"相关链接"以支持任意资料链接，且公开展示页的步骤序号/标题仍硬编码在前端 `data.ts` 中，后台修改无法生效。

## What Changes

- **新增管理页面** `/admin/learning-path`：Tab（cv/embed/struct 三个方向）+ Table（序号/标题/相关链接/操作）+ 右侧 Drawer（新增/编辑表单），支持删除确认
- **字段语义扩展（BREAKING）**：`video_url`/`videoUrl`/`videoLink` 全链路改名为 `related_url`/`relatedUrl`/`relatedLink`，语义从"视频链接"扩展为"相关链接"
- **数据库迁移 V26**：`tb_direction_learning_step.video_url` 重命名为 `related_url`，并将种子数据标题同步为现行展示文案
- **公开页打通后端（BREAKING）**：`/direction/[slug]` 移除 `data.ts` 中的 `learningPath` 硬编码，学习路径区块完全由后端数据驱动；后端不可用时渲染空区块（仅保留区块标题）
- **菜单注册**：AdminNav 新增"学习路线管理"菜单项，`minLevel: 2`（方向管理员及以上可见）
- **已知限制**：后端不区分方向管理员的方向归属，任何方向管理员均可管理全部三个方向的学习步骤（本次不做方向隔离）

## Capabilities

### New Capabilities

- `admin-learning-path-page`: 学习路径后台管理页面，提供三个方向学习步骤的查看、新增、编辑、删除交互

### Modified Capabilities

- `backend-direction-learning-path`: 字段 `video_url`/`videoUrl`/`videoLink` 更名为 `related_url`/`relatedUrl`/`relatedLink`，语义扩展为"相关链接"（API 契约破坏性变更）
- `frontend-direction-learning-path-service`: 前端 service/DTO 字段同步更名，新增 admin CRUD 方法（认证 client）
- `frontend-direction-detail-page`: 学习路径区块改为完全后端数据驱动，移除前端硬编码步骤，后端不可用时渲染空区块

## Impact

**后端**（`src/backend`）：
- 新增 Flyway 迁移 `V26__rename_video_url_to_related_url.sql`
- `DirectionLearningStep`（Entity）、`DirectionLearningStepDO`、`LearningPathMapper.xml`、`LearningPathRepositoryConverter`、`LearningPathCommands`、`LearningPathResult`、`LearningPathAppServiceImpl`、`LearningPathRequestConverter`、`LearningPathResponseConverter`、`LearningStepDTO`、`CreateLearningStepRequestDTO`、`UpdateLearningStepRequestDTO`
- 集成测试：`LearningPathControllerIntegrationTest`、`AdminLearningPathControllerIntegrationTest`、`LearningPathAppServiceImplIntegrationTest`、`LearningPathRepositoryImplIntegrationTest`

**前端**（`src/frontend`）：
- 新增：`app/admin/learning-path/page.tsx`、`LearningStepDrawer.tsx`
- 修改：`apis/services/direction.service.ts`、`apis/schema/direction.dto.ts`、`components/Admin/AdminNav/index.tsx`、`app/(public)/(other)/direction/[slug]/page.tsx`、`components/Direction/LearningPath/index.tsx`、`components/Direction/types.ts`、`components/Direction/data.ts`（移除 learningPath 字段）

**API 契约**：`LearningStepDTO.videoLink` → `relatedLink`（破坏性变更，消费方仅自家前端，同步修改）

**文档**：`docs/05-参考手册/05-01-数据库设计.md` 表结构说明同步更新

**运维**：上线后需在权限管理页为方向管理员角色授予 `direction-learning-path:create/update/delete` 权限
