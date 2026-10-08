## ADDED Requirements

### Requirement: 压缩包格式白名单拦截
考核答题界面（file_upload 题型）的上传区 SHALL 仅允许上传常见压缩包格式。白名单 MUST 至少包含：`zip`、`rar`、`7z`、`tar`、`gz`、`tar.gz`、`bz2`、`xz`。文件选择器的 `accept` 属性 MUST 按白名单过滤，且 `beforeUpload` MUST 对扩展名进行二次校验；扩展名不在白名单内的文件 MUST 被拒绝上传，并给出明确提示（提示中列出允许的格式类别，如「仅支持 zip、rar、7z 等压缩包格式，请将文件压缩后再上传」），且该文件 MUST NOT 进入上传流程。

#### Scenario: 选择合法压缩包
- **WHEN** 用户通过文件选择器选择 `answers.zip`，或拖拽 `homework.rar` 到上传区
- **THEN** 文件通过校验并进入正常上传流程

#### Scenario: 选择非压缩包文件
- **WHEN** 用户选择或拖拽扩展名为 `.docx`、`.exe`、`.jpg` 等白名单外的文件
- **THEN** 上传被拦截，弹出明确提示说明仅支持压缩包格式，文件不进入上传流程

#### Scenario: 无扩展名文件
- **WHEN** 用户选择或拖拽没有扩展名的文件
- **THEN** 上传被拦截并提示仅支持压缩包格式

### Requirement: 文件夹拖拽拦截
上传区 MUST 检测拖拽条目是否为文件夹。当浏览器支持 `DataTransferItem.webkitGetAsEntry()` 且条目 `isDirectory === true` 时，上传 MUST 被直接拦截，并提示「不支持上传文件夹，请压缩后上传」；文件夹 MUST NOT 进入上传流程。当浏览器不支持该 API 时，系统 MUST 降级为依赖扩展名白名单与空文件检测进行兜底拦截。

#### Scenario: 拖拽文件夹进入上传区
- **WHEN** 用户将文件夹拖入上传区（Chromium/WebKit 内核浏览器）
- **THEN** 弹出「不支持上传文件夹，请压缩后上传」提示，文件夹不进入上传流程

#### Scenario: 不支持 webkitGetAsEntry 的浏览器
- **WHEN** 用户使用 Firefox 等不支持该 API 的浏览器拖拽文件夹
- **THEN** 文件夹被当作 0 字节伪文件处理，因扩展名不在白名单（或内容为空）而被拦截，并提示仅支持压缩包格式

### Requirement: 上传错误信息透传
`customRequest` 捕获上传异常时 SHALL 优先展示底层错误对象的具体 `message`（如「文件校验未通过」「准备上传失败」「上传失败: HTTP 409」等），仅当错误对象无可用 `message` 时才回退到「上传失败，请重试」。错误提示 MUST NOT 被统一吞并为无信息的固定文案。

#### Scenario: 上传过程校验失败
- **WHEN** 文件已通过前端校验，但后端确认阶段返回校验失败（如 MD5 校验不通过）
- **THEN** 弹出的 message 显示具体失败原因（如「文件校验未通过」），而非笼统的「上传失败，请重试」

#### Scenario: 未知异常兜底
- **WHEN** 上传过程抛出无 message 的异常
- **THEN** 弹出兜底文案「上传失败，请重试」

### Requirement: 上传区提示文案与白名单一致
上传区展示的格式提示文案（拖拽区 hint 及格式说明）MUST 与实际白名单一致，MUST NOT 显示「所有文件格式」之类的无限制表述。若未来后端返回题目级 `allowedExtensions`，文案 MUST 以题目配置优先，白名单常量作为缺省值。

#### Scenario: 默认展示
- **WHEN** 后端未返回题目级 allowedExtensions（当前现状）
- **THEN** 拖拽区 hint 显示压缩包白名单格式说明（如「zip, rar, 7z, tar.gz…」）

#### Scenario: 题目配置了白名单
- **WHEN** 后端返回了题目级 allowedExtensions
- **THEN** 提示文案以题目配置为准
