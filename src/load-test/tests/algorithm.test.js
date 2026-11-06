import test from 'node:test';
import assert from 'node:assert/strict';
import { selectAlgorithmOperation, selectQuestion } from '../lib/algorithm.js';

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
