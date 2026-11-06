import crypto from 'k6/crypto';
import http from 'k6/http';
import { requireBusiness } from './http.js';
import { apiGet } from './k6-http.js';

/** 与前端登录约定一致：先计算原始密码的 SHA256，后端再执行 BCrypt 校验。 */
export function passwordHash(rawPassword) {
  return crypto.sha256(rawPassword, 'hex');
}

/**
 * 通过真实学号登录接口建立会话。
 * 返回的 authToken 仅保留在当前 k6 进程内存中，用于 setup 向 VU 传递预登录会话；禁止写入报告。
 */
export function login(baseUrl, studentId, rawPassword) {
  const response = http.post(
    `${baseUrl}/api/v1/auth/login/student-id`,
    JSON.stringify({ studentId, password: passwordHash(rawPassword) }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'login' } },
  );
  const result = requireBusiness(response, 200, 'login');
  if (!result.data?.csrfToken) {
    throw new Error('login: missing CSRF token');
  }
  const authToken = response.cookies?.auth_token?.[0]?.value;
  if (!authToken) {
    throw new Error('login: missing auth_token cookie');
  }
  return {
    csrfToken: result.data.csrfToken,
    authToken,
    userId: result.data.userInfo?.id,
  };
}

/** 调用 auth/me 验证当前会话未被其他登录撤销，并返回当前认证信息。 */
export function assertSession(baseUrl, session) {
  const result = apiGet(baseUrl, '/api/v1/auth/me', null, 'auth_me', session.authToken);
  if (!result.data?.authenticated) {
    throw new Error('auth_me: session is not authenticated');
  }
  return result.data;
}
