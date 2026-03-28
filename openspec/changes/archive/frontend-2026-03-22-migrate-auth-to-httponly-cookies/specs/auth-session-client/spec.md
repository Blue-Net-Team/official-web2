## Purpose

前端认证会话管理，包括登录状态检查、CSRF Token 管理、Cookie 自动携带。

## ADDED Requirements

### Requirement: Axios withCredentials Configuration
The frontend SHALL configure axios to automatically send cookies with requests.

#### Scenario: API client configured with credentials
- **WHEN** creating axios apiClient instance
- **THEN** `withCredentials: true` SHALL be set
- **THEN** all requests SHALL automatically include cookies

#### Scenario: Public client configured with credentials
- **WHEN** creating axios publicClient instance
- **THEN** `withCredentials: true` SHALL be set
- **THEN** public requests SHALL also include cookies (for CSRF token)

### Requirement: CSRF Token Request Interceptor
The frontend SHALL automatically add CSRF token header for state-modifying requests.

#### Scenario: POST request includes CSRF header
- **WHEN** making POST request with apiClient
- **AND** authStore has csrfToken
- **THEN** `X-CSRF-Token` header SHALL be set to csrfToken value

#### Scenario: PUT request includes CSRF header
- **WHEN** making PUT request with apiClient
- **AND** authStore has csrfToken
- **THEN** `X-CSRF-Token` header SHALL be set to csrfToken value

#### Scenario: DELETE request includes CSRF header
- **WHEN** making DELETE request with apiClient
- **AND** authStore has csrfToken
- **THEN** `X-CSRF-Token` header SHALL be set to csrfToken value

#### Scenario: GET request does NOT include CSRF header
- **WHEN** making GET request with apiClient
- **THEN** `X-CSRF-Token` header SHALL NOT be added

#### Scenario: Request without csrfToken
- **WHEN** making POST request but authStore has no csrfToken
- **THEN** request SHALL proceed without `X-CSRF-Token` header

### Requirement: Auth Store State Management
The frontend SHALL manage authentication state without persisting sensitive tokens.

#### Scenario: Login updates auth store
- **WHEN** user logs in successfully
- **THEN** authStore SHALL store `userInfo` in memory
- **THEN** authStore SHALL store `csrfToken` in memory
- **THEN** authStore SHALL set `isAuthenticated: true`
- **THEN** authStore SHALL NOT store any token in localStorage

#### Scenario: Logout clears auth store
- **WHEN** user logs out
- **THEN** authStore SHALL clear `userInfo`
- **THEN** authStore SHALL clear `csrfToken`
- **THEN** authStore SHALL set `isAuthenticated: false`

#### Scenario: Auth store persisted data
- **WHEN** authStore is persisted with Zustand persist middleware
- **THEN** only `userInfo` SHALL be persisted to localStorage
- **THEN** `csrfToken` SHALL NOT be persisted
- **THEN** `token` field SHALL NOT exist in store

### Requirement: Login Status Check on App Init
The frontend SHALL check login status when application initializes.

#### Scenario: App init with valid session
- **WHEN** application initializes
- **THEN** `authService.getAuthMe()` SHALL be called
- **WHEN** response is successful with userInfo
- **THEN** authStore SHALL be updated with userInfo and csrfToken
- **THEN** `isAuthenticated` SHALL be `true`

#### Scenario: App init with expired session
- **WHEN** application initializes
- **THEN** `authService.getAuthMe()` SHALL be called
- **WHEN** response is 401
- **THEN** authStore SHALL be cleared
- **THEN** `isAuthenticated` SHALL be `false`

#### Scenario: App init with cached userInfo
- **WHEN** application initializes
- **AND** localStorage has cached userInfo
- **THEN** cached userInfo SHALL be loaded to authStore immediately (for UI rendering)
- **THEN** `getAuthMe()` SHALL be called asynchronously to verify session

### Requirement: Auth Service API Methods
The frontend SHALL provide auth service methods for new authentication flow.

#### Scenario: Login method returns userInfo and csrfToken
- **WHEN** `authService.login(credentials)` is called
- **THEN** POST request SHALL be sent to `/auth/login` with `withCredentials: true`
- **THEN** response data SHALL include `userInfo` and `csrfToken`
- **THEN** method SHALL return `{ userInfo, csrfToken }`

#### Scenario: Logout method sends CSRF header
- **WHEN** `authService.logout()` is called
- **THEN** POST request SHALL be sent to `/auth/logout` with `X-CSRF-Token` header
- **THEN** request SHALL include `withCredentials: true`

#### Scenario: GetAuthMe method fetches current session
- **WHEN** `authService.getAuthMe()` is called
- **THEN** GET request SHALL be sent to `/auth/me` with `withCredentials: true`
- **THEN** response data SHALL include `userInfo` and `csrfToken`
- **THEN** method SHALL return `{ userInfo, csrfToken }`

### Requirement: 401 Response Handler
The frontend SHALL handle 401 responses appropriately.

#### Scenario: 401 response clears auth state
- **WHEN** apiClient receives 401 response
- **THEN** authStore SHALL be cleared
- **THEN** user SHALL be redirected to `/login` page
- **THEN** localStorage auth data SHALL be cleared

#### Scenario: 403 CSRF error shows message
- **WHEN** apiClient receives 403 with CSRF error message
- **THEN** user SHALL see error message
- **THEN** user MAY be prompted to refresh page
