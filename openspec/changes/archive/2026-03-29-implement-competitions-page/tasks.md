## 1. 后端 - 数据库迁移

- [x] 1.1 创建 Flyway 迁移脚本 `V18__add_competition_fields.sql`
- [x] 1.2 添加 `level` VARCHAR(20) NOT NULL DEFAULT '省级' 字段
- [x] 1.3 添加 `month` VARCHAR(10) 可空字段
- [x] 1.4 添加 `organizer` VARCHAR(200) 可空字段
- [x] 1.5 确认 `tb_introduce_image` 表已存在 `competition_id` 字段用于关联介绍图片

## 2. 后端 - Entity 和 DTO 更新

- [x] 2.1 更新 `Competition` 实体类，添加 level、month、organizer 字段
- [x] 2.2 更新 `CompetitionBriefDTO`，添加 level、month、organizer、introduceImageFileId 字段
- [x] 2.3 更新 `CompetitionDetailDTO`，添加新字段
- [x] 2.4 更新 `CreateCompetitionRequestDTO`，添加 level、month、organizer 字段及校验注解
  - level: 必填，默认 "省级"
  - month: 可选
  - organizer: 可选
- [x] 2.5 更新 `UpdateCompetitionRequestDTO`，添加 level、month、organizer 字段

## 3. 后端 - 介绍图片查询逻辑

- [x] 3.1 创建 `IntroduceImageMapper` 查询方法：根据 competition_id 和 type='competition' 查询介绍图片
- [x] 3.2 更新 `CompetitionServiceImpl`，在获取竞赛列表/详情时关联查询介绍图片
- [x] 3.3 取 `sort_order` 最小的图片作为 `introduceImageFileId`
- [x] 3.4 无介绍图片时返回 `introduceImageFileId` 为 null

## 4. 后端 - 业务逻辑更新

- [x] 4.1 更新 `CompetitionConverter`，添加新字段的转换逻辑
- [x] 4.2 更新 `CompetitionServiceImpl`，确保新字段在创建/更新时正确处理
  - 创建时 level 默认为 "省级"
  - month 和 organizer 可为 null
- [x] 4.3 运行后端测试确保无回归

## 5. 前端 - 项目结构准备

- [x] 5.1 创建竞赛页面目录 `src/app/(public)/(other)/competitions/`
- [x] 5.2 创建子目录 `src/components/CompetitionCard/`
- [x] 5.3 创建类型定义文件 `src/apis/schema/type.ts`（更新 CompetitionBriefDTO）
- [x] 5.4 创建 API 调用文件 `src/apis/services/competition.service.ts`

## 6. 前端 - 类型定义和 API

- [x] 6.1 定义 `CompetitionBriefDTO` 接口，包含所有后端字段
  - level: '国家级' | '省级'
  - month?: string (可选)
  - organizer?: string (可选)
  - introduceImageFileId?: number (可选)
- [x] 6.2 创建 `fetchCompetitions()` 函数调用后端接口
- [x] 6.3 添加错误处理逻辑

## 7. 前端 - CSS Modules 样式

- [x] 7.1 创建 `src/app/(public)/(other)/competitions/page.module.css`
- [x] 7.2 创建 `src/components/CompetitionCard/CompetitionCard.module.css`
- [x] 7.3 实现页面布局样式（黑色背景、内边距、标题样式）
- [x] 7.4 实现卡片样式（背景色、圆角、内边距）
- [x] 7.5 实现响应式样式（桌面端/移动端适配）
- [x] 7.6 实现级别标签样式（国家级橙色 #E86835、省级蓝色 #4A90E2）
- [x] 7.7 实现第一张卡片介绍图片背景样式
- [x] 7.8 实现渐变遮罩样式（270度，#1a1a1a 45% 到 #1a1a1aaa 100%）

## 8. 前端 - 竞赛卡片组件

- [x] 8.1 创建 `CompetitionCard.tsx` 组件
- [x] 8.2 实现卡片基础布局（flex 垂直布局，间距 12px）
- [x] 8.3 实现卡片头部（名称 + 级别标签 + 时间）
- [x] 8.4 实现级别标签组件（根据 level 显示不同颜色）
- [x] 8.5 实现举办时间显示逻辑（month 有值时显示，移动端隐藏）
- [x] 8.6 实现主办单位信息展示（organizer 有值时显示）
- [x] 8.7 实现竞赛简介展示（行高 1.5，颜色 #ffffffcc）
- [x] 8.8 实现第一张卡片的介绍图片背景（introduceImageFileId 有值时）
- [x] 8.9 实现渐变遮罩效果
- [x] 8.10 实现其他卡片的深色背景 (#1a1a1a)

## 9. 前端 - 页面组件

- [x] 9.1 创建 `page.tsx` 页面组件（Server Component）
- [x] 9.2 在 page.tsx 中调用后端接口获取数据
- [x] 9.3 实现页面标题 "团队参加的竞赛"（白色，48px/28px 响应式）
- [x] 9.4 实现页面副标题（#ffffff99，20px/14px 响应式）
- [x] 9.5 实现竞赛卡片列表渲染
- [x] 9.6 实现错误处理和空状态展示

## 10. 前端 - 响应式适配

- [x] 10.1 实现桌面端样式（>= 768px）
  - 页面内边距 80px 垂直 / 147px 水平
  - 页面标题 48px，副标题 20px
  - 卡片高度 200px，圆角 24px
  - 标签尺寸 80x28px，圆角 14px
  - 举办时间 24px（month 有值时显示）
- [x] 10.2 实现移动端样式（< 768px）
  - 页面内边距 40px 垂直 / 24px 水平
  - 页面标题 28px，副标题 14px
  - 卡片高度自适应，圆角 20px
  - 标签尺寸 64x24px，圆角 12px
  - 举办时间隐藏

## 11. 测试与验证

### 后端验证
- [x] 11.1 验证后端迁移脚本执行正常
- [x] 11.2 验证后端接口返回新字段（level、month、organizer、introduceImageFileId）
- [x] 11.3 验证创建竞赛时 level 默认为 "省级"
- [x] 11.4 验证 month 和 organizer 可为空
- [x] 11.5 验证现有数据无需补充 month 和 organizer
- [x] 11.6 验证介绍图片查询逻辑（取 sort_order 最小的一张）

### 前端验证
- [x] 11.7 验证前端能正确获取和显示数据
- [x] 11.8 桌面端视觉走查（对比设计稿）
- [x] 11.9 移动端视觉走查（对比设计稿）
- [x] 11.10 验证 month 为空时不显示举办时间
- [x] 11.11 验证 organizer 为空时不显示主办单位
- [x] 11.12 验证第一张卡片介绍图片背景正常（有图片时）
- [x] 11.13 验证第一张卡片默认背景正常（无图片时）
- [x] 11.14 验证介绍图片只取一张（不支持轮播）
- [x] 11.15 运行前端代码检查

## 12. 代码审查

- [x] 12.1 邀请代码审查师审查后端实现
- [x] 12.2 邀请代码审查师审查前端实现
- [x] 12.3 根据反馈修改代码（已更新 spec.md 使规格说明与代码实现一致）
