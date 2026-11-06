import { login, passwordHash } from '../lib/auth.js';
import { apiPost } from '../lib/k6-http.js';
import { runtimeConfig } from '../lib/runtime.js';

export const options = { vus: 1, iterations: 1 };

/** 创建独立 CANDIDATE 监控账号，避免监控与初始化共用管理员会话而相互撤销 JWT。 */
export function setup() {
  const config = runtimeConfig();
  const admin = login(config.baseUrl, config.systemStudentId, config.systemPassword);
  const studentId = __ENV.MONITOR_USER_STUDENT_ID;
  apiPost(
    config.baseUrl,
    '/api/v1/admin/users',
    {
      studentId,
      email: `${studentId}@loadtest.invalid`,
      username: '压测监控只读用户',
      password: passwordHash(__ENV.MONITOR_USER_PASSWORD),
      roleId: config.candidateRoleId,
      direction: config.direction,
      gender: 'unknown',
      assessmentGradeYear: config.grade,
    },
    admin.csrfToken,
    'setup_create_monitor_user',
  );
}

/** 初始化在 setup 完成，默认函数不需要产生业务负载。 */
export default function () {}
