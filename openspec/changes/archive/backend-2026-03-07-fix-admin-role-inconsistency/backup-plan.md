# 备份和回滚方案

## 1. 备份策略

### 1.1 Git 状态备份
当前 Git 状态已包含所有修改，可作为自然备份点。

**当前分支**: `develop`
**远程跟踪**: `GitHub/develop`
**未提交变更**: 已识别并记录

### 1.2 测试数据备份
项目使用 Testcontainers 进行集成测试，测试数据在内存数据库中，无需物理备份。

**测试数据库**: PostgreSQL Testcontainer
**测试配置文件**: `application-test.yml`
**数据初始化**: Flyway 迁移脚本

### 1.3 代码快照
修复前代码状态已记录在修复清单 (`fix-list.md`) 中，包含：
- 所有硬编码角色字符串的位置
- 具体问题描述
- 修复方案

## 2. 回滚方案

### 2.1 快速回滚（如果修复失败）
如果修复过程中出现严重问题，可快速回滚：

1. **撤销单个文件的修改**
   ```bash
   git checkout -- path/to/file.java
   ```

2. **撤销所有未提交的修改**
   ```bash
   git reset --hard
   ```

3. **创建修复前快照标签**（建议）
   ```bash
   git tag before-role-fix-$(date +%Y%m%d-%H%M%S)
   ```

### 2.2 分阶段回滚
如果部分修复成功但需要回滚特定部分：

1. **仅回滚 FileDownloadServiceImpl 修改**
   ```bash
   git checkout -- src/main/java/com/bluenet/web/application/service/impl/FileDownloadServiceImpl.java
   ```

2. **仅回滚测试文件修改**
   ```bash
   git checkout -- src/test/java/com/bluenet/web/api/controller/v1/enrollment/AdminEnrollControllerIntegrationTest.java
   git checkout -- src/test/java/com/bluenet/web/api/controller/v1/admin/AdminCollegeControllerIntegrationTest.java
   git checkout -- src/test/java/com/bluenet/web/api/controller/v1/admin/AdminCompetitionControllerIntegrationTest.java
   ```

### 2.3 数据库回滚
测试使用内存数据库，重启测试即可重置。

**生产环境注意事项**：
- 生产环境角色数据不受影响（使用正确的角色名称）
- 无需数据库迁移脚本

## 3. 验证检查点

### 3.1 修复前检查点（当前状态）
- [x] 所有测试通过（验证基准）
- [x] 代码扫描完成（修复清单）
- [x] Git 状态记录

### 3.2 修复中检查点
每个阶段完成后运行测试验证：

1. **阶段1后**: 运行 `FileDownloadServiceImpl` 相关测试
2. **阶段2后**: 运行所有集成测试
3. **阶段3后**: 运行完整测试套件

### 3.3 修复后检查点
- [ ] 所有测试通过
- [ ] 代码中无硬编码角色字符串
- [ ] 权限检查逻辑与之前一致

## 4. 风险缓解

### 4.1 低风险修改
- 不改变 API 接口
- 不改变数据库结构
- 仅修改内部实现逻辑

### 4.2 隔离修改
- 分阶段实施，每阶段可独立验证
- 每阶段完成后提交，便于问题定位

### 4.3 测试保障
- 修复前建立测试基准
- 修复过程中持续运行测试
- 修复后完整回归测试

## 5. 应急响应

### 5.1 测试失败
**症状**: 修复后测试失败
**响应**:
1. 分析失败原因
2. 如果问题复杂，回滚到上一阶段
3. 调整修复方案后重试

### 5.2 权限检查逻辑错误
**症状**: 权限检查结果与预期不符
**响应**:
1. 检查 `RoleHierarchy` 工具类逻辑
2. 验证 `RoleType` 枚举值正确性
3. 调试 `hasRoleAtLeast` 方法实现

### 5.3 生产环境问题
**症状**: 修复后生产环境权限问题
**响应**:
1. 立即回滚到修复前版本
2. 分析生产环境日志
3. 修复问题后重新部署

## 6. 备份验证

已执行的备份验证：
- [x] 运行简单单元测试 (`ResponseMessageTest`) 通过
- [x] 运行权限相关测试 (`UserInfoControllerTest`) 通过
- [x] 记录当前 Git 状态
- [x] 创建详细修复清单

## 7. 后续步骤

1. **任务 1.4**: 运行完整测试套件，建立基准
2. **阶段 2**: 开始实施 FileDownloadServiceImpl 重构
3. **持续验证**: 每阶段完成后运行相关测试