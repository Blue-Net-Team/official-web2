## 1. 后端 - Repository 扩展

### Task 1: 扩展 AssessmentQuestionRepository

#### 实现步骤（严格按顺序）
- [x] 编写测试用例（红灯阶段）
- [x] AssessmentQuestionRepository 新增方法：findAllByTimeId、update、deleteById、existsById
- [x] AssessmentQuestionRepositoryImpl 实现新方法
- [x] 运行全部测试（绿灯阶段）
- [x] 重构优化

## 2. 后端 - Domain Service 扩展

### Task 2: 扩展 AssessmentQuestionDomainService

#### 实现步骤（严格按顺序）
- [x] 编写测试用例（红灯阶段）
- [x] AssessmentQuestionDomainService 新增方法签名
- [x] AssessmentQuestionDomainServiceImpl 实现
- [x] 运行全部测试（绿灯阶段）
- [x] 重构优化

## 3. 后端 - Application Service 层

### Task 3: 实现 AssessmentQuestionService 应用服务

#### 实现步骤（严格按顺序）
- [x] 编写测试用例（红灯阶段）
- [x] 创建 DTO：AssessmentQuestionDTO、CreateQuestionRequestDTO、UpdateQuestionRequestDTO
- [x] 创建 AssessmentQuestionConverter（VO ↔ DTO 转换）
- [x] 创建 AssessmentQuestionService 接口
- [x] 创建 AssessmentQuestionServiceImpl 实现
- [x] 运行全部测试（绿灯阶段）
- [x] 重构优化

## 4. 后端 - Controller 层

### Task 4: 实现管理端和用户端 Controller

#### 实现步骤（严格按顺序）
- [x] 编写测试用例（红灯阶段）
- [x] 创建 AdminAssessmentQuestionController（/api/v1/admin/assessment-questions）
- [x] 创建 AssessmentQuestionController（/api/v1/assessment-questions）
- [x] 添加 @RequiresPermission 注解和 Swagger 文档注解
- [x] 运行全部测试（绿灯阶段）
- [x] 重构优化

## 5. 前端 - API Service 和类型定义

### Task 5: 创建前端 API 服务和类型

#### 实现步骤（严格按顺序）
- [x] 在 `types/assessment.ts` 新增 AssessmentQuestionDTO、QuestionType 枚举等类型
- [x] 创建 `apis/services/assessment-question.service.ts`，实现分页查询方法
- [x] 确认类型与后端 DTO 一致

## 6. 前端 - 考题目录页实现

### Task 6: 实现考题目录展示页

#### 实现步骤（严格按顺序）
- [x] 创建 `app/(public)/(other)/assessment/[timeId]/questions/page.tsx`
- [x] 创建对应的 `styles.module.css`（延续暗色毛玻璃风格）
- [x] 实现头部信息（返回导航 + 考核标题 + 状态）
- [x] 实现统计卡片（题目总数、已作答、未作答、总分）
- [x] 实现考题列表行（序号、题目、题型徽章、分值、状态）
- [x] 实现分页控件
- [x] 实现空状态和加载状态

## 7. 前端 - 考核列表页跳转

### Task 7: 修改考核列表页添加跳转

#### 实现步骤（严格按顺序）
- [x] 修改 `assessment/page.tsx` 中 actionButton 添加 onClick 事件
- [x] 进行中/已结束状态点击跳转到 `/assessment/{id}/questions`
- [x] 未开始状态按钮保持禁用
