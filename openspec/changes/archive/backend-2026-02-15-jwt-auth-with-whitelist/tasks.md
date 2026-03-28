## 1. Dependencies and Configuration

- [x] 1.1 Add jjwt dependencies to pom.xml (jjwt-api, jjwt-impl, jjwt-jackson)
- [x] 1.2 Add spring-boot-starter-data-redis dependency to pom.xml
- [x] 1.3 Add JWT configuration properties to application.yml (secret, expiration)
- [x] 1.4 Add Redis configuration to application.yml (host, port, password)
- [x] 1.5 Create JwtProperties configuration class for JWT settings
- [x] 1.6 Create RedisConfig configuration class if needed (skipped - Spring Boot auto-configures Redis)

## 2. JWT Utility Implementation

- [x] 2.1 Create JwtPayload class with userId, jti, issuedAt, expiration fields
- [x] 2.2 Create JwtUtil class with generateToken(userId) method
- [x] 2.3 Implement parseToken(jwtString) method in JwtUtil
- [x] 2.4 Implement getJti(jwtString) method in JwtUtil
- [x] 2.5 Implement validateToken(jwtString) method in JwtUtil
- [x] 2.6 Add error handling for expired/invalid tokens
- [x] 2.7 Write unit tests for JwtUtil (generation, parsing, validation)

## 3. Auth Whitelist Service Implementation

- [x] 3.1 Create AuthTokenService interface with storeToken, validateToken, revokeToken methods
- [x] 3.2 Create AuthTokenServiceImpl using StringRedisTemplate
- [x] 3.3 Implement storeToken(jti, userId) with TTL 12 hours
- [x] 3.4 Implement validateToken(jti) returning Optional<Long>
- [x] 3.5 Implement revokeToken(jti) method
- [x] 3.6 Implement revokeAllUserTokens(userId) for logout-all feature
- [x] 3.7 Write unit tests for AuthTokenServiceImpl

## 4. Login Functionality Implementation

- [x] 4.1 Update StudentIdLoginRequestDTO with studentId and password fields
- [x] 4.2 Modify UserAuthResponseDTO - remove refreshToken field, rename accessToken to token
- [x] 4.3 Create AuthService interface with login method
- [x] 4.4 Create AuthServiceImpl with studentId/password validation logic
- [x] 4.5 Implement password verification using BCrypt or existing hash
- [x] 4.6 Implement token generation and whitelist storage in login method
- [x] 4.7 Implement revoke old token logic on new login
- [x] 4.8 Update AuthController.studentIdLogin() to use AuthService
- [x] 4.9 Write unit tests for AuthServiceImpl
- [x] 4.10 Write integration tests for login endpoint

## 5. JWT Authentication Filter and SecurityContext

- [x] 5.1 Create JwtAuthenticationFilter extending OncePerRequestFilter
- [x] 5.2 Implement token extraction from Authorization header
- [x] 5.3 Implement token validation using JwtUtil and AuthTokenService
- [x] 5.4 Update SecurityContext with current user info after validation
- [x] 5.5 Implement SecurityContext.clear() in finally block to prevent memory leaks
- [x] 5.6 Configure filter in SecurityConfig
- [x] 5.7 Handle missing/invalid token scenarios (return 401)
- [x] 5.8 Write unit tests for JwtAuthenticationFilter
- [x] 5.9 Add JavaDoc to SecurityContext warning about async usage limitations

## 6. Logout Functionality

- [x] 6.1 Add logout endpoint to AuthController
- [x] 6.2 Implement logout logic in AuthService
- [x] 6.3 Extract jti from JWT and revoke from whitelist
- [x] 6.4 Clear SecurityContext on logout
- [x] 6.5 Write tests for logout functionality

## 7. Testing and Validation

- [x] 7.1 Verify all JwtUtil tests pass
- [x] 7.2 Verify all AuthTokenService tests pass
- [x] 7.3 Verify all AuthService tests pass
- [x] 7.4 Run integration tests for login/logout flow
- [x] 7.5 Test token expiration scenario
- [x] 7.6 Test token revocation scenario
- [x] 7.7 Verify SecurityContext is properly updated
- [x] 7.8 Run existing tests to ensure no regression

## 8. Documentation and Cleanup

- [x] 8.1 Update API documentation with new login response format
- [x] 8.2 Add JWT authentication section to README
- [x] 8.3 Document Redis whitelist key format
- [x] 8.4 Document SecurityContext usage guidelines (sync only, async must pass userId explicitly)
- [x] 8.5 Add example code showing correct async method signatures
- [x] 8.6 Review code for TODO comments and remove completed ones
- [x] 8.7 Ensure all new classes have proper JavaDoc
