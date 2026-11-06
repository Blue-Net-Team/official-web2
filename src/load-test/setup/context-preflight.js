import { check } from 'k6';
import { assertSession, login } from '../lib/auth.js';
import { apiGet } from '../lib/k6-http.js';
import { assignUser } from '../lib/identity.js';
import { makeRunId, runtimeConfig } from '../lib/runtime.js';
import { initializeContext } from './context.js';

export const options = { vus: 2, iterations: 2, setupTimeout: '300s' };

/** 通过真实管理 API 创建完整测试上下文。 */
export function setup() {
  const config = runtimeConfig();
  return { context: initializeContext(config, makeRunId()) };
}

/** 两个 VU 使用不同考生登录并验证题目可见性，覆盖账号隔离和会话稳定性。 */
export default function ({ context }) {
  const user = assignUser(context.users, __VU);
  const session = login(context.baseUrl, user.studentId, user.password);
  assertSession(context.baseUrl, session);
  const questions = apiGet(
    context.baseUrl,
    `/api/v1/assessment-questions?assessmentTimeId=${context.assessmentTimeId}&page=0&size=20`,
    null,
    'preflight_candidate_questions',
  ).data;
  check(questions, {
    'candidate sees file and all algorithm questions': (value) =>
      value.questions?.totalElements === 1 + context.algorithmQuestionIds.length,
  });
}
