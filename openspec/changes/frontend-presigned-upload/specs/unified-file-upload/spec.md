## MODIFIED Requirements

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

### Requirement: 预签名上传准备接口
系统 SHALL 提供 `POST /api/v1/file/prepare-upload` 接口，生成预签名 PUT URL 和回调 Token。AVATAR 和 NORMAL_IMG 类型允许匿名调用，其他类型需要登录。

#### Scenario: 已登录用户准备上传考核作品
- **WHEN** 已登录用户 POST `/api/v1/file/prepare-upload` body=`{filename: 'work.zip', type: WORK, size: 10485760, contentType: 'application/zip'}`
- **THEN** 在 tb_file 创建 PENDING 记录
- **AND** 生成预签名 PUT URL（默认 15 分钟有效）
- **AND** 生成 callbackToken
- **AND** 返回 200 + PrepareUploadResponse `{fileId, uploadUrl, callbackToken, filename, type}`

#### Scenario: 匿名用户准备上传头像
- **WHEN** 未登录用户 POST `/api/v1/file/prepare-upload` body=`{filename: 'avatar.jpg', type: AVATAR, size: 1024, contentType: 'image/jpeg'}`
- **THEN** 通过限流检查
- **AND** 返回 200 + PrepareUploadResponse

#### Scenario: 匿名用户准备上传非 AVATAR/NORMAL_IMG 类型被拒绝
- **WHEN** 未登录用户 POST `/api/v1/file/prepare-upload` body=`{filename: 'work.zip', type: WORK, ...}`
- **THEN** 返回 401 Unauthorized

#### Scenario: 匿名用户触发上传限流
- **WHEN** 同一 IP 在短时间内多次调用 `prepare-upload`
- **THEN** 返回 429 Too Many Requests

### Requirement: 预签名上传确认接口
系统 SHALL 提供 `POST /api/v1/file/confirm-upload` 接口，校验文件并激活记录。

#### Scenario: 上传确认成功
- **WHEN** POST `/api/v1/file/confirm-upload` body=`{fileId: 123, callbackToken: 'xxx', md5: 'abc123', size: 1024}`
- **THEN** 校验 callbackToken 有效且匹配 fileId
- **AND** 文件状态为 PENDING
- **AND** OSS 对象存在
- **AND** MD5、大小、魔数校验通过
- **AND** 状态更新为 ACTIVE
- **AND** 返回 200 + ConfirmUploadResponse `{fileId, filename, type, status: ACTIVE}`

#### Scenario: 上传确认失败（MD5 不匹配）
- **WHEN** POST `/api/v1/file/confirm-upload` body=`{fileId: 123, callbackToken: 'xxx', md5: 'wrong', size: 1024}`
- **THEN** 删除 OSS 对象
- **AND** 状态更新为 REJECTED
- **AND** 返回 200 + ConfirmUploadResponse `{fileId, filename, type, status: REJECTED}`

#### Scenario: 重复确认已激活的文件（幂等）
- **WHEN** 文件状态已为 ACTIVE 时再次 POST `/api/v1/file/confirm-upload` 携带有效 Token
- **THEN** 返回 200 + ConfirmUploadResponse `{fileId, filename, type, status: ACTIVE}`
- **AND** 不报错、不修改数据库

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
