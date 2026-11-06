import { check } from 'k6';
import exec from 'k6/execution';
import { Counter, Trend } from 'k6/metrics';
import { evaluateBusinessResponse } from '../lib/http.js';
import http from 'k6/http';
import { makeNumericStudentId } from '../lib/identity.js';
import { makeRunId, runtimeConfig } from '../lib/runtime.js';
import { initializeEnrollmentContext } from '../setup/context.js';

// 业务指标只统计真实新建结果；HTTP 200/409 不会混淆为有效报名。
const created = new Counter('enrollment_created');
const conflicts = new Counter('enrollment_conflicts');
const failed = new Counter('enrollment_failed');
const businessDuration = new Trend('enrollment_business_duration', true);

const config = runtimeConfig();
export const options = __ENV.PREFLIGHT === 'true'
  ? { vus: 1, iterations: 1 }
  : {
      scenarios: {
        enrollment: {
          executor: 'constant-arrival-rate',
          rate: config.enrollment.rate,
          timeUnit: config.enrollment.timeUnit,
          duration: config.enrollment.duration,
          preAllocatedVUs: config.enrollment.preAllocatedVUs,
          maxVUs: config.enrollment.maxVUs,
        },
      },
      thresholds: {
        checks: ['rate==1'],
        enrollment_created: [`count>=${config.enrollment.operations}`],
        enrollment_conflicts: ['count==0'],
      },
    };

/** 在正式计时前创建报名所需学院，避免把管理员初始化请求计入报名负载。 */
export function setup() {
  return initializeEnrollmentContext(config, makeRunId());
}

/** 每个全局迭代创建一个独立报名，首轮明确只测新建而不测覆盖更新。 */
export default function (context) {
  const globalIteration = exec.scenario.iterationInInstance;
  const studentId = makeNumericStudentId(context.runId, globalIteration);
  const response = http.post(
    `${context.baseUrl}/api/v1/enrollments`,
    JSON.stringify({
      username: `压测报名${globalIteration}`,
      studentId,
      email: `${studentId}@loadtest.invalid`,
      collegeId: context.collegeId,
      major: '计算机科学与技术',
      gender: 'unknown',
      direction: 'computer_vision',
      introduction: '这是用于隔离环境内存压测的固定自我介绍内容。'.repeat(5),
      forceUpdate: false,
    }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'enrollment_create' } },
  );
  const result = evaluateBusinessResponse(response, 201);
  businessDuration.add(response.timings.duration);
  if (result.ok && result.data?.id) {
    created.add(1);
  } else if (response.status === 409) {
    conflicts.add(1);
  } else {
    failed.add(1);
  }
  check(response, {
    'new enrollment created with a returned id': () => result.ok && Boolean(result.data?.id),
  });
}
