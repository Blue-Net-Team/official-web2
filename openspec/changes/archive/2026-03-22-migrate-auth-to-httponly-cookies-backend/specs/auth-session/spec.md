## Purpose

基于 HttpOnly Cookie 的认证会话管理，提供安全的 JWT 存储和登录状态检查能力。

## ADDED Requirements

### Requirement: Login sets HttpOnly Cookie
The system SHALL set JWT token as an HttpOnly Cookie upon successful login.

#### Scenario: Successful login sets auth cookie
- **WHEN** user logs in with valid credentials
- **THEN** the system SHALL generate a JWT token
- **THEN** the system SHALL set `Set-Cookie: auth_token=<jwt>; HttpOnly; Path=/; SameSite=Lax` response header
- **THEN** the system SHALL set `Secure=true` in production environment
- **THEN** the system SHALL set `Domain=.example.com` for cross-subdomain sharing (when configured)

#### Scenario: Login response includes CSRF token
- **WHEN** user logs in successfully
- **THEN** the response body SHALL include `csrfToken` field
- **THEN** the system SHALL also set `csrf_token` cookie for backend verification

### Requirement: Logout clears auth cookie
The system SHALL clear authentication cookies upon logout.

#### Scenario: Logout clears cookies
- **WHEN** user requests logout
- **THEN** the system SHALL remove JWT from whitelist
- **THEN** the system SHALL set `Set-Cookie: auth_token=; Max-Age=0; Path=/` to clear cookie
- **THEN** the system SHALL set `Set-Cookie: csrf_token=; Max-Age=0; Path=/` to clear cookie

### Requirement: Auth me endpoint
The system SHALL provide an endpoint to check current authentication status.

#### Scenario: Authenticated user checks status
- **WHEN** authenticated user requests `GET /auth/me`
- **THEN** the system SHALL return `{ code: 200, data: { userInfo, csrfToken } }`
- **THEN** the system SHALL NOT return the JWT token in response body

#### Scenario: Unauthenticated user checks status
- **WHEN** unauthenticated user requests `GET /auth/me`
- **THEN** the system SHALL return `{ code: 401, msg: "未登录或 token 无效", data: null }`

#### Scenario: Expired token
- **WHEN** user requests `GET /auth/me` with expired JWT cookie
- **THEN** the system SHALL return `{ code: 401, msg: "登录已过期，请重新登录", data: null }`

### Requirement: JWT extraction from Cookie
The system SHALL extract JWT token from Cookie for authentication.

#### Scenario: Extract JWT from Cookie
- **WHEN** request contains `auth_token` cookie
- **THEN** the JwtAuthenticationFilter SHALL extract JWT from cookie value
- **THEN** the system SHALL validate the JWT and set security context

#### Scenario: Fallback to Authorization header (transition period)
- **WHEN** request does NOT contain `auth_token` cookie but contains `Authorization: Bearer <token>` header
- **THEN** the JwtAuthenticationFilter SHALL extract JWT from header (for backward compatibility)

#### Scenario: No valid token
- **WHEN** request contains neither valid cookie nor valid header
- **THEN** the system SHALL NOT set authentication in security context
- **THEN** protected endpoints SHALL return 401

### Requirement: Cookie configuration
The system SHALL support configurable cookie settings.

#### Scenario: Production cookie settings
- **WHEN** running in production profile
- **THEN** `auth_token` cookie SHALL have `Secure=true`
- **THEN** `auth_token` cookie SHALL have `HttpOnly=true`
- **THEN** `auth_token` cookie SHALL have `SameSite=Lax`
- **THEN** cookie domain SHALL be configurable via environment variable

#### Scenario: Development cookie settings
- **WHEN** running in dev profile
- **THEN** `auth_token` cookie SHALL have `Secure=false`
- **THEN** `auth_token` cookie SHALL have `HttpOnly=true`
- **THEN** `auth_token` cookie SHALL have `SameSite=Lax`
- **THEN** cookie domain SHALL NOT be set (localhost)

### Requirement: CORS with credentials
The system SHALL support CORS requests with credentials.

#### Scenario: CORS preflight with credentials
- **WHEN** browser sends CORS preflight request
- **THEN** response SHALL include `Access-Control-Allow-Credentials: true`
- **THEN** response SHALL include specific `Access-Control-Allow-Origin` (not `*`)

#### Scenario: Configured allowed origins
- **WHEN** application starts
- **THEN** CORS allowed origins SHALL be loaded from `CORS_ALLOWED_ORIGINS` environment variable
- **THEN** multiple origins SHALL be supported (comma-separated)
