import { check, sleep } from 'k6';
import exec from 'k6/execution';
import { Counter, Trend } from 'k6/metrics';
import { selectQuestion, selectRealisticOutcome, sourceCodeForOutcome } from '../lib/algorithm.js';
import { login } from '../lib/auth.js';
import { assignUser } from '../lib/identity.js';
import { apiGet, apiPost } from '../lib/k6-http.js';
import { runtimeConfig } from '../lib/runtime.js';

const created = new Counter('formal_burst_jobs_created');
const completed = new Counter('formal_burst_jobs_completed');
const failed = new Counter('formal_burst_jobs_failed');
const polls = new Counter('formal_burst_job_polls');
const outcomes = new Counter('formal_burst_case_outcomes');
const submitLatency = new Trend('formal_burst_submit_latency', true);
const firstNonPending = new Trend('formal_burst_first_non_pending', true);
const endToEnd = new Trend('formal_burst_end_to_end', true);

const config = runtimeConfig();
if (!__ENV.ALGORITHM_CONTEXT_FILE) {
  throw new Error('ALGORITHM_CONTEXT_FILE is required for the FORMAL burst');
}
const preparedContext = JSON.parse(open(__ENV.ALGORITHM_CONTEXT_FILE));

export const options = {
  setupTimeout: '120s',
  scenarios: {
    formalBurst: {
      executor: 'shared-iterations',
      vus: 100,
      iterations: 100,
      maxDuration: '3m',
      gracefulStop: '30s',
    },
  },
  thresholds: {
    checks: ['rate==1'],
    formal_burst_jobs_created: ['count==100'],
    formal_burst_jobs_completed: ['count==100'],
    formal_burst_case_outcomes: ['count==100'],
    formal_burst_jobs_failed: ['count==0'],
  },
};

/** 登录属于准备阶段，不计入集中提交窗口。 */
export function setup() {
  if (preparedContext.users?.length !== 100 || !preparedContext.algorithms?.length) {
    throw new Error('FORMAL burst requires exactly 100 users and prepared algorithm questions');
  }
  for (const question of preparedContext.algorithms) {
    if (question.benchmark?.status !== 'DONE'
      || question.limitMeasurement?.source !== 'judge_benchmark_max'
      || !Number.isFinite(question.timeLimitMs)
      || question.timeLimitMs <= 0) {
      throw new Error(`Algorithm question ${question.questionId} has no measured confirmed limit`);
    }
  }
  return {
    ...preparedContext,
    users: preparedContext.users.map((user) => ({
      ...user,
      session: login(config.baseUrl, user.studentId, config.candidatePassword),
    })),
  };
}

function waitForFormalJob(context, session, jobId, questionId, startedAt) {
  const deadline = startedAt + config.jobTimeoutSeconds * 1000;
  let firstNonPendingRecorded = false;
  while (Date.now() < deadline) {
    sleep(config.pollIntervalMilliseconds / 1000);
    polls.add(1, { questionId: String(questionId) });
    const job = apiGet(
      context.baseUrl,
      `/api/v1/algorithm-judge/jobs/${jobId}`,
      null,
      'formal_burst_poll',
      session.authToken,
    ).data;
    if (job.judgeJobId !== jobId) {
      throw new Error(`FORMAL burst: job ownership/id mismatch for ${jobId}`);
    }
    if (job.status !== 'PENDING' && !firstNonPendingRecorded) {
      firstNonPending.add(Date.now() - startedAt, { questionId: String(questionId) });
      firstNonPendingRecorded = true;
    }
    if (job.status === 'SUCCEEDED') {
      return job;
    }
    if (job.status === 'FAILED_REVIEW_REQUIRED') {
      throw new Error(`FORMAL burst: job ${jobId} requires review`);
    }
  }
  throw new Error(`FORMAL burst: job ${jobId} timed out`);
}

function assertFormalOutcome(job, expectedOutcome) {
  if (expectedOutcome === 'AC') {
    if (job.judgement?.resultCode !== 'AC') {
      throw new Error(`expected formal AC judgement, got ${job.judgement?.resultCode ?? 'missing judgement'}`);
    }
    return;
  }
  const cases = Array.isArray(job.caseResults) ? job.caseResults : [];
  if (cases.length === 0) {
    throw new Error(`expected ${expectedOutcome}, but no formal case results were returned`);
  }
  const actual = cases.map((item) => item.status);
  if (actual.some((status) => status !== expectedOutcome)) {
    throw new Error(`expected ${expectedOutcome}, got ${actual.join(',')}`);
  }
}

/** 每个 VU 只提交一次；100 个 VU 尽量同时进入 FORMAL 提交。 */
export default function (context) {
  const iteration = exec.scenario.iterationInInstance;
  const question = selectQuestion(context.algorithms, iteration);
  const expectedOutcome = selectRealisticOutcome(iteration);
  const user = assignUser(context.users, iteration + 1);
  const session = user.session;
  const tags = { questionId: String(question.questionId), expectedOutcome };
  const startedAt = Date.now();
  try {
    const result = apiPost(
      context.baseUrl,
      '/api/v1/algorithm-judge/submit',
      {
        questionId: question.questionId,
        language: 'python',
        content: sourceCodeForOutcome(expectedOutcome, question.sourceCode),
      },
      session.csrfToken,
      'formal_burst_submit',
      200,
      session.authToken,
    );
    submitLatency.add(Date.now() - startedAt, tags);
    const jobId = result.data?.judgeJobId;
    if (!jobId) {
      throw new Error('FORMAL burst: missing judgeJobId');
    }
    created.add(1, tags);
    const job = waitForFormalJob(context, session, jobId, question.questionId, startedAt);
    assertFormalOutcome(job, expectedOutcome);
    outcomes.add(1, { ...tags, outcome: expectedOutcome });
    completed.add(1, tags);
    endToEnd.add(Date.now() - startedAt, tags);
    check(result.payload, { 'FORMAL burst reached expected outcome': () => true });
  } catch (error) {
    console.error(`FORMAL burst failed for question ${question.questionId}: ${error.message}`);
    failed.add(1, tags);
    endToEnd.add(Date.now() - startedAt, { ...tags, outcome: 'failed' });
    check(null, { 'FORMAL burst reached expected outcome': () => false });
  }
}
