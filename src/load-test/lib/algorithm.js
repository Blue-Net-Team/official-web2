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

// 100 次一个周期：WA 固定占 25 个四等分点，RE 占不与 WA 重叠的 10 个十等分点，
// TLE 只放在 17/67，剩余 63 个位置均为 AC，避免错误结果集中爆发。
const REALISTIC_OUTCOMES = Object.freeze(Array.from({ length: 100 }, (_, index) => {
  if (index === 17 || index === 67) return 'TLE';
  if (index % 20 === 0 || index % 20 === 5) return 'RE';
  if (index % 4 === 2) return 'WA';
  return 'AC';
}));

/** 100 次为一个周期：AC 63、WA 25、RE 10、TLE 2。 */
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

/** AC 使用当前题自己的标准解；WA 使用不可能等于任何夹具答案的固定错误输出。 */
export function sourceCodeForOutcome(outcome, acceptedSourceCode) {
  switch (outcome) {
    case 'AC':
      if (!acceptedSourceCode) {
        throw new Error('AC outcome requires the current question standard solution');
      }
      return acceptedSourceCode;
    case 'WA':
      return 'print("load-test-wa")\n';
    case 'TLE':
      return 'while True:\n    pass\n';
    case 'RE':
      return 'raise RuntimeError("load-test expected runtime error")\n';
    default:
      throw new Error(`Unsupported algorithm outcome: ${outcome}`);
  }
}
