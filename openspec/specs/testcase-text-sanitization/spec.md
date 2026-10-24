# testcase-text-sanitization Specification

## Purpose
定义算法题测试用例文本（`examples` / `runTestCases` / `testCases` 的 `input` 与 `expectedOutput`，以及考生自定义运行输入）在持久化前的清洗规则：删除不可见格式字符（`Cf` / `Zl` / `Zp`）、把非 ASCII 空格归一为 ASCII 空格、换行统一为 `
`；清洗先于校验执行，且保证幂等与 null 安全。
## Requirements
### Requirement: Sanitization scope

The system SHALL sanitize algorithm question testcase text fields (`examples`, `runTestCases`, and `testCases`, each with `input` and `expectedOutput`) before persisting question content, and SHALL sanitize candidate custom run input before persisting the judge job.

The system MUST NOT sanitize `content`, `inputDescription`, `outputDescription`, `constraints`, `explanation`, or `starterCode`.

#### Scenario: Administrator saves question with testcase text

- **WHEN** an administrator creates or updates an algorithm question whose testcase fields contain invisible characters
- **THEN** the system SHALL persist the testcase fields in sanitized form
- **AND** the system SHALL persist the remaining algorithm content fields unchanged

#### Scenario: Candidate submits custom run input

- **WHEN** a candidate runs algorithm code with custom input containing invisible characters
- **THEN** the system SHALL persist the judge job custom input in sanitized form

### Requirement: Invisible formatting characters are removed

The system SHALL remove all characters of Unicode general category `Cf` (including `U+200B` zero width space, `U+200C`, `U+200D`, `U+2060`, `U+FEFF` byte order mark, and `U+00AD` soft hyphen) together with characters of category `Zl` and `Zp` (`U+2028`, `U+2029`) from sanitized text.

#### Scenario: Zero width space inside a number

- **WHEN** a testcase input contains `12` immediately followed by `U+200B`
- **THEN** the sanitized input SHALL contain `12` with no zero width space

#### Scenario: Byte order mark at the start of a testcase

- **WHEN** a testcase input starts with `U+FEFF`
- **THEN** the sanitized input SHALL start with the first printable character

#### Scenario: Paragraph separator between lines

- **WHEN** a testcase input separates two lines with `U+2028`
- **THEN** the sanitized input SHALL not contain `U+2028`

### Requirement: Non-ASCII spaces become ASCII spaces

The system SHALL replace non-ASCII space separators (`U+00A0`, `U+1680`, `U+2000`–`U+200A`, `U+202F`, `U+205F`, `U+3000`) with a single ASCII space (`U+0020`). The system MUST NOT delete them.

#### Scenario: Non-breaking space between two numbers

- **WHEN** a testcase input contains `12` followed by `U+00A0` followed by `34`
- **THEN** the sanitized input SHALL contain `12` followed by one ASCII space followed by `34`
- **AND** the sanitized input SHALL still contain two space-separated tokens

#### Scenario: Ideographic space between two tokens

- **WHEN** a testcase input contains two tokens separated by `U+3000`
- **THEN** the sanitized input SHALL separate them with one ASCII space

### Requirement: Line endings are normalized while tabs and newlines are preserved

The system SHALL replace `\r\n` and a lone `\r` with `\n`, and SHALL preserve `\t`, `\n`, and ASCII space unchanged.

#### Scenario: Windows line endings

- **WHEN** a testcase input uses `\r\n` line endings
- **THEN** the sanitized input SHALL use `\n` line endings

#### Scenario: Tabs in testcase input

- **WHEN** a testcase input contains tab characters
- **THEN** the sanitized input SHALL retain the tab characters at the same positions

### Requirement: Sanitization precedes testcase validation

The system SHALL sanitize testcase fields before validating that each testcase has a non-empty input and expected output.

#### Scenario: Testcase containing only invisible characters

- **WHEN** an administrator saves an algorithm question with a testcase whose input consists solely of zero width spaces
- **THEN** the system SHALL reject the request with a validation error stating that the testcase must contain input and expected output

### Requirement: Sanitization is idempotent and null-safe

The system SHALL return `null` for a `null` input, an empty string for an empty input, and SHALL return an identical value when sanitization is applied to already sanitized text.

#### Scenario: Sanitizing already sanitized text

- **WHEN** sanitization is applied twice to the same input
- **THEN** the second application SHALL return a value equal to the first result

#### Scenario: Sanitizing null

- **WHEN** sanitization is applied to `null`
- **THEN** the result SHALL be `null`

