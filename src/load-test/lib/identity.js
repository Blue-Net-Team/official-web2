/**
 * 将 k6 的 1-based VU 编号固定映射到独立测试用户；容量不足时失败，绝不循环复用账号。
 */
export function assignUser(users, vu) {
  if (!Number.isInteger(vu) || vu < 1) {
    throw new Error('VU id must be a 1-based positive integer');
  }
  if (!Array.isArray(users) || !users[vu - 1]) {
    throw new Error(`No test user available for VU ${vu}`);
  }
  return users[vu - 1];
}

/** 使用 FNV-1a 将 runId 映射为数字前缀，使同轮 ID 确定、不同轮通常隔离。 */
function hashRunId(runId) {
  let hash = 2166136261;
  for (let index = 0; index < runId.length; index += 1) {
    hash ^= runId.charCodeAt(index);
    hash = Math.imul(hash, 16777619);
  }
  return hash >>> 0;
}

/**
 * 生成符合后端 12 位数字约束的学号：6 位运行前缀 + 6 位全局迭代序号。
 * 全局序号同时避免 constant-arrival-rate 中不同 VU 的局部迭代号冲突。
 */
export function makeNumericStudentId(runId, globalIteration) {
  if (!Number.isInteger(globalIteration) || globalIteration < 0 || globalIteration > 999999) {
    throw new Error('globalIteration must be between 0 and 999999');
  }
  const prefix = String(100000 + (hashRunId(String(runId)) % 900000));
  return `${prefix}${String(globalIteration).padStart(6, '0')}`;
}
