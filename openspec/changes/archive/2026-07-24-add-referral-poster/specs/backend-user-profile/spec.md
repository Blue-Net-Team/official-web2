## MODIFIED Requirements

### Requirement: 用户信息包含微信二维码
系统 SHALL 在 `GET /api/v1/user/info` 响应中返回当前登录用户的微信二维码 URL，并同时返回用户的内推码。

#### Scenario: 用户已设置微信二维码
- **WHEN** 已登录用户调用 `GET /api/v1/user/info`
- **THEN** 响应 SHALL 包含 `wechatQrcode` 字段，值为预签名文件 URL
- **AND** 响应 SHALL 包含 `internalReferralCode` 字段，值为用户内推码

#### Scenario: 用户未设置微信二维码
- **WHEN** 已登录用户调用 `GET /api/v1/user/info` 且该用户 `qrcode_id` 为空
- **THEN** 响应 SHALL 包含 `wechatQrcode` 字段，值为 `null`
- **AND** 响应 SHALL 包含 `internalReferralCode` 字段，值为用户内推码

#### Scenario: 未登录用户获取用户信息
- **WHEN** 未登录用户调用 `GET /api/v1/user/info`
- **THEN** 返回 401 Unauthorized

## ADDED Requirements

### Requirement: 用户资料响应包含内推码
系统 SHALL 在 `GET /api/v1/user/info` 响应中返回当前登录用户的 `internalReferralCode`。

#### Scenario: 成员获取内推码
- **WHEN** 已登录成员调用 `GET /api/v1/user/info`
- **THEN** 响应 SHALL 包含 `internalReferralCode` 字段
- **AND** 字段值 SHALL 与 `tb_user.internal_referral_code` 一致

#### Scenario: 未设置内推码的用户
- **WHEN** 已登录用户调用 `GET /api/v1/user/info` 但该用户 `internal_referral_code` 为空
- **THEN** 响应 SHALL 包含 `internalReferralCode` 字段，值为 `null` 或空字符串

#### Scenario: 未登录用户
- **WHEN** 未登录用户调用 `GET /api/v1/user/info`
- **THEN** 返回 401 Unauthorized
