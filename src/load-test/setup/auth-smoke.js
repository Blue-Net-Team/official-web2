import { check } from 'k6';
import { assertSession, login } from '../lib/auth.js';
import { runtimeConfig } from '../lib/runtime.js';

export const options = { vus: 1, iterations: 1 };

/** 只读取运行配置，不在 setup 登录，因为 setup 与 VU 默认不共享 Cookie Jar。 */
export function setup() {
  return { config: runtimeConfig() };
}

/** 在 VU 内登录并立即验证会话，形成最小认证冒烟测试。 */
export default function ({ config }) {
  const session = login(config.baseUrl, config.systemStudentId, config.systemPassword);
  const me = assertSession(config.baseUrl, session);
  check(me, { 'system administrator session is authenticated': (value) => value.authenticated });
}
