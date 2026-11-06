import { check, sleep } from 'k6';
import exec from 'k6/execution';
import { Counter, Trend } from 'k6/metrics';
import { selectAlgorithmOperation, selectQuestion } from '../lib/algorithm.js';
import { assignUser } from '../lib/identity.js';
import { apiGet, apiPost } from '../lib/k6-http.js';
import { makeRunId, runtimeConfig } from '../lib/runtime.js';
import { initializeContext } from '../setup/context.js';

// 创建、完成、失败和轮询分别计量，并按 operation/questionId 标签拆分。
const created = new Counter('algorithm_jobs_created');
const completed = new Counter('algorithm_jobs_completed');
const failed = new Counter('algorithm_jobs_failed');
const polls = new Counter('algorithm_job_polls');
const endToEnd = new Trend('algorithm_job_end_to_end', true);

const config = runtimeConfig();
const preflight = __ENV.PREFLIGHT === 'true';
export const options = {
  setupTimeout: '300s',
  scenarios: {
    algorithm: {
      executor: 'shared-iterations',
      vus: preflight ? config.algorithm.questionCount : config.algorithm.vus,
      iterations: preflight ? config.algorithm.questionCount * 2 : config.algorithm.totalJobs,
      maxDuration: preflight ? '4m' : '10m',
    },
  },
  thresholds: {
    checks: ['rate==1'],
    algorithm_jobs_completed: [
      `count==${preflight ? config.algorithm.questionCount * 2 : config.algorithm.totalJobs}`,
    ],
    algorithm_jobs_failed: ['count==0'],
  },
};

/** 初始化多道 READY 算法题并预登录考生，避免初始化任务污染正式判题负载。 */
export function setup() {
  return initializeContext(config, makeRunId());
}

/**
 * 有界轮询本人任务；PENDING/RUNNING/RETRYING 继续等待，SUCCEEDED 成功，其他失败终态抛出异常。
 * 调用方在返回前不会创建下一个任务，因此每 VU 最多一个未终态任务。
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

/** 按确定规则选择题和操作，完成从任务创建到 Judge 真实执行终态的完整闭环。 */
export default function (context) {
  const globalIteration = exec.scenario.iterationInInstance;
  const question = selectQuestion(context.algorithms, globalIteration);
  const operation = preflight
    ? globalIteration % 2 === 0 ? 'run' : 'submit'
    : selectAlgorithmOperation(globalIteration, config.algorithm.runPercent);
  const user = assignUser(context.users, __VU);
  const session = user.session;
  const tags = { operation, questionId: String(question.questionId) };
  const startedAt = Date.now();
  try {
    const body = operation === 'run'
      ? {
          questionId: question.questionId,
          language: 'python',
          sourceCode: question.sourceCode,
          testcaseType: 'DEFAULT_RUN',
          customInput: null,
        }
      : {
          questionId: question.questionId,
          language: 'python',
          content: question.sourceCode,
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
    waitForJob(context, session, jobId, operation, question.questionId);
    completed.add(1, tags);
    endToEnd.add(Date.now() - startedAt, tags);
    check(result.payload, { 'algorithm run/submit reached SUCCEEDED': () => true });
  } catch (error) {
    console.error(`algorithm ${operation} failed for question ${question.questionId}: ${error.message}`);
    failed.add(1, tags);
    endToEnd.add(Date.now() - startedAt, { ...tags, outcome: 'failed' });
    check(null, { 'algorithm run/submit reached SUCCEEDED': () => false });
  }
}
