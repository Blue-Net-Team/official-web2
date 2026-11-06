import { BASELINE } from '../config/baseline.js';
import { validateCommonConfig } from './config.js';

/** 合并版本化基线与 k6 环境变量，并在返回前执行本地环境和容量安全检查。 */
export function runtimeConfig() {
  return validateCommonConfig({
    ...BASELINE,
    baseUrl: __ENV.API_BASE_URL || 'http://localhost:8080',
    judgeBaseUrl: __ENV.JUDGE_BASE_URL || 'http://localhost:8090',
    storagePublicUrl: __ENV.STORAGE_PUBLIC_URL || 'http://host.docker.internal:7000',
    systemStudentId: __ENV.SYSTEM_USER_STUDENT_ID || '',
    systemPassword: __ENV.SYSTEM_USER_PASSWORD || '',
    candidatePassword: __ENV.CANDIDATE_PASSWORD || 'LoadTestOnly-Local',
    candidateRoleId: Number(__ENV.CANDIDATE_ROLE_ID || BASELINE.candidateRoleId),
  });
}

/** 使用时间戳生成可读且通常唯一的本地实验标识，用于关联所有结果文件。 */
export function makeRunId() {
  return `local-${Date.now().toString(36)}`;
}
