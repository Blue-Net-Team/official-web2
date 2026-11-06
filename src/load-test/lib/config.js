/** 仅允许显式列出的本机地址，防止配置错误将压力发往远程或生产环境。 */
const LOCAL_HOSTS = new Set(['localhost', '127.0.0.1', '::1', 'host.docker.internal']);

/**
 * 使用正则而非 Node URL API 判断本机地址，因为同一模块同时运行在 Node 测试和 k6 中。
 */
export function isLocalUrl(value) {
  if (typeof value !== 'string') {
    return false;
  }
  const match = value.match(/^https?:\/\/(\[[0-9a-f:]+\]|[^\/:?#]+)(?::\d+)?(?:[\/?#]|$)/i);
  if (!match) {
    return false;
  }
  const hostname = match[1].replace(/^\[|\]$/g, '').toLowerCase();
  return LOCAL_HOSTS.has(hostname);
}

/** 在创建数据或加压前验证公共配置；返回原配置以便调用方继续链式使用。 */
export function validateCommonConfig(config) {
  for (const field of ['baseUrl', 'storagePublicUrl']) {
    if (!isLocalUrl(config[field])) {
      throw new Error(`${field} must use a local isolated environment address`);
    }
  }
  for (const field of ['systemStudentId', 'systemPassword', 'candidatePassword']) {
    if (typeof config[field] !== 'string' || config[field].length === 0) {
      throw new Error(`${field} is required`);
    }
  }
  if (!Number.isInteger(config.userCount) || config.userCount < 1) {
    throw new Error('userCount must be a positive integer');
  }
  if (!Number.isInteger(config.file?.flows) || config.file.flows < 1) {
    throw new Error('file.flows must be a positive integer');
  }
  if (!Number.isInteger(config.algorithm?.questionCount) || config.algorithm.questionCount < 2) {
    throw new Error('algorithm.questionCount must be at least 2');
  }
  return config;
}

/** 确保用户数覆盖最大 VU 数，避免共享账号导致 JWT 相互撤销。 */
export function validateUserCapacity(usersOrCount, maxVUs) {
  const count = Array.isArray(usersOrCount) ? usersOrCount.length : usersOrCount;
  if (!Number.isInteger(count) || count < maxVUs) {
    throw new Error(`Test user pool must contain at least ${maxVUs} users; got ${count}`);
  }
}
