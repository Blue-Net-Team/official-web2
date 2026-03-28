## ADDED Requirements

### Requirement: Database Schema Extension
数据库 SHALL 扩展 tb_competition 表以支持新字段。

#### Scenario: Add new columns to tb_competition
- **WHEN** 执行 Flyway 迁移脚本
- **THEN** 添加 `level` VARCHAR(20) NOT NULL DEFAULT '省级' 字段
- **AND** 添加 `month` VARCHAR(10) 可空字段
- **AND** 添加 `organizer` VARCHAR(200) 可空字段

### Requirement: Introduce Image Design
介绍图片（introduceImage）SHALL 使用现有文件系统存储。

#### Scenario: Store introduce image in file system
- **WHEN** 上传竞赛介绍图片
- **THEN** 文件类型为 `FileType.NORMAL_IMG`
- **AND** 图片子类型为 `ImageType.COMPETITION`
- **AND** 存储在 `tb_introduce_image` 表
- **AND** 通过 `competition_id` 关联到竞赛

#### Scenario: Retrieve introduce image for competition
- **WHEN** 获取竞赛列表或详情
- **THEN** 查询 `tb_introduce_image` 表
- **AND** 条件：`type = 'competition' AND competition_id = ?`
- **AND** 按 `sort_order` 降序排序取第一张（sort_order 值越大优先级越高）
- **AND** 返回关联的 `file_id` 作为 `introduceImageFileId`

#### Scenario: Introduce image specifications
- **WHEN** 上传介绍图片
- **THEN** 推荐长方形横图
- **AND** 尺寸无具体限制，推荐 1200x600 或类似比例
- **AND** 暂不支持多张轮播，只取 sort_order 最大的一张

### Requirement: Backend DTO Extension
后端 DTO SHALL 包含新字段。

#### Scenario: CompetitionBriefDTO includes new fields
- **WHEN** 获取竞赛列表接口返回数据
- **THEN** 包含 `level` 字段（国家级/省级）
- **AND** 包含 `month` 字段（如 "5月"）
- **AND** 包含 `organizer` 字段（可为 null）
- **AND** 包含 `introduceImageFileId` 字段（可为 null）

#### Scenario: CreateCompetitionRequestDTO validation
- **WHEN** 创建竞赛时
- **THEN** `level` 字段必填，只允许 "国家级" 或 "省级"，默认 "省级"
- **AND** `month` 字段可选
- **AND** `organizer` 字段可选，最大长度 200
- **AND** 介绍图片通过 `tb_introduce_image` 关联，不在创建请求中指定

#### Scenario: Month field is optional
- **WHEN** 创建或更新竞赛时 month 字段为空
- **THEN** 数据库存储 NULL
- **AND** 前端不显示举办时间

#### Scenario: Organizer field is optional
- **WHEN** 创建或更新竞赛时 organizer 字段为空
- **THEN** 数据库存储 NULL
- **AND** 前端不显示主办单位行

### Requirement: API Integration
页面 SHALL 调用后端接口获取竞赛列表数据。

#### Scenario: Fetch competition list from API
- **WHEN** 页面作为 Server Component 渲染
- **THEN** 调用 `GET /api/v1/competitions?limit=10` 接口
- **AND** 使用返回的完整竞赛数据渲染卡片
- **AND** 接口返回字段包括：id, name, shortName, logoFileId, summary, level, month, organizer, introduceImageFileId

#### Scenario: Handle API error
- **WHEN** 后端接口调用失败
- **THEN** 显示错误提示信息
- **AND** 或降级显示空状态

### Requirement: Page Layout
页面 SHALL 实现响应式布局，支持桌面端和移动端展示。

#### Scenario: Desktop layout
- **WHEN** 用户在桌面端浏览器访问页面
- **THEN** 页面宽度为 1440px，内边距为 80px 垂直 / 147px 水平
- **AND** 页面标题字号为 48px，副标题字号为 20px
- **AND** 竞赛卡片高度为 200px，圆角为 24px

#### Scenario: Mobile layout
- **WHEN** 用户在移动端浏览器访问页面
- **THEN** 页面宽度为 375px，内边距为 40px 垂直 / 24px 水平
- **AND** 页面标题字号为 28px，副标题字号为 14px
- **AND** 竞赛卡片高度自适应，圆角为 20px

### Requirement: Page Header
页面 SHALL 展示标题和副标题。

#### Scenario: Display page header
- **WHEN** 页面加载完成
- **THEN** 显示标题 "团队参加的竞赛"，颜色为白色 (#ffffff)
- **AND** 显示副标题 "记录我们在各类竞赛中的成长与突破"，颜色为半透明白色 (#ffffff99)
- **AND** 标题与副标题间距为 48px (桌面端) / 24px (移动端)

### Requirement: Competition Cards
页面 SHALL 展示竞赛卡片列表。

#### Scenario: Display competition cards
- **WHEN** 页面加载完成
- **THEN** 显示竞赛卡片列表
- **AND** 每个卡片包含：竞赛名称、级别标签、举办时间、主办单位（可为空）、竞赛简介
- **AND** 卡片之间间距为 20px (桌面端) / 16px (移动端)

### Requirement: First Card Styling
第一个竞赛卡片 SHALL 使用介绍图片背景 + 渐变遮罩效果。

#### Scenario: First card with introduce image background
- **WHEN** 页面渲染第一个竞赛卡片且 `introduceImageFileId` 不为空
- **THEN** 卡片使用介绍图片作为背景
- **AND** 叠加线性渐变遮罩，方向 270度，从 #1a1a1a (45%) 到 #1a1a1aaa (100%)
- **AND** 遮罩覆盖整个卡片

#### Scenario: First card without introduce image
- **WHEN** 页面渲染第一个竞赛卡片但 `introduceImageFileId` 为空
- **THEN** 使用默认深色背景 (#1a1a1a)

### Requirement: Other Cards Styling
其他竞赛卡片 SHALL 使用深色背景。

#### Scenario: Other cards with dark background
- **WHEN** 页面渲染第 2-N 个竞赛卡片
- **THEN** 卡片背景色为 #1a1a1a
- **AND** 不使用背景图片
- **AND** 无边框

### Requirement: Level Badge
竞赛卡片 SHALL 显示级别标签。

#### Scenario: National level badge
- **WHEN** 竞赛级别为国家级
- **THEN** 标签背景色为 #E86835 (橙色)
- **AND** 标签文字为 "国家级"
- **AND** 标签尺寸为 80x28px (桌面端) / 64x24px (移动端)
- **AND** 标签圆角为 14px (桌面端) / 12px (移动端)

#### Scenario: Provincial level badge
- **WHEN** 竞赛级别为省级
- **THEN** 标签背景色为 #4A90E2 (蓝色)
- **AND** 标签文字为 "省级"
- **AND** 标签尺寸为 80x28px (桌面端) / 64x24px (移动端)
- **AND** 标签圆角为 14px (桌面端) / 12px (移动端)

### Requirement: Competition Information
竞赛卡片 SHALL 正确显示竞赛信息。

#### Scenario: Display competition name
- **WHEN** 渲染竞赛卡片
- **THEN** 显示竞赛名称
- **AND** 字号为 28px (桌面端) / 20px (移动端)
- **AND** 字重为 700 (bold)
- **AND** 颜色为白色 (#ffffff)

#### Scenario: Display competition time when present
- **WHEN** 渲染竞赛卡片且 `month` 不为空
- **THEN** 显示举办时间（月份）
- **AND** 字号为 24px (桌面端) / 隐藏 (移动端)
- **AND** 颜色为半透明白色 (#ffffff66)

#### Scenario: Hide competition time when absent
- **WHEN** 渲染竞赛卡片且 `month` 为空或 null
- **THEN** 不显示举办时间行
- **AND** 移动端始终不显示（无论是否有值）

#### Scenario: Display organizer when present
- **WHEN** 渲染竞赛卡片且 `organizer` 不为空
- **THEN** 显示 "主办单位：" 标签，颜色为 #ffffff99
- **AND** 显示主办单位名称，颜色为白色 (#ffffff)
- **AND** 字号为 16px (桌面端) / 14px (移动端)

#### Scenario: Hide organizer when absent
- **WHEN** 渲染竞赛卡片且 `organizer` 为空或 null
- **THEN** 不显示主办单位行

#### Scenario: Display description
- **WHEN** 渲染竞赛卡片
- **THEN** 显示竞赛简介
- **AND** 字号为 16px (桌面端) / 14px (移动端)
- **AND** 颜色为 #ffffffcc
- **AND** 行高为 1.5

### Requirement: Card Layout
竞赛卡片内部 SHALL 正确布局。

#### Scenario: Card internal layout
- **WHEN** 渲染竞赛卡片
- **THEN** 卡片内边距为 32px 垂直 / 40px 水平 (桌面端)
- **AND** 卡片内边距为 24px 垂直 / 20px 水平 (移动端)
- **AND** 卡片内部使用 flex 垂直布局
- **AND** 卡片内部间距为 12px

### Requirement: CSS Modules Usage
前端 SHALL 使用 CSS Modules 管理样式。

#### Scenario: Use CSS Modules for styling
- **WHEN** 开发前端组件
- **THEN** 使用 `.module.css` 文件定义样式
- **AND** 不使用 Tailwind CSS 类名
- **AND** 样式类名使用 camelCase 命名
