## ADDED Requirements

### Requirement: 统一文件上传接口
系统 SHALL 提供单一文件上传接口 `POST /api/v1/file/upload`，接受 `file`（MultipartFile）和 `type`（FileType 枚举）参数，仅负责文件存储（上传到 MinIO + 记录 tb_file），不涉及任何业务逻辑。

#### Scenario: 上传头像文件（未登录用户）
- **WHEN** 未登录用户 POST `/api/v1/file/upload` params=`{file: avatar.jpg, type: AVATAR}`
- **THEN** 文件存储到 MinIO `avatar` 桶
- **AND** tb_file 新增记录 type=AVATAR
- **AND** 返回 200 + FileInfo `{id, name, type}`

#### Scenario: 上传考题作品（已登录用户）
- **WHEN** 已登录用户 POST `/api/v1/file/upload` params=`{file: work.zip, type: WORK}`
- **THEN** 文件存储到 MinIO `work` 桶
- **AND** tb_file 新增记录 type=WORK
- **AND** 返回 200 + FileInfo

#### Scenario: 上传普通图片（管理员）
- **WHEN** 管理员 POST `/api/v1/file/upload` params=`{file: photo.jpg, type: NORMAL_IMG}`
- **THEN** 文件存储到 MinIO `normal-img` 桶
- **AND** tb_file 新增记录 type=NORMAL_IMG
- **AND** 返回 200 + FileInfo

#### Scenario: 上传二维码图片（已登录用户）
- **WHEN** 已登录用户 POST `/api/v1/file/upload` params=`{file: qrcode.png, type: QRCODE}`
- **THEN** 文件存储到 MinIO `qrcode` 桶
- **AND** tb_file 新增记录 type=QRCODE
- **AND** 返回 200 + FileInfo

#### Scenario: 上传考题附件（已登录用户）
- **WHEN** 已登录用户 POST `/api/v1/file/upload` params=`{file: attachment.pdf, type: ASSESSMENT_ATTACHMENT}`
- **THEN** 文件存储到 MinIO `assessment-attachment` 桶
- **AND** tb_file 新增记录 type=ASSESSMENT_ATTACHMENT
- **AND** 返回 200 + FileInfo

#### Scenario: 非 AVATAR 类型未登录被拒绝
- **WHEN** 未登录用户 POST `/api/v1/file/upload` params=`{file: work.zip, type: WORK}`
- **THEN** 返回 401 Unauthorized

#### Scenario: 文件类型参数缺失
- **WHEN** POST `/api/v1/file/upload` params=`{file: test.jpg}`（无 type 参数）
- **THEN** 返回 400 Bad Request

#### Scenario: 文件参数缺失
- **WHEN** POST `/api/v1/file/upload` params=`{type: AVATAR}`（无 file 参数）
- **THEN** 返回 400 Bad Request

### Requirement: 文件名自动生成
系统 SHALL 自动生成文件名，格式为 `{fileType-枚举小写}-{uuid}.{ext}`。

#### Scenario: 文件名生成
- **WHEN** 上传文件 `my-photo.jpg` type=AVATAR
- **THEN** 生成的文件名格式为 `avatar-{uuid}.jpg`

### Requirement: FileService 仅提供纯粹文件操作
`FileService` SHALL 仅提供 `uploadFile(file, type)` 方法进行文件存储，不依赖任何业务领域服务。

#### Scenario: FileService 无业务领域依赖
- **WHEN** 检查 FileServiceImpl 的依赖注入
- **THEN** 不存在对 UserDomainService、AssessmentQuestionDomainService、QrcodeDomainService、IntroduceImageDomainService、CompetitionDomainService 的依赖
