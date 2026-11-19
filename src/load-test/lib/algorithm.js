/**
 * 按全局迭代序号轮询选择题，保证多题分布确定且不同参数实验可复现。
 * @param {Array<{questionId: number}>} questions 初始化阶段创建的算法题
 * @param {number} globalIteration k6 场景内的全局迭代序号
 */
export function selectQuestion(questions, globalIteration) {
  if (!Array.isArray(questions) || questions.length === 0) {
    throw new Error('No algorithm questions available');
  }
  return questions[globalIteration % questions.length];
}

/**
 * 按百分比均匀地交错分配 run/submit，避免先跑完一种操作再跑另一种。
 * 使用累计目标数的 ceiling 差值决定当前迭代是否为运行任务。
 */
export function selectAlgorithmOperation(globalIteration, runPercent) {
  if (runPercent < 0 || runPercent > 100) {
    throw new Error('runPercent must be between 0 and 100');
  }
  const before = Math.ceil((globalIteration * runPercent) / 100);
  const after = Math.ceil(((globalIteration + 1) * runPercent) / 100);
  return after > before ? 'run' : 'submit';
}

// 8 次一个周期；与 50% run/submit 交错后，两种操作都覆盖四种判题结果。
const OUTCOMES = Object.freeze(['AC', 'WA', 'TLE', 'RE', 'WA', 'AC', 'RE', 'TLE']);

const REALISTIC_OUTCOMES = Object.freeze([
  'AC', 'WA', 'AC', 'RE', 'AC', 'AC', 'WA', 'AC', 'TLE', 'AC',
  'WA', 'AC', 'AC', 'WA', 'AC', 'RE', 'AC', 'AC', 'WA', 'AC',
]);

/** 20 次为一个周期：AC 12、WA 5、RE 2、TLE 1，并交错避免集中爆发。 */
export function selectRealisticOutcome(globalIteration) {
  if (!Number.isInteger(globalIteration) || globalIteration < 0) {
    throw new Error('globalIteration must be a non-negative integer');
  }
  return REALISTIC_OUTCOMES[globalIteration % REALISTIC_OUTCOMES.length];
}

/** 确定性地选择预期判题结果，确保对照实验可复现且结果数量均衡。 */
export function selectAlgorithmOutcome(globalIteration) {
  if (!Number.isInteger(globalIteration) || globalIteration < 0) {
    throw new Error('globalIteration must be a non-negative integer');
  }
  return OUTCOMES[globalIteration % OUTCOMES.length];
}

/** 标准答案保持 AC；仅提交/运行代码按预期结果变化。 */
export function sourceCodeForOutcome(outcome) {
  switch (outcome) {
    case 'AC':
      return 'a, b = map(int, input().split())\nprint(a + b)\n';
    case 'WA':
      return 'print(0)\n';
    case 'TLE':
      return 'while True:\n    pass\n';
    case 'RE':
      return 'raise RuntimeError("load-test expected runtime error")\n';
    default:
      throw new Error(`Unsupported algorithm outcome: ${outcome}`);
  }
}
