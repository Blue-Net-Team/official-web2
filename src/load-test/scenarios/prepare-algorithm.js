import { makeRunId, runtimeConfig } from '../lib/runtime.js';
import { initializeContext } from '../setup/context.js';

// 只创建考核、考生、算法题并执行标准解 benchmark；default 不提交任何 run/submit。
export const options = {
  setupTimeout: '600s',
  scenarios: {
    prepare: {
      executor: 'shared-iterations',
      exec: 'prepareOnly',
      vus: 1,
      iterations: 1,
      maxDuration: '1s',
    },
  },
};

export function setup() {
  return initializeContext(runtimeConfig(), makeRunId());
}

export function prepareOnly() {}

export function handleSummary(data) {
  const context = data.setup_data;
  if (!context) {
    throw new Error('algorithm preparation returned no context');
  }
  const outputPath = __ENV.ALGORITHM_PREPARE_OUTPUT || 'results/algorithm-prepared-context.json';
  const serializable = {
    ...context,
    users: context.users.map(({ session, password, ...user }) => user),
  };
  return {
    [outputPath]: `${JSON.stringify(serializable, null, 2)}\n`,
    stdout: `Algorithm benchmark preparation written to ${outputPath}; no candidate jobs were submitted.\n`,
  };
}
