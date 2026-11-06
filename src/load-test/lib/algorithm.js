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
