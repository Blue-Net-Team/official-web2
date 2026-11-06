import http from 'k6/http';
import { evaluateBusinessResponse, requireBusiness } from './http.js';

/** 构造统一 JSON/CSRF/Bearer 请求头；stage 同时作为 k6 指标标签，便于按业务阶段分析。 */
export function headers(csrfToken, tags, authToken) {
  const value = { 'Content-Type': 'application/json' };
  if (csrfToken) {
    value['X-CSRF-Token'] = csrfToken;
  }
  if (authToken) {
    value.Authorization = `Bearer ${authToken}`;
  }
  return { headers: value, tags };
}

/** 发送要求业务 code=200 的 GET 请求。 */
export function apiGet(baseUrl, path, csrfToken, stage, authToken) {
  const response = http.get(`${baseUrl}${path}`, headers(csrfToken, { name: stage }, authToken));
  return requireBusiness(response, 200, stage);
}

/** 发送 JSON POST，并按接口语义校验预期的 HTTP/业务状态（报名为 201，其余通常为 200）。 */
export function apiPost(baseUrl, path, body, csrfToken, stage, expectedStatus = 200, authToken) {
  const response = http.post(
    `${baseUrl}${path}`,
    JSON.stringify(body),
    headers(csrfToken, { name: stage }, authToken),
  );
  return requireBusiness(response, expectedStatus, stage);
}

/** 发送要求业务 code=200 的 JSON PUT 请求。 */
export function apiPut(baseUrl, path, body, csrfToken, stage, authToken) {
  const response = http.put(
    `${baseUrl}${path}`,
    JSON.stringify(body),
    headers(csrfToken, { name: stage }, authToken),
  );
  return requireBusiness(response, 200, stage);
}

/** 用于不需要 ResponseMessage 包装的非业务 HTTP 响应状态检查。 */
export function evaluateRaw(response, expectedStatus, stage) {
  if (response.status !== expectedStatus) {
    return { ok: false, error: `${stage}: expected HTTP ${expectedStatus}, got ${response.status}` };
  }
  return { ok: true };
}

export { evaluateBusinessResponse };
