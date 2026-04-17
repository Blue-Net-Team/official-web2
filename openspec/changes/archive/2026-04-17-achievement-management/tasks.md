# 成就管理实现任务

## 任务概述
本变更实现成就管理的超级管理员CRUD功能，遵循DDD四层架构和TDD开发流程。参照竞赛管理模块 (`AdminCompetitionController`) 的实现模式。

---

### Task 1: 创建成就 - 后端实现

#### 测试边界
- **输入条件**：管理员用户（角色等级=4，超级管理员）通过管理接口提交成就创建请求
- **前置状态**：系统正常运行时，文件系统包含有效的NORMAL_IMG类型文件
- **后置状态**：成就记录被创建并持久化到数据库

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-001 | 创建竞赛获奖成就（正常场景） | title="蓝桥杯全国一等奖", type="COMPETITION", relateTo="蓝桥杯", achieveAt="2024-04-15", awardLevel="NATIONAL", awardName="一等奖", fileId=123 | 返回AchievementDTO，包含生成的ID | - |
| TC-002 | 创建论文成就（无奖项信息） | title="基于深度学习的图像识别研究", type="PAPER", relateTo="计算机学报", achieveAt="2024-03-20", fileId=124 | 返回AchievementDTO，awardLevel和awardName为null | - |
| TC-003 | 创建专利成就（无关联项） | title="一种新型数据处理装置", type="PATENT", achieveAt="2024-02-10", fileId=125 | 返回AchievementDTO，relateTo为null | - |
| TC-004 | 参数校验失败 - 标题为空 | title=null, type="COMPETITION", achieveAt="2024-04-15" | - | 抛出IllegalArgumentException，提示"成就标题不能为空" |
| TC-005 | 参数校验失败 - 类型无效 | title="测试", type="INVALID_TYPE", achieveAt="2024-04-15" | - | 抛出IllegalArgumentException，提示"无效的成就类型" |
| TC-006 | 业务规则违反 - 竞赛成就缺少奖项级别 | title="测试", type="COMPETITION", achieveAt="2024-04-15", awardLevel=null | - | 抛出IllegalArgumentException，提示"竞赛成就必须指定奖项级别" |
| TC-007 | 业务规则违反 - 获奖日期为未来日期 | title="测试", type="PAPER", achieveAt="2026-01-01" | - | 抛出IllegalArgumentException，提示"获奖日期不能是未来日期" |
| TC-008 | 资源不存在 - 文件ID无效 | fileId=999999 | - | 抛出IllegalArgumentException，提示"文件不存在" |
| TC-009 | 业务规则违反 - 文件类型不匹配 | fileId=126（非NORMAL_IMG类型） | - | 抛出IllegalArgumentException，提示"文件类型不匹配，期望NORMAL_IMG" |

#### 实现步骤（严格按顺序）
- [x] 编写测试用例（红灯阶段）- `AchievementCreateServiceTest`
- [x] 领域层：扩展 `Achievement` 实体（添加创建方法）和新增 `AchievementDomainService`
- [x] 领域层：扩展 `AchievementRepository` 接口，增加 `save(Achievement)` 方法
- [x] 基础设施层：实现 `AchievementRepositoryImpl.save()` 方法
- [x] 应用层：扩展 `AchievementService` 接口，增加 `createAchievement(CreateAchievementRequestDTO)` 方法
- [x] 应用层：实现 `AchievementServiceImpl.createAchievement()`，包含DTO→VO转换和业务规则验证
- [x] 控制层：创建 `AdminAchievementController` 和 `CreateAchievementRequestDTO`
- [x] 控制层：实现 `POST /api/v1/admin/achievements` 接口
- [x] 运行全部测试（绿灯阶段）
- [x] 重构优化：检查代码风格，提取重复逻辑

---

### Task 2: 更新成就 - 后端实现

#### 测试边界
- **输入条件**：管理员用户（角色等级=4，超级管理员）通过管理接口提交成就更新请求
- **前置状态**：系统中已存在目标成就记录（ID有效）
- **后置状态**：成就记录被更新并持久化到数据库

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-010 | 更新成就基本信息（正常场景） | id=1, title="更新后的标题", relateTo="更新后的关联项" | 返回更新后的AchievementDTO | - |
| TC-011 | 更新竞赛成就奖项信息 | id=2, awardLevel="PROVINCIAL", awardName="二等奖" | 返回更新后的AchievementDTO，奖项信息已更新 | - |
| TC-012 | 更新成就图片 | id=3, fileId=127（有效的NORMAL_IMG文件） | 返回更新后的AchievementDTO，fileId已更新 | - |
| TC-013 | 更新失败 - 成就不存在 | id=999999, title="测试" | - | 抛出IllegalArgumentException，提示"成就不存在" |
| TC-014 | 更新失败 - 业务规则违反 | id=1, type="COMPETITION", awardLevel=null | - | 抛出IllegalArgumentException，提示"竞赛成就必须指定奖项级别" |
| TC-015 | 更新失败 - 无效的文件ID | id=1, fileId=999999 | - | 抛出IllegalArgumentException，提示"文件不存在" |
| TC-016 | 部分字段更新（保持其他字段不变） | id=1, title="仅更新标题" | 返回AchievementDTO，仅标题更新，其他字段保持不变 | - |

#### 实现步骤（严格按顺序）
- [x] 编写测试用例（红灯阶段）- `AchievementUpdateServiceTest`
- [x] 领域层：扩展 `AchievementDomainService`，添加 `updateAchievement(Achievement, UpdateData)` 方法
- [x] 领域层：扩展 `AchievementRepository` 接口，增加 `findById(Long)` 和 `update(Achievement)` 方法
- [x] 基础设施层：实现 `AchievementRepositoryImpl.findById()` 和 `update()` 方法
- [x] 应用层：扩展 `AchievementService` 接口，增加 `updateAchievement(Long, UpdateAchievementRequestDTO)` 方法
- [x] 应用层：实现 `AchievementServiceImpl.updateAchievement()`，包含验证和更新逻辑
- [x] 控制层：创建 `UpdateAchievementRequestDTO`
- [x] 控制层：实现 `PUT /api/v1/admin/achievements/{id}` 接口
- [x] 运行全部测试（绿灯阶段）
- [x] 重构优化：提取验证逻辑，优化代码结构

---

### Task 3: 删除成就 - 后端实现

#### 测试边界
- **输入条件**：管理员用户（角色等级=4，超级管理员）通过管理接口提交成就删除请求
- **前置状态**：系统中已存在目标成就记录（ID有效）
- **后置状态**：成就记录从数据库中被删除

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-017 | 删除单个成就（正常场景） | id=1 | 返回成功响应，无返回数据 | - |
| TC-018 | 删除失败 - 成就不存在 | id=999999 | - | 抛出IllegalArgumentException，提示"成就不存在" |
| TC-019 | 删除后验证 - 再次查询应返回空 | id=1（已删除） | 查询ID=1的成就应返回404 | - |

#### 实现步骤（严格按顺序）
- [x] 编写测试用例（红灯阶段）- `AchievementDeleteServiceTest`
- [x] 领域层：扩展 `AchievementDomainService`，添加 `validateDelete(Achievement)` 方法（预留未来扩展）
- [x] 领域层：扩展 `AchievementRepository` 接口，增加 `deleteById(Long)` 方法
- [x] 基础设施层：实现 `AchievementRepositoryImpl.deleteById()` 方法
- [x] 应用层：扩展 `AchievementService` 接口，增加 `deleteAchievement(Long)` 方法
- [x] 应用层：实现 `AchievementServiceImpl.deleteAchievement()`，包含存在性验证
- [x] 控制层：实现 `DELETE /api/v1/admin/achievements/{id}` 接口
- [x] 运行全部测试（绿灯阶段）
- [x] 重构优化：检查错误处理一致性

---

### Task 4: 成就管理前端页面

#### 测试边界
- **输入条件**：用户访问 `/admin/achievement` 页面
- **前置状态**：用户已登录，后端API服务正常运行
- **后置状态**：页面正确显示，功能可用

#### 测试用例
| 用例ID | 场景 | 输入 | 期望输出/行为 | 异常 |
|--------|------|------|---------------|------|
| TC-020 | 超级管理员访问页面 | 角色等级=4（超级管理员）的用户访问 `/admin/achievement` | 显示成就管理页面，包含列表和操作按钮 | - |
| TC-021 | 权限不足访问 | 角色等级<4的用户访问 `/admin/achievement` | 显示403禁止访问页面 | - |
| TC-022 | 页面加载成就列表 | 进入页面时 | 调用 `GET /api/v1/achievements` 接口，显示成就列表 | - |
| TC-023 | 创建成就操作 | 点击"创建成就"按钮 | 弹出抽屉表单，包含所有必填字段 | - |
| TC-024 | 编辑成就操作 | 点击成就记录旁的"编辑"按钮 | 弹出抽屉表单，预填充该成就的现有信息 | - |
| TC-025 | 删除成就操作 | 点击成就记录旁的"删除"按钮 | 弹出确认对话框，确认后调用删除接口 | - |
| TC-026 | 表单验证 - 前端校验 | 提交表单时缺少必填字段 | 显示红色错误提示，阻止提交 | - |
| TC-027 | 表单验证 - 后端错误处理 | 提交表单时后端返回验证错误 | 显示后端返回的错误信息 | - |

#### 实现步骤（严格按顺序）
- [x] 创建前端API服务：`src/frontend/src/apis/services/admin-achievement.service.ts`
- [x] 创建成就管理页面：`src/frontend/src/app/admin/achievement/page.tsx`
- [x] 创建成就抽屉表单组件：`src/frontend/src/app/admin/achievement/AchievementDrawer.tsx`
- [x] 使用Ant Design组件：日期选择器(DatePicker)、选择器(Select)、输入框(Input)、上传组件(Upload)等
- [x] 更新AdminNav配置：将成就管理的minLevel从3改为4
- [x] 集成权限检查：使用现有AdminLayout的权限控制
- [x] 页面样式优化：确保与现有管理界面风格一致
- [x] 功能测试：手动测试CRUD操作流程
- [x] 错误处理：添加加载状态、错误提示和重试机制
- [x] 图片预览：上传图片后显示图片预览，图片在上、上传按钮在下，宽度自适应
- [x] 修复antd警告：将静态 `message` 替换为 `App.useApp()` 模式，保持与项目其他组件一致

---

## DTO设计规范

### CreateAchievementRequestDTO
参照 `CompetitionRequestDTO` 设计：
```java
@Schema(description = "创建成就请求")
public class CreateAchievementRequestDTO {
    @NotBlank(message = "成就标题不能为空")
    @Size(max = 200, message = "成就标题最多200个字符")
    private String title;
    
    @NotNull(message = "成就类型不能为空")
    private String type; // "PAPER", "PATENT", "COMPETITION"
    
    @Size(max = 100, message = "关联项最多100个字符")
    private String relateTo; // 竞赛名称/期刊名称
    
    @NotNull(message = "获奖日期不能为空")
    @PastOrPresent(message = "获奖日期不能是未来日期")
    private LocalDate achieveAt;
    
    private String awardLevel; // "NATIONAL", "PROVINCIAL", "SCHOOL"
    
    @Size(max = 50, message = "奖项名称最多50个字符")
    private String awardName;
    
    @NotNull(message = "成就图片不能为空")
    private Long fileId;
}
```

### UpdateAchievementRequestDTO
与Create请求类似，但所有字段都是可选的（除了验证规则）。

## 权限配置
- 权限标识：`achievement:create`, `achievement:update`, `achievement:delete`
- 访问级别：`AccessLevel.PROTECTED`（需要超级管理员角色）
- 菜单配置：已存在于 `AdminNav` 中（需要将minLevel从3更新为4）

## 依赖关系
1. Task 1-3（后端）可并行开发，但需按顺序完成每个任务的DDD分层
2. Task 4（前端）依赖后端API完成
3. 所有任务完成后进行集成测试