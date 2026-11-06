import test from 'node:test';
import assert from 'node:assert/strict';
import { validateCommonConfig, validateUserCapacity } from '../lib/config.js';
import { BASELINE } from '../config/baseline.js';

const valid = {
  baseUrl: 'http://localhost:8080',
  storagePublicUrl: 'http://localhost:7000',
  systemStudentId: '000000000000',
  systemPassword: 'secret',
  candidatePassword: 'secret',
  ...BASELINE,
};

test('accepts an isolated local baseline', () => {
  assert.doesNotThrow(() => validateCommonConfig(valid));
});

test('rejects non-local API targets', () => {
  assert.throws(() => validateCommonConfig({ ...valid, baseUrl: 'https://example.com' }), /local/);
});

test('rejects an undersized user pool before load', () => {
  assert.throws(() => validateUserCapacity(2, 3), /at least 3/);
  assert.doesNotThrow(() => validateUserCapacity(3, 3));
});

test('validates file baseline shape', () => {
  assert.throws(() => validateCommonConfig({ ...valid, file: { ...valid.file, flows: 0 } }), /flows/);
});
