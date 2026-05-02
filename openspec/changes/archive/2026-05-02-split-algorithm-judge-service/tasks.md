## 1. Build Structure

- [x] 1.1 Add or update the root Maven parent so Backend and Judge Service share Java 21, Spring Boot version management, Spotless, compiler, and Surefire configuration without sharing business code.
- [x] 1.2 Create `src/judge-service` as an independently runnable Spring Boot JAR with its own package namespace, configuration files, logging, and health endpoint.
- [x] 1.3 Configure Judge Service dependencies for PostgreSQL access, RabbitMQ consumption, OSS access, process execution, validation, tests, and observability.
- [x] 1.4 Disable Flyway in Judge Service and document that Backend is the only migration owner.

## 2. Database And Storage Model

- [x] 2.1 Add a Backend Flyway migration for current judge problem configuration, standard solution metadata, testcase metadata, and per-language resource limits.
- [x] 2.2 Add narrow Backend data access and application services for managing generator files, standard solution files, testcase configuration, generated manifest metadata, and language limit confirmation.
- [x] 2.3 Add narrow Judge Service data access for reading judge jobs, question testcases, language limits, and writing only allowed judge job, case result, test data generation, benchmark, and judgement fields.
- [x] 2.4 Configure a dedicated `bluenet-judge` OSS bucket on the shared OSS service so candidates cannot access judge assets through normal business file APIs.

## 3. Admin Test Data Configuration

- [x] 3.1 Add Backend DTOs and validation for uploading generator code, each supported language standard solution, testcase configurations, and benchmark settings.
- [x] 3.2 Generate manifest content from admin configuration and save the generated manifest to the judge OSS bucket.
- [x] 3.3 Persist generator object key/hash, standard solution object keys/hashes, testcase categories, generator arguments, weights, hidden flags, sample flags, and config status.
- [x] 3.4 Add admin APIs and frontend controls for configuring testcase categories including sample, normal, edge, empty, minimum, maximum, large, random, worst case, special, and regression.

## 4. Test Data Generation Service

- [x] 4.1 Add a RabbitMQ task path for test data generation requests that passes only the question/config identifier.
- [x] 4.2 Implement Judge Service manifest loading and OSS download for generator and standard solutions.
- [x] 4.3 Implement sandbox execution for generator code with CPU, wall-time, memory, process, network, output, and generated-file-size limits.
- [x] 4.4 Run the generator once per configured testcase and upload generated `.in` files to the judge bucket.
- [x] 4.5 Run the configured primary standard solution against generated inputs to produce `.out` files and upload them to the judge bucket.
- [x] 4.6 Persist generated testcase records only after all required inputs and outputs are generated successfully.
- [x] 4.7 Mark generation failures without publishing incomplete testcase sets.

## 5. Standard Solution Benchmark

- [x] 5.1 Run each configured language standard solution across the generated testcase set multiple times in the sandbox.
- [x] 5.2 Record p95 time, max time, peak memory, benchmark status, and suggested time limit for each standard solution.
- [x] 5.3 Implement the suggested time formula using multiplier, minimum extra milliseconds, and rounding settings from the generated manifest.
- [x] 5.4 Add Backend APIs and frontend controls for administrators to review and confirm per-language time, memory, and output limits.
- [x] 5.5 Persist confirmed `question_id + language` resource limits and reject formal submissions for languages without confirmed limits.

## 6. Formal Judge Service

- [x] 6.1 Keep Backend submission flow responsible for permission, assessment time, language readiness, answer persistence, `source_code` snapshot creation, judge job creation, and RabbitMQ job id publishing.
- [x] 6.2 Implement Judge Service consumption of formal judge job ids from RabbitMQ.
- [x] 6.3 Load the job source snapshot, current testcase records, and confirmed language limits from the database.
- [x] 6.4 Download formal testcase `.in/.out` files from the judge OSS bucket.
- [x] 6.5 Compile and run candidate code in the sandbox with language-specific time, memory, output, process, network, and filesystem limits.
- [x] 6.6 Compare candidate output with expected output and classify each case as AC, WA, TLE, MLE, RE, or CE.
- [x] 6.7 Persist case results, update judge job status, and create or update the automatic assessment judgement directly in the database.
- [x] 6.8 Flag missing OSS objects, sandbox infrastructure errors, and other operational failures for retry or review without producing a candidate result code.

## 7. Frontend And Polling

- [x] 7.1 Preserve candidate-facing polling through Backend APIs and avoid direct frontend access to Judge Service or the judge OSS bucket.
- [x] 7.2 Update algorithm question detail responses to expose confirmed per-language limits for supported languages.
- [x] 7.3 Update admin views to show generation status, benchmark results, confirmed language limits, and testcase metadata.
- [x] 7.4 Ensure hidden testcase failure details follow the existing candidate visibility behavior after formal judging completes.

## 8. Verification

- [x] 8.1 Add backend tests for manifest generation, config validation, language readiness checks, and migration-backed persistence.
- [x] 8.2 Add Judge Service tests for generator execution, standard solution output generation, benchmark limit suggestion, formal judge classification, and operational failure handling.
- [x] 8.3 Add integration tests covering Backend job creation, RabbitMQ task consumption, OSS testcase access, Judge Service result writing, and Backend polling.
- [x] 8.4 Run backend Maven tests for touched modules and Judge Service tests.
- [x] 8.5 Run frontend type checking and relevant page verification for candidate polling and admin test data configuration.
