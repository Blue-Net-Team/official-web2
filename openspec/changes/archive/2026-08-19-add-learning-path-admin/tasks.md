# Tasks: add-learning-path-admin

## 1. 数据库迁移

- [x] 1.1 新增 `V26__rename_video_url_to_related_url.sql`：`ALTER TABLE tb_direction_learning_step RENAME COLUMN video_url TO related_url`，补充列注释"相关链接URL"
- [x] 1.2 V26 中 UPDATE 三个方向既有步骤标题为现行展示文案（对齐原 `data.ts`：cv「Python基础/OpenCV图像处理基础/Linux开发板的使用/深度学习与目标检测」、embed「C语言基础/单片机基础/外设通信与控制/PCB设计与绘制」、struct「机械制图基础/三维建模入门/装配与工程图/仿真与优化」）
- [x] 1.3 更新 `docs/05-参考手册/05-01-数据库设计.md` 中 `tb_direction_learning_step` 表结构（video_url → related_url）

## 2. 后端改名（TDD：先改测试，再改实现）

- [x] 2.1 更新集成测试中的字段引用（`LearningPathControllerIntegrationTest`、`AdminLearningPathControllerIntegrationTest`、`LearningPathAppServiceImplIntegrationTest`、`LearningPathRepositoryImplIntegrationTest`），确认测试红灯
- [x] 2.2 领域层：`DirectionLearningStep` 字段 `videoUrl` → `relatedUrl`，`updateVideoUrl` → `updateRelatedUrl`，注释改为"相关链接"
- [x] 2.3 基础设施层：`DirectionLearningStepDO`、`LearningPathMapper.xml`、`LearningPathRepositoryConverter` 同步改名
- [x] 2.4 应用层：`LearningPathCommands`、`LearningPathResult`、`LearningPathAppServiceImpl` 同步改名
- [x] 2.5 API 层：`LearningStepDTO`、`CreateLearningStepRequestDTO`、`UpdateLearningStepRequestDTO` 字段 `videoLink` → `relatedLink`；`LearningPathRequestConverter`、`LearningPathResponseConverter` 同步更新
- [x] 2.6 运行全部学习路径相关集成测试，确认绿灯

## 3. 前端 service 与类型

- [x] 3.1 `apis/schema/direction.dto.ts`：`LearningStepDTO.videoLink` → `relatedLink`
- [x] 3.2 `apis/services/direction.service.ts`：新增 admin CRUD 方法（`createStep` / `updateStep` / `deleteStep`，认证 client；查询复用公开接口）

## 4. 前端公开页打通

- [x] 4.1 `components/Direction/types.ts`：`LearningStep` 类型字段对齐后端（`stepNumber` / `title` / `relatedLink?`）
- [x] 4.2 `components/Direction/data.ts`：移除三个方向的 `learningPath` 字段
- [x] 4.3 `app/(public)/(other)/direction/[slug]/page.tsx`：移除 merge 逻辑，直接使用后端 `steps`；catch 时传空数组；保留 `revalidate = 3600` 静态字面量
- [x] 4.4 `components/Direction/LearningPath/index.tsx`：字段改用 `relatedLink`，文案"点击观看视频" → "查看相关资料"，空数据时仅渲染区块标题

## 5. 前端管理页

- [x] 5.1 新增 `app/admin/learning-path/page.tsx`：Tab（cv/embed/struct，中文 label）+ Table（步骤序号/标题/相关链接/操作），切换 Tab 重新拉取
- [x] 5.2 新增 `app/admin/learning-path/LearningStepDrawer.tsx`：右侧 Drawer，新增/编辑共用；表单校验（序号 ≥1、标题必填、链接 URL 格式）；提交失败透出后端错误消息
- [x] 5.3 删除交互：`Popconfirm` 二次确认后调用 `deleteStep` 并刷新列表
- [x] 5.4 `components/Admin/AdminNav/index.tsx`：注册菜单项"学习路线管理"（`path: /admin/learning-path`，`minLevel: 2`，图标 `NodeIndexOutlined`）

## 6. 编译与端到端验证

- [x] 6.1 后端 `mvnw clean compile package`，构建 `bluenet-api-service:latest` 镜像并重启容器（compose 基础设施先行）
- [x] 6.2 检查 3000 端口占用：已占用则直接用现有前端服务验证；未占用方可 `pnpm dev`
- [x] 6.3 Playwright 验证公开页 `/direction/cv|embed|struct`：步骤标题与数据库一致、有相关链接的步骤可点击、文案为"查看相关资料"
- [x] 6.4 Playwright 验证管理页全链路：方向管理员登录 → Tab 切换 → 新增 → 编辑 → 删除 → 前台页面（等待 ISR 再验证或重启前端）确认变更生效
- [x] 6.5 权限验证：在权限管理页为方向管理员角色授予 `direction-learning-path:create/update/delete`；MEMBER 角色菜单不可见"学习路线管理"
