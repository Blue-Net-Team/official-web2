/**
 * 同时校验 HTTP 状态与 ResponseMessage.code，防止把业务失败的 HTTP 200 计为成功。
 * 该纯函数不依赖 k6，便于使用 Node 测试覆盖异常响应。
 */
export function evaluateBusinessResponse(response, expectedStatus = 200) {
  if (response.status !== expectedStatus) {
    return { ok: false, error: `expected HTTP ${expectedStatus}, got HTTP ${response.status}` };
  }

  let payload;
  try {
    payload = typeof response.body === 'string' ? JSON.parse(response.body) : response.body;
  } catch {
    return { ok: false, error: 'invalid JSON response body' };
  }

  if (!payload || payload.code !== expectedStatus) {
    return {
      ok: false,
      error: `expected business code ${expectedStatus}, got business code ${payload?.code ?? 'missing'}`,
      payload,
    };
  }

  return { ok: true, data: payload.data, payload };
}

/** 在校验失败时抛出带业务阶段的异常，成功时返回解析后的 data 与完整 payload。 */
export function requireBusiness(response, expectedStatus, stage) {
  const result = evaluateBusinessResponse(response, expectedStatus);
  if (!result.ok) {
    throw new Error(`${stage}: ${result.error}`);
  }
  return result;
}
