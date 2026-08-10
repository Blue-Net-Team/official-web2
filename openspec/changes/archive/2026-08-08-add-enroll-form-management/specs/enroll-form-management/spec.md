# 报名表管理（enroll-form-management）

## ADDED Requirements

### Requirement: 报名表文件类型
系统 SHALL 定义 `ENROLL_FORM` 文件类型（存储值 `enroll-form`），用于标识报名表文件。该类型文件的存储路径与生成文件名遵循现有文件模块规则（`enroll_form-<uuid>.<ext>`）。

#### Scenario: 上传报名表文件
- **WHEN** 已登录用户通过预签名直传流程上传文件并指定类型为 `enroll-form`
- **THEN** 系统按现有 prepare-upload → confirm-upload 流程创建文件记录并校验 MD5、大小与魔数
- **AND** 确认通过后文件状态为 ACTIVE

### Requirement: 报名表下载公开可见
系统 SHALL 允许任何人（包括未登录访客）下载 `ENROLL_FORM` 类型的文件。下载权限校验 MUST 将 `ENROLL_FORM` 归入与 `NORMAL_IMG`、`QRCODE` 相同的公开分支，不得落入未知类型的拒绝分支。

#### Scenario: 访客下载报名表
- **WHEN** 未登录用户请求 `GET /api/v1/file/download/{fileId}` 且该文件类型为 `ENROLL_FORM`
- **THEN** 系统返回文件（302 重定向至预签名下载地址），不要求登录

### Requirement: 当前报名表查询
系统 SHALL 提供公开接口 `GET /api/v1/enroll-form` 返回当前报名表信息（`fileId`、`createdAt`，即上传时间，取自 `tb_file.created_at`）。当前报名表 MUST 定义为 `tb_file` 中 `type='enroll-form'` 且 `status='active'` 的最新一条记录（按 id 降序取第一条）。无报名表时接口 SHALL 返回成功且 `data` 为 null。

#### Scenario: 存在报名表
- **WHEN** 任一用户请求 `GET /api/v1/enroll-form` 且存在 ACTIVE 状态的报名表文件
- **THEN** 系统返回最新一条报名表记录的 fileId 与更新时间

#### Scenario: 不存在报名表
- **WHEN** 任一用户请求 `GET /api/v1/enroll-form` 且不存在 ACTIVE 状态的报名表文件
- **THEN** 系统返回成功响应且 data 为 null

#### Scenario: PENDING 或 REJECTED 文件不作为当前报名表
- **WHEN** 存在 `type='enroll-form'` 但状态为 PENDING 或 REJECTED 的文件记录
- **THEN** 查询结果 MUST NOT 包含这些记录

### Requirement: 管理端设置或更新报名表
系统 SHALL 提供管理端接口 `POST /api/v1/admin/enroll-form?fileId=`（PROTECTED，权限标识 `admin:enroll-form:update`），将指定文件设为当前报名表。接口 MUST 按顺序执行：先校验新文件有效性（存在、类型为 `ENROLL_FORM`、状态为 ACTIVE、扩展名为 pdf/doc/docx），校验全部通过后再删除旧报名表文件（数据库记录与对象存储一并删除）。校验失败时 MUST NOT 删除旧报名表。

#### Scenario: 首次设置报名表
- **WHEN** 具备权限的管理员提交已通过上传确认的 `enroll-form` 文件 fileId，且当前无报名表
- **THEN** 该文件成为当前报名表，公开接口可查到

#### Scenario: 更新替换报名表
- **WHEN** 具备权限的管理员提交新的有效 fileId，且已存在另一文件作为当前报名表
- **THEN** 新文件成为当前报名表
- **AND** 旧文件的数据库记录与对象存储对象被删除

#### Scenario: 重复设置同一文件
- **WHEN** 管理员提交的 fileId 已是当前报名表
- **THEN** 操作成功且该文件不被删除

#### Scenario: 文件类型不匹配
- **WHEN** 管理员提交的 fileId 对应文件类型不是 `ENROLL_FORM`
- **THEN** 系统返回 400 错误且旧报名表保持不变

#### Scenario: 文件未激活
- **WHEN** 管理员提交的 fileId 对应文件状态非 ACTIVE（未完成 confirm-upload）
- **THEN** 系统返回 400 错误且旧报名表保持不变

#### Scenario: 扩展名不合法
- **WHEN** 管理员提交的 fileId 对应文件扩展名不在 pdf/doc/docx 范围内
- **THEN** 系统返回 400 错误且旧报名表保持不变

#### Scenario: 无权限访问
- **WHEN** 未登录用户或不具备 `admin:enroll-form:update` 权限的用户请求该接口
- **THEN** 系统返回 401/403 错误

### Requirement: 管理端删除报名表
系统 SHALL 提供管理端接口 `DELETE /api/v1/admin/enroll-form`（PROTECTED，权限标识 `admin:enroll-form:delete`），删除当前报名表（数据库记录与对象存储对象一并删除）。无当前报名表时 SHALL 返回 404。

#### Scenario: 删除当前报名表
- **WHEN** 具备权限的管理员请求删除且当前存在报名表
- **THEN** 该文件记录与对象被删除，公开接口随后返回 data 为 null

#### Scenario: 删除时不存在报名表
- **WHEN** 具备权限的管理员请求删除但当前无报名表
- **THEN** 系统返回 404 错误

#### Scenario: 无权限访问
- **WHEN** 未登录用户或不具备 `admin:enroll-form:delete` 权限的用户请求该接口
- **THEN** 系统返回 401/403 错误

### Requirement: enroll 报名页展示报名表下载入口
enroll 报名页 SHALL 在左侧栏展示「报名表」卡片，包含下载按钮与提示文案「填写完成后请下载打印本报名表，并在面试时带到实验室」。点击下载按钮 SHALL 通过 `GET /file/download/{fileId}` 下载文件。移动端布局 SHALL 同样展示该卡片。公开接口返回 null 时卡片 MUST NOT 渲染。

#### Scenario: 桌面端展示下载卡片
- **WHEN** 访客打开 enroll 报名页且公开接口返回了报名表信息
- **THEN** 左侧栏显示「报名表」卡片，含下载按钮与提示文案

#### Scenario: 移动端展示下载卡片
- **WHEN** 访客在移动端宽度打开 enroll 报名页且公开接口返回了报名表信息
- **THEN** 表单下方区域显示「报名表」卡片

#### Scenario: 点击下载
- **WHEN** 访客点击下载按钮
- **THEN** 浏览器跳转 `/file/download/{fileId}` 开始下载报名表文件

#### Scenario: 无报名表时不渲染
- **WHEN** 公开接口返回 data 为 null
- **THEN** 页面不渲染报名表卡片，其余内容不受影响

### Requirement: admin 后台报名表管理页
系统 SHALL 提供 `/admin/enroll-form` 管理页，展示当前报名表状态（是否已上传、上传时间），并提供【更新】与【删除】操作。更新操作 SHALL 引导管理员选择文件（限制 pdf/doc/docx），走完预签名直传流程后调用设置接口完成替换。侧边栏 SHALL 新增「报名表管理」入口，最低角色等级 minLevel 2。

#### Scenario: 展示当前报名表状态
- **WHEN** 管理员打开 `/admin/enroll-form`
- **THEN** 页面显示当前是否已上传报名表及上传时间

#### Scenario: 更新报名表
- **WHEN** 管理员点击【更新】并选择合法格式文件完成上传
- **THEN** 系统完成直传与确认后调用设置接口替换报名表，页面刷新为最新状态

#### Scenario: 删除报名表
- **WHEN** 管理员点击【删除】并确认
- **THEN** 系统调用删除接口，页面刷新为"尚未上传"状态

#### Scenario: 侧边栏入口
- **WHEN** 角色等级 ≥ 2 的用户查看 admin 侧边栏
- **THEN** 可见「报名表管理」菜单项，点击跳转 `/admin/enroll-form`

#### Scenario: 低等级用户不可见入口
- **WHEN** 角色等级 < 2 的用户查看 admin 侧边栏
- **THEN** 不显示「报名表管理」菜单项
