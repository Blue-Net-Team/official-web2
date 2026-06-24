## 1. 后端 - 命令与领域契约

- [x] 1.1 `SoftwareResourceCommands` 新增 `SortItemCommand(Long id, Integer sortOrder)` 与 `BatchUpdateSortOrderCommand(List<SortItemCommand> items)` record（参照竞赛）
- [x] 1.2 `SoftwareResourceRepository` 接口新增 `boolean existsById(Long id)`、`void batchUpdateSortOrder(List<SortItem> sortItems)` 及 `record SortItem(Long id, Integer sortOrder)`

## 2. 后端 - TDD：先写测试确认边界

- [x] 2.1 `SoftwareResourceRepositoryImplTest` 新增用例：`existsById` 返回 true/false 两种；`batchUpdateSortOrder` 按 item 数验证调用 `updateSortOrderById`
- [x] 2.2 新增/补充 `SoftwareResourceAppServiceImplTest`：批量排序成功；某 id 不存在时抛 `IllegalArgumentException` 且不更新

## 3. 后端 - 基础设施实现

- [x] 3.1 `SoftwareResourceMapper` 接口新增 `void updateSortOrderById(@Param("id") Long id, @Param("sortOrder") Integer sortOrder)`
- [x] 3.2 `SoftwareResourceMapper.xml` 新增 `<update id="updateSortOrderById">` 更新 `tb_software_resource.sort_order`
- [x] 3.3 `SoftwareResourceRepositoryImpl` 实现 `existsById`（`selectById(id) != null`）与 `batchUpdateSortOrder`（逐条调 mapper）

## 4. 后端 - 应用层

- [x] 4.1 `SoftwareResourceAppService` 接口新增 `batchUpdateSortOrder(BatchUpdateSortOrderCommand command)`
- [x] 4.2 `SoftwareResourceAppServiceImpl` 实现：映射为 `SortItem`，逐项 `existsById` 校验，调用 `repository.batchUpdateSortOrder`，方法标注 `@Transactional`
- [x] 4.3 运行第 2 组单元测试，确认全部通过

## 5. 后端 - 接口层

- [x] 5.1 新建 `api/dto/softwareresource/BatchSortRequestDTO`（含内部 `SortItemDTO`，`@NotEmpty`/`@Valid`/`@NotNull` 校验，`@Schema` 文案为软件资源）
- [x] 5.2 `AdminSoftwareResourceController` 新增 `PUT /sort` 端点，权限 `@RequiresPermission(value = "software-resource:sort", name = "调整软件资源排序", access = AccessLevel.PROTECTED)`，构建 command 调 AppService，返回 `ResponseMessage<Void>`
- [x] 5.3 启动应用，确认 `PermissionScanner` 校验通过（无 `software-resource:sort` 重复）

## 6. 前端 - 服务与页面

- [x] 6.1 `admin-software-resource.service.ts` 新增 `batchUpdateSortOrder(data: BatchSortRequestDTO)`，`PUT /admin/software-resources/sort`（复用现有 `BatchSortRequestDTO` 类型）
- [x] 6.2 `app/admin/resources/page.tsx` 引入 `@dnd-kit` 并复制 `DraggableRow` 组件
- [x] 6.3 新增 `displayList` 乐观状态（`useEffect` 同步后端 `resources`）；表格改为 `DndContext` + `SortableContext` 包裹、`pagination={false}`、独立 `<Pagination>`
- [x] 6.4 新增拖拽手柄列（`HolderOutlined`）与 `handleDragEnd`：`arrayMove` 重排 → 按 `page * PAGE_SIZE + index + 1` 重算 → 调接口 → 失败回滚并 `message.error`
- [x] 6.5 拖拽手柄列与排序逻辑仅在 `isAdmin` 为真时启用（MEMBER 只读）

## 7. 端到端验证

- [x] 7.1 后端 `mvnw clean compile package`，确认编译与测试通过
- [x] 7.2 按需重建后端 Docker 镜像并运行（依赖现有 compose 基础设施）
- [x] 7.3 检查 3000 端口：被占用则用现有前端服务，未占用再 `pnpm dev`
- [x] 7.4 Playwright 打开 `/admin/resources`，以管理员拖拽排序，刷新确认顺序持久化；验证非管理员看不到拖拽手柄
