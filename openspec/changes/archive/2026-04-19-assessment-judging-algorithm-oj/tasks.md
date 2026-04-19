## 1. Data Model and Contracts

- [x] 1.1 Add a new Flyway migration for assessment judgement, judge job, and judge case result storage without modifying existing migrations.
- [x] 1.2 Add backend enums for judgement source, judgement status, reviewer type, judge job status, judge case status, and algorithm testcase type.
- [x] 1.3 Add normalized objective result code enum values for AC, WA, TLE, RE, CE, and MLE, with AC/WA shared by all objective questions and TLE/RE/CE/MLE limited to algorithm questions.
- [x] 1.4 Extend algorithm question content value objects with statement metadata, examples, default run testcases, formal testcases, starter code templates, time limit, and memory limit.
- [x] 1.5 Extend answer request/response DTOs to carry programming language for algorithm answers.
- [x] 1.6 Add DTOs for judgement records, manual review requests, assessment pass decisions, algorithm run requests, algorithm submit responses, judge job polling responses, and question statistics responses.

## 2. Backend Judgement Core

- [x] 2.1 Implement repository and domain service support for creating, updating, and querying assessment judgement records.
- [x] 2.2 Implement repository and domain service support for assessment pass decisions scoped to candidate and assessment time.
- [x] 2.3 Persist normalized result code on every automatic judgement.
- [x] 2.4 Add permission-protected APIs for viewing judgement results, manually scoring file upload answers, and setting final pass decisions.
- [x] 2.5 Enforce that only file upload answers can receive manual scores and comments.
- [x] 2.6 Enforce that single choice, multiple choice, and algorithm automatic judgement scores cannot be manually overwritten.

## 3. Objective Question Evaluation

- [x] 3.1 Dispatch answer evaluation by question type after answer create/update.
- [x] 3.2 Implement single choice automatic scoring against the configured correct answer.
- [x] 3.3 Implement multiple choice automatic scoring with exact set matching.
- [x] 3.4 Return automatic judgement results to candidates after single choice and multiple choice submission.
- [x] 3.5 Add backend tests for correct, incorrect, forbidden manual override, and judgement visibility scenarios.

## 4. Algorithm Question Administration

- [x] 4.1 Update backend validation so algorithm questions require at least one starter code template.
- [x] 4.2 Validate that formal testcases and default run testcases include required input and expected output fields.
- [x] 4.3 Sanitize candidate-facing algorithm content so formal testcases are not exposed before a judgement result exists.
- [x] 4.4 Update admin assessment question UI and TypeScript DTOs for examples, default run testcases, formal testcases, starter code templates, and limits.

## 5. Algorithm Run and Judge Queue

- [x] 5.1 Add APIs for algorithm run jobs and algorithm formal submissions.
- [x] 5.2 Validate submitted algorithm language against the question starter code keys.
- [x] 5.3 Persist judge jobs and publish RabbitMQ messages for formal algorithm submissions.
- [x] 5.4 Persist non-scoring run jobs for default testcase and custom input runs.
- [x] 5.5 Add polling API for judge job status and result details.
- [x] 5.6 Add backend tests for language rejection, run default testcase, run custom input, job polling, and hidden testcase detail output.

## 6. Judge Worker and Sandbox

- [x] 6.1 Add an independent Judge Worker component that consumes judge jobs from RabbitMQ.
- [x] 6.2 Implement language adapters for configured languages, starting with the existing ProgrammingLanguage enum.
- [x] 6.3 Execute candidate code in a sandbox with CPU, memory, wall-clock time, process, filesystem, network, and output-size limits.
- [x] 6.4 Record compile errors, runtime errors, time limit exceeded, memory limit exceeded, wrong answer, and accepted results.
- [x] 6.5 Write formal algorithm results back to judge job, case result, and automatic judgement storage.
- [x] 6.6 Keep sandbox, queue, or worker infrastructure failures retryable or flagged for operational review without writing candidate judgement result codes.
- [x] 6.7 Add worker-level tests or integration checks for accepted, wrong answer, compile error, runtime error, timeout, and retryable infrastructure failure cases.

## 7. Question Statistics

- [x] 7.1 Implement backend aggregation for submitted count, accepted count, pass rate, and objective result distribution using each candidate's latest formal judgement.
- [x] 7.2 Exclude algorithm run jobs from formal question statistics.
- [x] 7.3 Add permission-protected question statistics API for administrators and optional candidate-facing aggregate statistics.
- [x] 7.4 Add tests for single choice, multiple choice, algorithm AC/WA/TLE/RE/CE/MLE distributions, infrastructure-failure exclusion, latest judgement selection, and unauthorized access.

## 8. Candidate Frontend

- [x] 8.1 Render algorithm question statement, examples, limits, language selector, and starter code in the candidate question detail page.
- [x] 8.2 Add code editing, default testcase run, and custom input run controls.
- [x] 8.3 Poll run jobs and display default testcase comparison or custom input stdout/stderr results.
- [x] 8.4 Add formal algorithm submit flow, polling, judgement summary, and failed hidden testcase detail display.
- [x] 8.5 Display automatic judgement results for single choice and multiple choice submissions.
- [x] 8.6 Optionally display candidate-facing aggregate pass rate when enabled.
- [x] 8.7 Add frontend type-check and focused UI verification for algorithm and objective judgement paths.

## 9. Review, Statistics, and Decision Frontend

- [x] 9.1 Add member-facing judgement result views for candidate answers within authorized scope.
- [x] 9.2 Add file upload manual scoring and comment UI for team members or higher roles.
- [x] 9.3 Add question statistics display for submitted count, accepted count, pass rate, and ACM result distribution.
- [x] 9.4 Add direction-admin final pass decision UI based on aggregated judgement results.
- [x] 9.5 Hide or disable unauthorized scoring, statistics, and decision actions based on role.

## 10. Verification

- [x] 10.1 Run focused backend tests for assessment answer, question content JSON, judgement, statistics, and algorithm judge services.
- [x] 10.2 Run frontend TypeScript check from the frontend workspace.
- [x] 10.3 Verify RabbitMQ queue wiring and worker consumption in the local or containerized environment.
- [x] 10.4 Verify candidate flows for choice answer auto-judgement, file upload pending manual review, algorithm run, algorithm submit, result polling, and optional pass-rate display.
- [x] 10.5 Verify role boundaries for candidate, member, direction administrator, and super administrator.
