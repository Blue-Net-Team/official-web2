/**
 * 版本化保守基线：所有 JVM 对照实验必须复用这些负载输入。
 * 对象冻结用于避免 k6 初始化或场景代码意外修改实验参数。
 */
export const BASELINE = Object.freeze({
  direction: 'computer_vision',
  grade: 2026,
  candidateRoleId: 4,
  userCount: 8,
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
    operations: 30,
    rate: 1,
    timeUnit: '1s',
    duration: '30s',
    preAllocatedVUs: 1,
    maxVUs: 4,
  }),
  // 多题均匀分配；运行/提交各半，每个 VU 串行等待任务终态。
  algorithm: Object.freeze({
    questionCount: 3,
    totalJobs: 12,
    runPercent: 50,
    vus: 3,
  }),
});
