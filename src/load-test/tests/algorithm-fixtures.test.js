import test from 'node:test';
import assert from 'node:assert/strict';
import { ALGORITHM_FIXTURE_COUNT, algorithmFixture } from '../lib/algorithm-fixtures.js';
import { sourceCodeForOutcome } from '../lib/algorithm.js';

test('provides exactly fifteen distinct algorithm questions', () => {
  assert.equal(ALGORITHM_FIXTURE_COUNT, 15);
  const fixtures = Array.from({ length: 15 }, (_, index) => algorithmFixture(index + 1));
  assert.equal(new Set(fixtures.map((item) => item.title)).size, 15);
  assert.equal(new Set(fixtures.map((item) => item.sourceCode)).size, 15);
  assert.equal(new Set(fixtures.map((item) => item.generatorSource)).size, 15);
  assert.equal(new Set(fixtures.map((item) => item.input)).size, 15);
});

test('every fixture has complete question content and an independent accepted solution', () => {
  for (let index = 1; index <= 15; index += 1) {
    const fixture = algorithmFixture(index);
    assert.equal(fixture.questionIndex, index);
    for (const field of ['statement', 'inputDescription', 'outputDescription', 'constraints', 'expectedOutput', 'generatorSource']) {
      assert.ok(fixture[field], `fixture ${index} missing ${field}`);
    }
    assert.equal(sourceCodeForOutcome('AC', fixture.sourceCode), fixture.sourceCode);
    assert.notEqual(fixture.expectedOutput, 'load-test-wa');
  }
  assert.throws(() => algorithmFixture(0), /between 1 and 15/);
  assert.throws(() => algorithmFixture(16), /between 1 and 15/);
});
