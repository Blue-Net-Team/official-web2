## Why

当前系统已有团队成员列表页面，但缺少成员详情展示页面。UI 设计师已完成成员信息页面的设计，需要实现一个面向访客的成员详情展示页面，用于展示成员的个人信息、项目经历、竞赛经历和实习经历等完整信息。

## What Changes

- 新增成员详情页面，支持通过成员 ID 访问（路由：`/members/[id]`）
- 展示成员基本信息：头像、用户名、昵称、角色、个人简介、学院、专业、年级等
- 展示成员统计数据：项目经历数、竞赛经历数、实习经历数
- 实现 Tab 切换功能：个人信息、项目经历、竞赛经历、实习经历
- 展示成员经历卡片：包含项目名称/竞赛名称/实习公司、角色、时间、描述、技术栈、链接等
- 页面为公开页面，无需登录即可查看

## Capabilities

### New Capabilities

- `member-profile-view`: 成员信息展示页面，包含成员基本信息展示、经历列表展示、Tab 切换等功能

### Modified Capabilities

- `member-list-api`: 扩展现有成员 API 规格，新增获取成员详情接口的需求定义

## Impact

- **新增文件**:
  - `src/app/(public)/(other)/members/[id]/page.tsx` - 成员详情页面组件
  - `src/components/MemberProfile/` - 成员信息展示组件目录
  - `src/apis/services/member-profile.service.ts` - 成员详情 API 服务
- **修改文件**:
  - `src/apis/services/member.service.ts` - 新增获取成员详情的方法
  - `src/apis/schema/type.ts` - 新增成员详情和经历相关的类型定义
- **依赖**:
  - 依赖后端 `GET /api/v1/members/{id}` 接口
  - 依赖文件下载接口展示头像
