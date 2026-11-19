import test from 'node:test';
import assert from 'node:assert/strict';
import { assignUser } from '../lib/identity.js';
import { selectAlgorithmOperation, selectAlgorithmOutcome, selectQuestion, selectRealisticOutcome, sourceCodeForOutcome } from '../lib/algorithm.js';

const questions = [{ questionId: 11 }, { questionId: 22 }, { questionId: 33 }];

test('distributes jobs across all questions deterministically', () => {
  assert.deepEqual(
    [0, 1, 2, 3, 4, 5].map((index) => selectQuestion(questions, index).questionId),
    [11, 22, 33, 11, 22, 33],
  );
  assert.throws(() => selectQuestion([], 0), /No algorithm/);
});

test('alternates run and submit at fifty percent', () => {
  assert.deepEqual(
    [0, 1, 2, 3].map((index) => selectAlgorithmOperation(index, 50)),
    ['run', 'submit', 'run', 'submit'],
  );
});

test('rejects invalid run percentages', () => {
  assert.throws(() => selectAlgorithmOperation(0, -1), /between 0 and 100/);
  assert.throws(() => selectAlgorithmOperation(0, 101), /between 0 and 100/);
});

test('distributes AC WA TLE RE across both run and submit', () => {
  const operations = [0, 1, 2, 3, 4, 5, 6, 7].map((index) => ({
    operation: selectAlgorithmOperation(index, 50),
    outcome: selectAlgorithmOutcome(index),
  }));
  assert.deepEqual(operations.map((item) => item.outcome), ['AC', 'WA', 'TLE', 'RE', 'WA', 'AC', 'RE', 'TLE']);
  for (const operation of ['run', 'submit']) {
    assert.deepEqual(
      new Set(operations.filter((item) => item.operation === operation).map((item) => item.outcome)),
      new Set(['AC', 'WA', 'TLE', 'RE']),
    );
  }
  assert.deepEqual([0, 1, 2, 3].map((index) => sourceCodeForOutcome(selectAlgorithmOutcome(index))).map((source) => source.length > 0), [true, true, true, true]);
  assert.throws(() => selectAlgorithmOutcome(-1), /non-negative/);
  assert.throws(() => sourceCodeForOutcome('UNKNOWN'), /Unsupported/);
});

test('reuses the same user across repeated VU iterations', () => {
  const users = Array.from({ length: 8 }, (_, index) => ({ id: index + 1 }));
  const first = assignUser(users, 3);
  const repeated = assignUser(users, 3);
  assert.equal(first, repeated);
  assert.equal(first.id, 3);
});

test('models realistic outcome distribution and run submit ratio', () => {
  const outcomes = { AC: 0, WA: 0, RE: 0, TLE: 0 };
  for (let index = 0; index < 20; index += 1) {
    outcomes[selectRealisticOutcome(index)] += 1;
  }
  assert.deepEqual(outcomes, { AC: 12, WA: 5, RE: 2, TLE: 1 });
  const operations = [0, 1, 2, 3, 4, 5].map((index) => selectAlgorithmOperation(index, 500 / 6));
  assert.equal(operations.filter((item) => item === 'run').length, 5);
  assert.equal(operations.filter((item) => item === 'submit').length, 1);
});
