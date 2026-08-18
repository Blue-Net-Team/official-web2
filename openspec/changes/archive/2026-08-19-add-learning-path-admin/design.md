# Design: add-learning-path-admin

## Context

学习路径功能现状：后端具备完整 CRUD（`AdminLearningPathController`）与公开查询接口，但前端仅有公开展示页 `/direction/[slug]`，且步骤序号/标题硬编码在 `data.ts`，后端只补充 `videoLink`。管理员无管理界面可用。本次变更横跨数据库、后端全分层、前端公开页与管理端，属于跨切面变更，且包含破坏性 API 契约修改与数据迁移。

约束：
- 后端 DDD 分层，返回类型遵守层间约定；接口必须 `@RequiresPermission` 且 value 全局唯一（已存在 `direction-learning-path:view/create/update/delete`，不新增权限标识）
- 前端 ISR 页面 `revalidate` 必须为静态字面量（现有 `3600` 保留）
- Flyway 迁移只增不改，新增 `V26__rename_video_url_to_related_url.sql`
- Java 代码禁用 `var`，显式声明类型

## Goals / Non-Goals

**Goals:**
- 提供 `/admin/learning-path` 管理页：Tab + Table + 右侧 Drawer，完整 CRUD
- `video_url` 全链路更名为 `related_url`，语义扩展为"相关链接"
- 公开页学习路径区块完全后端数据驱动，移除前端硬编码步骤
- 数据库种子标题与现行展示文案对齐

**Non-Goals:**
- 不做方向管理员的方向隔离（任何方向管理员可管理全部方向，记录为已知限制）
- 不改动公开展示页的其他区块（Hero/TechStack/Career/Recruitment 仍由 `data.ts` 驱动）
- 不新增权限标识，不修改 `PermissionScanner`
- 不支持步骤拖拽排序（通过编辑"步骤序号"实现调整）

## Decisions

### D1: 命名采用 `related_url` / `relatedUrl` / `relatedLink`

- DB 列：`related_url VARCHAR(500)`
- Java（Entity/DO/Command/Result）：`relatedUrl`
- API DTO（`LearningStepDTO`、Create/Update Request）：`relatedLink`
- 前端 DTO/组件：`relatedLink`

理由：与产品语义"相关链接"直接对齐。备选 `resource_url`（偏"资源"）、`link_url`（过泛）均不如 `related` 准确。DTO 层沿用现有 `*Link` 后缀风格（原 `videoLink`），保持契约风格一致。

### D2: 数据库迁移在 V26 中同时完成改名与标题同步

```sql
ALTER TABLE tb_direction_learning_step RENAME COLUMN video_url TO related_url;
COMMENT ON COLUMN tb_direction_learning_step.related_url IS '相关链接URL';
-- UPDATE 各方向步骤标题为现行展示文案（与线上 data.ts 一致）
```

理由：改名与标题同步是同一上线单元，分开迁移会让前台在两次部署间显示旧标题。备选"管理员事后手动改"被否决——上线瞬间前台文案即"倒退"为旧种子标题。

### D3: 公开页数据获取策略——后端为主，失败渲染空区块

`/direction/[slug]/page.tsx` 中：
- 删除 `data.ts` 的 `learningPath` 字段及 `LearningStep` 类型中的相关定义
- 直接以 `directionService.getLearningPath(slug)` 返回的 `steps` 渲染
- try/catch 兜底：后端失败时 `steps = []`，`LearningPath` 组件渲染空区块（仅区块标题"学习路径"），页面其余部分正常
- `LearningStep` 前端类型改为 `{ stepNumber, title, relatedLink? }`，与后端 `LearningStepDTO` 对齐

理由：用户已确认"直接后端数据驱动"。备选"保留 data.ts 兜底"被否决——保留假数据会让后台删除步骤后前台仍显示旧内容，语义矛盾。ISR（`revalidate = 3600`）保证正常路径下构建/增量再验证时拉取，后端短暂故障只影响个别再验证窗口。

### D4: 管理页复用 message-template 模式

- 路由：`/admin/learning-path/page.tsx`（client component）
- 布局：`Tabs`（cv / embed / struct，Tab label 用方向中文名）+ `Table`（步骤序号、标题、相关链接、操作）+ `LearningStepDrawer`（右侧 Drawer，新增/编辑共用）
- 表单字段：步骤序号（InputNumber，≥1）、标题（Input，必填）、相关链接（Input，可选，URL 格式校验）
- 删除：`Popconfirm` 确认后调 DELETE
- service：`direction.service.ts` 新增 `adminDirectionService`（认证 client）：`getLearningPath(slug)`（复用公开查询即可，无需新增后端接口）、`createStep`、`updateStep`、`deleteStep`

理由：`message-template` 页是项目内最相近的成熟范式（Table + Drawer + useApi），直接对齐可降低 review 成本。备选 Steps 编辑器/拖拽排序交互过重，当前每方向仅约 4 步。

### D5: 菜单与权限

- AdminNav `menuConfig` 新增：`{ key: 'learning-path', label: '学习路线管理', path: '/admin/learning-path', icon: <NodeIndexOutlined />, minLevel: 2 }`（方向管理员 `DIRECTION_ADMIN`=2 及以上可见）
- 后端接口权限不变；部署后需在权限管理页为方向管理员角色勾选 `direction-learning-path:create/update/delete`

理由：内容运营性质，与方向管理员职责匹配。前端菜单只做可见性过滤，真实鉴权仍由后端注解兜底。

### D6: 前端文案同步

`LearningPath` 组件中"点击观看视频"改为"查看相关资料"；管理页列名"相关链接"。

## Risks / Trade-offs

- [V1 种子标题与现行展示不一致，打通后前台文案回退] → V26 迁移同步 UPDATE 标题（见 D2）
- [ISR 再验证窗口内后端故障 → 学习路径区块为空并可能被缓存 1 小时] → 可接受：属降级场景；缓存随下一次成功再验证自愈
- [`relatedLink` 为破坏性 API 变更，若有第三方调用方会解析失败] → 消费方仅自家前端，同 PR 同步修改；上线顺序先后端再前端
- [方向管理员可跨方向修改] → 已知限制，写入提案；后续如需隔离单独开变更
- [步骤序号冲突由后端 400 拒绝（uk_direction_step 唯一约束）] → Drawer 表单提交失败时透出后端错误消息，提示更换序号

## Migration Plan

1. 后端：V26 迁移 + 全链路改名 + 测试更新 → 编译打包 → 重建 `bluenet-api-service:latest` 镜像并重启容器
2. 前端：service/DTO 改名、公开页打通、新增管理页 → 部署
3. 部署顺序：先后端（迁移+接口）再前端；期间公开页短暂请求旧字段会拿不到链接，窗口内可接受（或选择低峰部署）
4. 部署后：权限管理页为方向管理员角色授权；抽查 `/direction/cv` 前台标题/链接与管理页 CRUD 全链路
5. 回滚：前端回滚即可恢复旧展示（旧前端读 `videoLink`，后端已改名字段会缺失 → 链接不显示但标题仍在 data.ts）；如需完整回滚后端，准备反向迁移脚本手动执行（Flyway 不支持自动回滚）

## Open Questions

- 无（命名、范围、权限级别、降级策略均已在 explore 阶段确认）
