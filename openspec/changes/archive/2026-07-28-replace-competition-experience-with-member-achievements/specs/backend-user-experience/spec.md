## MODIFIED Requirements

### Requirement: 用户可以查询经历列表

系统SHALL允许用户查询自己的经历列表，支持按类型过滤。经历类型仅包括项目经历和实习经历。

#### Scenario: 查询所有经历
- **WHEN** 已登录用户请求 GET /api/v1/user/experiences
- **THEN** 系统返回用户所有经历列表（项目、实习混合）

#### Scenario: 按类型过滤经历
- **WHEN** 已登录用户请求 GET /api/v1/user/experiences?type=project
- **THEN** 系统仅返回项目类型经历

#### Scenario: 查询竞赛类型经历
- **WHEN** 已登录用户请求 GET /api/v1/user/experiences?type=competition
- **THEN** 系统返回空列表

#### Scenario: 未登录用户查询经历
- **WHEN** 未登录用户请求 GET /api/v1/user/experiences
- **THEN** 系统返回401 Unauthorized错误

### Requirement: 用户可以创建经历

系统SHALL允许用户创建新的项目或实习经历记录。

#### Scenario: 创建项目经历
- **WHEN** 已登录用户请求 POST /api/v1/user/experiences，包含：
  ```json
  {
    "type": "project",
    "name": "智能交通监控系统",
    "role": "项目负责人",
    "startDate": "2024.09",
    "endDate": "2025.01",
    "description": "基于YOLOv8和DeepSort实现的多目标跟踪系统",
    "techStack": ["Python", "PyTorch", "OpenCV", "YOLOv8"],
    "demoUrl": "https://demo.example.com"
  }
  ```
- **THEN** 系统创建经历记录
- **AND** 系统返回创建的经历信息（包含id）

#### Scenario: 创建实习经历
- **WHEN** 已登录用户请求 POST /api/v1/user/experiences，包含：
  ```json
  {
    "type": "internship",
    "company": "字节跳动",
    "position": "算法实习生",
    "startDate": "2025.01",
    "status": "active",
    "description": "在推荐算法团队参与短视频推荐系统的优化工作",
    "achievements": ["优化用户兴趣模型，点击率提升3%"]
  }
  ```
- **THEN** 系统创建经历记录
- **AND** 系统返回创建的经历信息（包含id）

#### Scenario: 创建竞赛经历
- **WHEN** 已登录用户请求 POST /api/v1/user/experiences，包含 `type: "competition"`
- **THEN** 系统返回 400 错误，提示"竞赛经历已下线，请联系管理员在成就系统中维护"

### Requirement: 用户可以更新经历

系统SHALL允许用户更新自己的项目或实习经历记录。

#### Scenario: 更新经历
- **WHEN** 已登录用户请求 PUT /api/v1/user/experiences/{id}，包含部分字段
- **THEN** 系统更新指定经历
- **AND** 系统返回更新后的经历信息

#### Scenario: 更新不属于自己的经历
- **WHEN** 已登录用户尝试更新其他用户的经历
- **THEN** 系统返回404 Not Found错误

#### Scenario: 更新竞赛类型经历
- **WHEN** 已登录用户尝试更新类型为竞赛的经历
- **THEN** 系统返回 404 Not Found 错误

### Requirement: 用户可以删除经历

系统SHALL允许用户删除自己的项目或实习经历记录。

#### Scenario: 删除经历
- **WHEN** 已登录用户请求 DELETE /api/v1/user/experiences/{id}
- **THEN** 系统删除指定经历
- **AND** 系统返回204 No Content

#### Scenario: 删除不属于自己的经历
- **WHEN** 已登录用户尝试删除其他用户的经历
- **THEN** 系统返回404 Not Found错误

### Requirement: 经历数据存储格式

系统SHALL使用以下格式存储经历数据到tb_user_experience表：

| 数据库字段 | 存储内容 |
|------------|----------|
| user_id | 当前登录用户ID |
| type | 经历类型枚举（PROJECT/INTERNSHIP） |
| title | 经历名称（项目名/公司名） |
| content | JSON格式的详细内容 |
| start_time | 开始时间 |
| end_time | 结束时间（可选） |

#### Scenario: 项目经历存储格式
- **WHEN** 创建项目经历
- **THEN** title存储项目名称
- **AND** content存储JSON: `{"role": "xxx", "description": "xxx", "techStack": [...], "demoUrl": "xxx"}`

#### Scenario: 实习经历存储格式
- **WHEN** 创建实习经历
- **THEN** title存储公司名称
- **AND** content存储JSON: `{"position": "xxx", "description": "xxx", "achievements": [...], "status": "xxx"}`

### Requirement: 经历返回数据格式

系统SHALL返回以下格式的经历数据：

#### 项目经历返回格式
```typescript
interface Project {
  id: string;
  name: string;           // 项目名称
  role: string;           // 角色
  startDate: string;      // 开始时间
  endDate?: string;       // 结束时间
  description: string;    // 项目描述
  techStack: string[];    // 技术栈
  demoUrl?: string;       // 演示链接
}
```

#### 实习经历返回格式
```typescript
interface Internship {
  id: string;
  company: string;        // 公司名称
  position: string;       // 实习岗位
  startDate: string;      // 开始时间
  endDate?: string;       // 结束时间
  status: string;         // 状态：active/ended
  description: string;    // 描述
  achievements?: string[]; // 成就列表
}
```

