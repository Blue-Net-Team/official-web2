## Context

当前文件上传下载模块存在不符合DDD规范的实现问题：

**问题1：应用层直接使用Mapper**
- `FileDownloadServiceImpl` 注入了大量 Mapper（FileMapper、UserMapper、AssessmentAnswerMapper等）
- 这些Mapper属于基础设施层细节，不应该在应用层使用

**问题2：应用层直接操作Entity**
- 应用层通过 `fileMapper.selectById()` 直接获取 Entity
- DDD规范要求应用层只对VO操作，不应感知Entity

**问题3：应用层直接调用Repository**
- `FileServiceImpl.uploadQrcode()` 直接调用 `qrcodeRepository.save()`
- 应用层应该通过领域服务完成，而不是直接调用Repository

**问题4：接口层传递领域对象**
- `FileUploadController` 将 `UserVO` 传递给 `FileService.updateUserAvatar()`
- 接口层应该只传递userId，让领域层自己获取VO

## Goals / Non-Goals

**Goals:**
- 重构 `FileServiceImpl`，移除所有Mapper和Repository的直接调用
- 重构 `FileDownloadServiceImpl`，移除所有Mapper注入
- 创建 `QrcodeDomainService` 处理二维码业务逻辑
- 扩展 `FileDomainService` 添加查询方法
- 修改接口层参数，只传递userId而非完整UserVO
- 确保所有业务逻辑下沉到领域层

**Non-Goals:**
- 不修改现有文件存储机制（MinIO）
- 不修改现有数据库表结构
- 不改变现有API接口的外部行为
- 不修改文件权限校验的业务规则

## Decisions

### 1. 移除应用层的Mapper依赖
**决策**: 应用层只依赖领域服务，不直接使用Mapper。

**理由**:
- 遵循DDD分层架构原则
- 应用层不应感知基础设施实现细节
- 便于后续替换存储实现（如从MinIO切换到其他存储）

### 2. 通过领域服务获取VO
**决策**: 应用层需要获取业务对象时，通过领域服务的VO方法。

**理由**:
- 领域服务封装了Repository调用
- 返回的是领域VO，符合DDD规范
- 便于添加业务逻辑和缓存

### 3. 接口层传递userId
**决策**: 接口层只传递userId，不传递领域对象。

**理由**:
- 接口层是外部边界，应该与内部领域模型解耦
- 避免领域对象泄漏到Web层
- 便于后续接口参数变化不影响领域层

### 4. 权限校验下沉到领域层
**决策**: 文件下载权限校验逻辑移到领域服务中。

**理由**:
- 权限校验是业务逻辑的一部分
- 便于复用和测试
- 保持应用层简洁

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| 重构范围大，可能引入bug | 分阶段重构，每阶段验证 |
| 领域服务可能变得臃肿 | 按职责拆分，保持单一职责 |
| 性能可能受影响（多次方法调用） | 在领域服务层添加缓存 |

## Migration Plan

1. **Phase 1**: 扩展 `FileDomainService`，添加查询方法
2. **Phase 2**: 创建 `QrcodeDomainService`
3. **Phase 3**: 重构 `FileServiceImpl`，移除Repository直接调用
4. **Phase 4**: 重构 `FileDownloadServiceImpl`，移除所有Mapper
5. **Phase 5**: 修改 `FileUploadController` 参数
6. **Phase 6**: 编译和测试验证

**回滚策略**: 代码回滚到上一版本。

## Open Questions

<!-- 无遗留问题 -->
