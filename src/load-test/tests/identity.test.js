import test from 'node:test';
import assert from 'node:assert/strict';
import { assignUser, makeNumericStudentId } from '../lib/identity.js';

const users = [
  { studentId: '260000000001' },
  { studentId: '260000000002' },
];

test('assigns VUs to stable distinct users', () => {
  assert.equal(assignUser(users, 1), users[0]);
  assert.equal(assignUser(users, 2), users[1]);
  assert.equal(assignUser(users, 2), users[1]);
});

test('never wraps to a shared account when capacity is exhausted', () => {
  assert.throws(() => assignUser(users, 0), /1-based/);
  assert.throws(() => assignUser(users, 3), /No test user/);
});

test('generates unique valid numeric student IDs within a run', () => {
  const first = makeNumericStudentId('run-a', 0);
  const second = makeNumericStudentId('run-a', 1);
  assert.match(first, /^\d{12}$/);
  assert.notEqual(first, second);
  assert.equal(first, makeNumericStudentId('run-a', 0));
  assert.notEqual(first, makeNumericStudentId('run-b', 0));
});
