## 1. Configuration

- [x] 1.1 Add system-user configuration section to `application.yml`
- [x] 1.2 Create `SystemUserProperties` class in `infrastructure/config/properties/`

## 2. Initialization Logic

- [x] 2.1 Create `infrastructure/init/` directory
- [x] 2.2 Create `SystemUserInitializer` class implementing `CommandLineRunner`
- [x] 2.3 Implement system user existence check using `UserRepository.findByStudentId()`
- [x] 2.4 Implement user creation with BCrypt password encoding

## 3. Testing

- [x] 3.1 Write unit test for `SystemUserProperties` configuration binding
- [x] 3.2 Write integration test for `SystemUserInitializer` - first startup scenario
- [x] 3.3 Write integration test for `SystemUserInitializer` - existing user scenario

## 4. Documentation

- [x] 4.1 Update project documentation with system user configuration instructions (skipped - documentation not required per project rules)
