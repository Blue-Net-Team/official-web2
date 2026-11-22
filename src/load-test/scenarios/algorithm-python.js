import { check, sleep } from 'k6';
import exec from 'k6/execution';
import { Counter, Trend } from 'k6/metrics';
import { selectAlgorithmOperation, selectAlgorithmOutcome, selectQuestion, selectRealisticOutcome, sourceCodeForOutcome } from '../lib/algorithm.js';
import { assignUser } from '../lib/identity.js';
import { login } from '../lib/auth.js';
import { apiGet, apiPost } from '../lib/k6-http.js';
import { runtimeConfig } from '../lib/runtime.js';

// 创建、完成、失败、预期结果和轮询分别计量，并按 operation/questionId/outcome 拆分。
const created = new Counter('algorithm_jobs_created');
const completed = new Counter('algorithm_jobs_completed');
const failed = new Counter('algorithm_jobs_failed');
const polls = new Counter('algorithm_job_polls');
const caseOutcomes = new Counter('algorithm_case_outcomes');
const endToEnd = new Trend('algorithm_job_end_to_end', true);

const config = runtimeConfig();
const preflight = __ENV.PREFLIGHT === 'true';
if (!__ENV.ALGORITHM_CONTEXT_FILE) {
  throw new Error('ALGORITHM_CONTEXT_FILE is required; run prepare-algorithm.js and measure_algorithm_limits.py first');
}
const preparedContext = JSON.parse(open(__ENV.ALGORITHM_CONTEXT_FILE));
const arrivalRate = Number(__ENV.ALGORITHM_RATE || config.algorithm.rate);
const arrivalDuration = __ENV.ALGORITHM_DURATION || config.algorithm.duration;
if (!Number.isFinite(arrivalRate) || arrivalRate <= 0) {
  throw new Error('ALGORITHM_RATE must be a positive number');
}

export const options = {
  setupTimeout: '600s',
  scenarios: {
    algorithm: preflight ? {
      executor: 'shared-iterations',
      vus: config.algorithm.questionCount,
      iterations: config.algorithm.questionCount * 2,
      maxDuration: '4m',
    } : {
      executor: 'constant-arrival-rate',
      rate: Math.round(arrivalRate * 2),
      timeUnit: '2s',
      duration: arrivalDuration,
      preAllocatedVUs: config.algorithm.preAllocatedVUs,
      maxVUs: config.algorithm.maxVUs,
      gracefulStop: '30s',
    },
  },
  thresholds: {
    checks: ['rate==1'],
    algorithm_jobs_completed: ['count>0'],
    algorithm_case_outcomes: ['count>0'],
    algorithm_jobs_failed: ['count==0'],
  },
};

/** 只接受已完成真实 benchmark 和管理员确认的上下文，禁止回退到固定限时。 */
export function setup() {
  for (const question of preparedContext.algorithms || []) {
    if (question.benchmark?.status !== 'DONE'
      || question.limitMeasurement?.source !== 'judge_benchmark_max'
      || !Number.isFinite(question.timeLimitMs)
      || question.timeLimitMs <= 0) {
      throw new Error(`Algorithm question ${question.questionId} has no measured confirmed limit`);
    }
  }
  if (!preparedContext.algorithms?.length || !preparedContext.users?.length) {
    throw new Error('Prepared algorithm context requires questions and users');
  }
  return {
    ...preparedContext,
    users: preparedContext.users.map((user) => ({
      ...user,
      session: login(config.baseUrl, user.studentId, config.candidatePassword),
    })),
  };
}

/**
 * 有界轮询本人任务；PENDING/RUNNING/RETRYING 继续等待，SUCCEEDED 成功，其他失败终态抛出异常。
 * 每 VU 同时最多处理一个任务，实际任务到达速率由 constant-arrival-rate 控制。
 */
function waitForJob(context, session, jobId, operation, questionId) {
  const deadline = Date.now() + config.jobTimeoutSeconds * 1000;
  while (Date.now() < deadline) {
    sleep(config.pollIntervalMilliseconds / 1000);
    polls.add(1, { operation, questionId: String(questionId) });
    const result = apiGet(
      context.baseUrl,
      `/api/v1/algorithm-judge/jobs/${jobId}`,
      null,
      `algorithm_${operation}_poll`,
      session.authToken,
    ).data;
    if (result.judgeJobId !== jobId) {
      throw new Error(`algorithm ${operation}: job ownership/id mismatch`);
    }
    if (result.status === 'SUCCEEDED') {
      return result;
    }
    if (result.status === 'FAILED_REVIEW_REQUIRED') {
      throw new Error(`algorithm ${operation}: job ${jobId} requires review`);
    }
  }
  throw new Error(`algorithm ${operation}: job ${jobId} timed out`);
}

/** Judge 任务成功只代表执行完成；还必须校验 AC/WA/TLE/RE 与该样本的预期一致。 */
function assertExpectedOutcome(job, expectedOutcome, operation) {
  // 生产 API 刻意不向考生返回正式 AC 用例；正式通过应校验 judgement.resultCode。
  if (operation === 'submit' && expectedOutcome === 'AC') {
    if (job.judgement?.resultCode !== 'AC') {
      throw new Error(`expected formal AC judgement, got ${job.judgement?.resultCode ?? 'missing judgement'}`);
    }
    return;
  }
  const cases = Array.isArray(job.caseResults) ? job.caseResults : [];
  if (cases.length === 0) {
    throw new Error(`expected ${expectedOutcome}, but no case results were returned`);
  }
  const actual = cases.map((item) => item.status);
  if (actual.some((status) => status !== expectedOutcome)) {
    throw new Error(`expected ${expectedOutcome}, got ${actual.join(',')}`);
  }
}

/** 按确定规则分配题目、用户、操作和预期结果，验证完整判题闭环。 */
export default function (context) {
  const globalIteration = exec.scenario.iterationInInstance;
  const question = selectQuestion(context.algorithms, globalIteration);
  const operation = preflight
    ? globalIteration % 2 === 0 ? 'run' : 'submit'
    : selectAlgorithmOperation(globalIteration, config.algorithm.runPercent);
  const expectedOutcome = preflight
    ? selectAlgorithmOutcome(globalIteration)
    : selectRealisticOutcome(globalIteration);
  const sourceCode = sourceCodeForOutcome(expectedOutcome, question.sourceCode);
  // 到达率模式下按全局样本轮转 100 名在线用户，不把在线人数等同于固定并发执行数。
  const user = assignUser(context.users, (globalIteration % context.users.length) + 1);
  const session = user.session;
  const tags = { operation, questionId: String(question.questionId), expectedOutcome };
  const startedAt = Date.now();
  try {
    const body = operation === 'run'
      ? {
          questionId: question.questionId,
          language: 'python',
          sourceCode: sourceCode,
          testcaseType: 'DEFAULT_RUN',
          customInput: null,
        }
      : {
          questionId: question.questionId,
          language: 'python',
          content: sourceCode,
        };
    const result = apiPost(
      context.baseUrl,
      `/api/v1/algorithm-judge/${operation}`,
      body,
      session.csrfToken,
      `algorithm_${operation}`,
      200,
      session.authToken,
    );
    const jobId = result.data?.judgeJobId;
    if (!jobId) {
      throw new Error(`algorithm_${operation}: missing judgeJobId`);
    }
    created.add(1, tags);
    const job = waitForJob(context, session, jobId, operation, question.questionId);
    assertExpectedOutcome(job, expectedOutcome, operation);
    caseOutcomes.add(1, { ...tags, outcome: expectedOutcome });
    completed.add(1, tags);
    endToEnd.add(Date.now() - startedAt, tags);
    check(result.payload, { 'algorithm reached expected outcome': () => true });
  } catch (error) {
    console.error(`algorithm ${operation} failed for question ${question.questionId}: ${error.message}`);
    failed.add(1, tags);
    endToEnd.add(Date.now() - startedAt, { ...tags, outcome: 'failed' });
    check(null, { 'algorithm reached expected outcome': () => false });
  }
}
