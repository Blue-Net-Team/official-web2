import test from 'node:test';
import assert from 'node:assert/strict';
import { evaluateBusinessResponse } from '../lib/http.js';

test('accepts matching HTTP and business status', () => {
  const result = evaluateBusinessResponse({ status: 201, body: '{"code":201,"data":{"id":7}}' }, 201);
  assert.equal(result.ok, true);
  assert.deepEqual(result.data, { id: 7 });
});

test('rejects HTTP 200 with business failure', () => {
  const result = evaluateBusinessResponse({ status: 200, body: '{"code":409,"msg":"conflict"}' }, 200);
  assert.equal(result.ok, false);
  assert.match(result.error, /business code 409/);
});

test('rejects malformed success response without throwing', () => {
  const result = evaluateBusinessResponse({ status: 200, body: 'not-json' }, 200);
  assert.equal(result.ok, false);
  assert.match(result.error, /invalid JSON/);
});

test('rejects unexpected HTTP status before accepting payload', () => {
  const result = evaluateBusinessResponse({ status: 500, body: '{"code":200}' }, 200);
  assert.equal(result.ok, false);
  assert.match(result.error, /HTTP 500/);
});
