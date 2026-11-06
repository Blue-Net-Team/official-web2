import test from 'node:test';
import assert from 'node:assert/strict';
import { createZipFixture } from '../lib/file-fixture.js';

test('creates a deterministic fixed-size ZIP fixture with magic bytes', () => {
  const bytes = new Uint8Array(createZipFixture(1024));
  assert.equal(bytes.length, 1024);
  assert.deepEqual(Array.from(bytes.slice(0, 4)), [0x50, 0x4b, 0x03, 0x04]);
  assert.deepEqual(createZipFixture(1024), createZipFixture(1024));
});

test('rejects sizes that cannot hold an empty ZIP record', () => {
  assert.throws(() => createZipFixture(21), /at least 22/);
});
