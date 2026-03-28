## Purpose

CSRF (Cross-Site Request Forgery) Token 保护机制，防止跨站请求伪造攻击。

## ADDED Requirements

### Requirement: CSRF Token Generation
The system SHALL generate a cryptographically secure CSRF token for each authenticated session.

#### Scenario: Generate CSRF token on login
- **WHEN** user successfully logs in
- **THEN** the system SHALL generate a random 32-character CSRF token
- **THEN** the token SHALL be returned in response body as `csrfToken`
- **THEN** the token SHALL be set in `csrf_token` cookie with `HttpOnly=false`

#### Scenario: CSRF token in auth me response
- **WHEN** authenticated user requests `GET /auth/me`
- **THEN** the response SHALL include current `csrfToken` in response body
- **THEN** the token SHALL match the value in cookie

### Requirement: CSRF Token Validation Scope
The system SHALL validate CSRF token for state-modifying requests that require authentication.

#### Scenario: POST request with valid CSRF token
- **WHEN** authenticated user sends POST request with header `X-CSRF-Token: <valid_token>`
- **AND** the `csrf_token` cookie matches the header value
- **THEN** the request SHALL be processed normally

#### Scenario: POST request with missing CSRF token
- **WHEN** authenticated user sends POST request without `X-CSRF-Token` header
- **THEN** the system SHALL return `403 Forbidden`
- **THEN** response body SHALL be `{ code: 403, msg: "CSRF Token 无效或缺失", data: null }`

#### Scenario: POST request with invalid CSRF token
- **WHEN** authenticated user sends POST request with `X-CSRF-Token` header that doesn't match cookie
- **THEN** the system SHALL return `403 Forbidden`
- **THEN** response body SHALL be `{ code: 403, msg: "CSRF Token 无效或缺失", data: null }`

#### Scenario: GET request does not require CSRF token
- **WHEN** authenticated user sends GET request without `X-CSRF-Token` header
- **THEN** the request SHALL be processed normally
- **THEN** CSRF validation SHALL be skipped

### Requirement: Public endpoints exempt from CSRF
The system SHALL NOT require CSRF token for public (unauthenticated) endpoints.

#### Scenario: Public POST without CSRF token
- **WHEN** unauthenticated user sends POST request to public endpoint (e.g., `/enrollments`)
- **THEN** CSRF validation SHALL be skipped
- **THEN** the request SHALL be processed normally

#### Scenario: Login endpoint exempt from CSRF
- **WHEN** user sends POST request to `/auth/login` without CSRF token
- **THEN** CSRF validation SHALL be skipped
- **THEN** the request SHALL be processed normally

### Requirement: CSRF Token Cookie Configuration
The system SHALL configure CSRF token cookie with appropriate security settings.

#### Scenario: CSRF cookie settings
- **WHEN** setting `csrf_token` cookie
- **THEN** `HttpOnly` SHALL be `false` (JavaScript needs to read it)
- **THEN** `SameSite` SHALL be `Lax`
- **THEN** `Path` SHALL be `/`
- **THEN** `Secure` SHALL match environment (true in production)

#### Scenario: CSRF cookie cleared on logout
- **WHEN** user logs out
- **THEN** `csrf_token` cookie SHALL be cleared with `Max-Age=0`

### Requirement: CSRF Token Lifecycle
The system SHALL manage CSRF token lifecycle aligned with JWT token.

#### Scenario: CSRF token validity period
- **WHEN** CSRF token is generated
- **THEN** it SHALL be valid for the same duration as the JWT token (12 hours)
- **THEN** when JWT is invalidated (logout), CSRF token SHALL also be invalidated

#### Scenario: Concurrent requests with same CSRF token
- **WHEN** multiple concurrent requests use the same valid CSRF token
- **THEN** all requests SHALL pass CSRF validation
- **THEN** token SHALL NOT be consumed or rotated on each request
