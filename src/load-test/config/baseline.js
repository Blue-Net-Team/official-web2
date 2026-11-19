/**
 * 版本化保守基线：所有 JVM 对照实验必须复用这些负载输入。
 * 对象冻结用于避免 k6 初始化或场景代码意外修改实验参数。
 */
export const BASELINE = Object.freeze({
  direction: 'computer_vision',
  grade: 2026,
  candidateRoleId: 4,
  userCount: 100,
  setupTimeoutSeconds: 180,
  pollIntervalMilliseconds: 1000,
  jobTimeoutSeconds: 120,
  // 文件基线：每 3 秒启动 2 个闭环，5 分钟目标至少 200 个有效人次。
  file: Object.freeze({
    filename: 'load-test.zip',
    contentType: 'application/zip',
    sizeBytes: 1024,
    flows: 200,
    duration: '5m',
    rate: 2,
    timeUnit: '3s',
    preAllocatedVUs: 4,
    maxVUs: 8,
  }),
  // 报名使用固定到达速率，学号由运行 ID 与全局迭代号共同生成。
  enrollment: Object.freeze({
    operations: 300,
    rate: 10,
    timeUnit: '1s',
    duration: '30s',
    preAllocatedVUs: 10,
    maxVUs: 20,
  }),
  // 真实业务模型：100 人在线；默认 2.5 任务/秒，可用环境变量切换到高峰/尖峰。
  algorithm: Object.freeze({
    questionCount: 15,
    runPercent: 500 / 6,
    rate: 2.5,
    duration: '30m',
    preAllocatedVUs: 20,
    maxVUs: 100,
  }),
});
