import { check } from 'k6';
import crypto from 'k6/crypto';
import http from 'k6/http';
import { Counter, Trend } from 'k6/metrics';
import { createZipFixture } from '../lib/file-fixture.js';
import { assignUser } from '../lib/identity.js';
import { apiPost, apiPut } from '../lib/k6-http.js';
import { makeRunId, runtimeConfig } from '../lib/runtime.js';
import { initializeContext } from '../setup/context.js';

// API 与对象存储分别计时，防止把 MinIO/RustFS 直传耗时误判为 API 延迟。
const completed = new Counter('file_flows_completed');
const failed = new Counter('file_flows_failed');
const apiDuration = new Trend('file_api_duration', true);
const storageDuration = new Trend('file_storage_duration', true);
const flowDuration = new Trend('file_flow_duration', true);

const config = runtimeConfig();
export const options = __ENV.PREFLIGHT === 'true'
  ? { vus: 1, iterations: 1, setupTimeout: '300s' }
  : {
      setupTimeout: '300s',
      scenarios: {
        file_upload: {
          executor: 'constant-arrival-rate',
          rate: config.file.rate,
          timeUnit: config.file.timeUnit,
          duration: config.file.duration,
          preAllocatedVUs: config.file.preAllocatedVUs,
          maxVUs: config.file.maxVUs,
        },
      },
      thresholds: {
        checks: ['rate==1'],
        file_flows_completed: [`count>=${config.file.flows}`],
        file_flows_failed: ['count==0'],
        dropped_iterations: ['count==0'],
      },
    };

/** 创建完整考核上下文并预登录用户，正式 5 分钟窗口只统计上传答题闭环。 */
export function setup() {
  return initializeContext(config, makeRunId());
}

// 该变量位于 VU 自己的执行上下文：首次 POST 创建答案，后续 PUT 更新答案。
let hasSubmitted = false;

/** 执行 prepare → 直传 → confirm → 答案提交/更新；任何阶段失败都不形成有效人次。 */
export default function (context) {
  const user = assignUser(context.users, __VU);
  const session = user.session;
  const fixture = createZipFixture(context.fileFixture.sizeBytes);
  const startedAt = Date.now();
  let stage = 'prepare';
  try {
    const prepareResponse = apiPost(
      context.baseUrl,
      '/api/v1/file/prepare-upload',
      {
        filename: context.fileFixture.filename,
        type: 'WORK',
        size: fixture.byteLength,
        contentType: context.fileFixture.contentType,
      },
      session.csrfToken,
      'file_prepare',
      200,
      session.authToken,
    );
    apiDuration.add(Date.now() - startedAt, { stage: 'prepare' });
    const prepared = prepareResponse.data;

    stage = 'storage';
    const storageStartedAt = Date.now();
    const uploadResponse = http.put(prepared.uploadUrl, fixture, {
      headers: { 'Content-Type': context.fileFixture.contentType },
      tags: { name: 'file_storage_put' },
    });
    storageDuration.add(Date.now() - storageStartedAt);
    if (uploadResponse.status !== 200) {
      throw new Error(`file_storage_put: expected HTTP 200, got ${uploadResponse.status}`);
    }

    stage = 'confirm';
    const confirmStartedAt = Date.now();
    const confirmed = apiPost(
      context.baseUrl,
      '/api/v1/file/confirm-upload',
      {
        fileId: prepared.fileId,
        callbackToken: prepared.callbackToken,
        md5: crypto.md5(fixture, 'hex'),
        size: fixture.byteLength,
      },
      session.csrfToken,
      'file_confirm',
      200,
      session.authToken,
    );
    apiDuration.add(Date.now() - confirmStartedAt, { stage: 'confirm' });
    if (confirmed.data?.status !== 'ACTIVE') {
      throw new Error(`file_confirm: unexpected file status ${confirmed.data?.status}`);
    }

    stage = hasSubmitted ? 'answer_update' : 'answer_create';
    const operation = hasSubmitted ? 'update' : 'create';
    const answerStartedAt = Date.now();
    const answerBody = { questionId: context.fileQuestionId, fileId: prepared.fileId };
    if (hasSubmitted) {
      apiPut(
        context.baseUrl,
        '/api/v1/assessment-answers',
        answerBody,
        session.csrfToken,
        stage,
        session.authToken,
      );
    } else {
      apiPost(
        context.baseUrl,
        '/api/v1/assessment-answers',
        answerBody,
        session.csrfToken,
        stage,
        200,
        session.authToken,
      );
    }
    apiDuration.add(Date.now() - answerStartedAt, { stage });
    hasSubmitted = true;
    completed.add(1);
    flowDuration.add(Date.now() - startedAt, { operation });
    check(uploadResponse, { 'complete file upload and answer flow': () => true });
  } catch (error) {
    console.error(`file flow failed at ${stage}: ${error.message}`);
    failed.add(1, { stage });
    flowDuration.add(Date.now() - startedAt, { operation: 'failed' });
    check(null, { 'complete file upload and answer flow': () => false });
  }
}
