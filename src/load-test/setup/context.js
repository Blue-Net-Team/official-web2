import { sleep } from 'k6';
import { login, passwordHash } from '../lib/auth.js';
import { validateUserCapacity } from '../lib/config.js';
import { makeNumericStudentId } from '../lib/identity.js';
import { apiGet, apiPost, apiPut } from '../lib/k6-http.js';
import { algorithmFixture } from '../lib/algorithm-fixtures.js';

/** 生成后端 LocalDateTime 可接受的本地 ISO 字符串，不带 UTC 的 Z 后缀。 */
function localDateTime(date) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

/**
 * 创建单道算法题并完成配置、测试数据生成和真实 Python benchmark。
 * 只有 READY 且主标准解 DONE 才返回测量结果；资源限制由独立 Python 工具确认。
 */
function createAlgorithmQuestion(config, session, assessmentTimeId, fixture) {
  const created = apiPost(
    config.baseUrl,
    '/api/v1/admin/assessment-questions',
    {
      assessmentTimeId,
      questionNo: fixture.questionIndex + 1,
      questionType: 'algorithm',
      title: `压测 Python ${fixture.questionIndex}：${fixture.title}`,
      content: {
        type: 'algorithm',
        content: fixture.statement,
        inputDescription: fixture.inputDescription,
        outputDescription: fixture.outputDescription,
        constraints: fixture.constraints,
        examples: [{ input: fixture.input, expectedOutput: fixture.expectedOutput }],
        runTestCases: [{ input: fixture.input, expectedOutput: fixture.expectedOutput, hidden: false, weight: 1 }],
        starterCode: { python: fixture.sourceCode },
      },
      score: 10,
    },
    session.csrfToken,
    'setup_create_algorithm_question',
  );
  const questionId = created.data.id;

  apiPut(
    config.baseUrl,
    `/api/v1/admin/judge/questions/${questionId}/config`,
    {
      generatorLanguage: 'python',
      generatorSource: fixture.generatorSource,
      primaryStandardLanguage: 'python',
      benchmarkRepeatTimes: 5,
      marginMultiplier: 2,
      minExtraMs: 100,
      roundToMs: 100,
      standardSolutions: [{ language: 'python', source: fixture.sourceCode, primarySolution: true }],
      testcases: [
        {
          caseNo: 1,
          category: 'NORMAL',
          generatorArgs: { seed: fixture.questionIndex },
          weight: 1,
          hidden: false,
          sample: true,
          description: `固定轻量用例：${fixture.title}`,
        },
      ],
    },
    session.csrfToken,
    'setup_upsert_judge_config',
  );
  apiPost(
    config.baseUrl,
    `/api/v1/admin/judge/questions/${questionId}/config/generation-tasks`,
    null,
    session.csrfToken,
    'setup_generate_test_data',
  );

  const deadline = Date.now() + config.setupTimeoutSeconds * 1000;
  let status;
  while (Date.now() < deadline) {
    const current = apiGet(
      config.baseUrl,
      `/api/v1/admin/judge/questions/${questionId}/config`,
      null,
      'setup_poll_judge_config',
    ).data;
    status = current.status;
    if (status === 'FAILED') {
      throw new Error(`Algorithm question ${questionId} generation failed`);
    }
    if (status === 'READY') {
      const solution = current.standardSolutions?.find(
        (item) => item.language === 'python' && item.primarySolution === true,
      );
      if (!solution || solution.benchmarkStatus !== 'DONE') {
        throw new Error(`Algorithm question ${questionId} has no completed primary Python benchmark`);
      }
      return {
        questionId,
        ...fixture,
        benchmark: {
          status: solution.benchmarkStatus,
          p95TimeMs: solution.p95TimeMs,
          maxTimeMs: solution.maxTimeMs,
          peakMemoryKb: solution.peakMemoryKb,
          benchmarkRepeatTimes: current.benchmarkRepeatTimes,
        },
      };
    }
    sleep(1);
  }
  throw new Error(`Algorithm question ${questionId} setup timed out in status ${status}`);
}

/** 通过管理员 API 创建本轮独立学院并返回动态 ID。 */
function createCollege(config, admin, runId) {
  return apiPost(
    config.baseUrl,
    '/api/v1/admin/colleges',
    { name: `压测学院-${runId}` },
    admin.csrfToken,
    'setup_create_college',
  ).data;
}

/** 报名只需学院，不创建无关的考核和判题数据。 */
export function initializeEnrollmentContext(config, runId) {
  const admin = login(config.baseUrl, config.systemStudentId, config.systemPassword);
  const college = createCollege(config, admin, runId);
  return { runId, baseUrl: config.baseUrl, collegeId: college.id, workload: config.enrollment };
}

/**
 * 构建文件与算法场景共用的完整上下文：用户池、考核、文件题和多道 READY 算法题。
 * 最后在 setup 中逐用户预登录；会话仅存在内存，报告生成时会移除 k6 setup_data。
 */
export function initializeContext(config, runId) {
  validateUserCapacity(config.userCount, Math.max(config.file.maxVUs, config.algorithm.vus));
  const admin = login(config.baseUrl, config.systemStudentId, config.systemPassword);
  const college = createCollege(config, admin, runId);

  const users = [];
  for (let index = 0; index < config.userCount; index += 1) {
    const studentId = makeNumericStudentId(runId, index);
    const created = apiPost(
      config.baseUrl,
      '/api/v1/admin/users',
      {
        studentId,
        email: `${studentId}@loadtest.invalid`,
        username: `压测考生${index + 1}`,
        password: passwordHash(config.candidatePassword),
        roleId: config.candidateRoleId,
        collegeId: college.id,
        major: '计算机科学与技术',
        direction: config.direction,
        gender: 'unknown',
        assessmentGradeYear: config.grade,
      },
      admin.csrfToken,
      'setup_create_candidate',
    ).data;
    users.push({
      id: created.id,
      studentId,
      password: config.candidatePassword,
      direction: config.direction,
      assessmentGradeYear: config.grade,
    });
  }

  const now = Date.now();
  const assessment = apiPost(
    config.baseUrl,
    '/api/v1/admin/assessment-times',
    {
      direction: config.direction,
      epoch: 1,
      grade: config.grade,
      startTime: localDateTime(new Date(now - 60000)),
      endTime: localDateTime(new Date(now + 24 * 60 * 60 * 1000)),
      timeLimit: false,
      allowTeam: false,
    },
    admin.csrfToken,
    'setup_create_assessment',
  ).data;

  const fileQuestion = apiPost(
    config.baseUrl,
    '/api/v1/admin/assessment-questions',
    {
      assessmentTimeId: assessment.id,
      questionNo: 1,
      questionType: 'file_upload',
      title: '压测文件上传题',
      content: { type: 'file_upload', content: '上传固定 ZIP 作品文件。' },
      score: 10,
    },
    admin.csrfToken,
    'setup_create_file_question',
  ).data;

  const algorithms = [];
  for (let index = 1; index <= config.algorithm.questionCount; index += 1) {
    algorithms.push(createAlgorithmQuestion(config, admin, assessment.id, algorithmFixture(index)));
  }

  const authenticatedUsers = users.map((user) => ({
    ...user,
    session: login(config.baseUrl, user.studentId, user.password),
  }));

  return {
    runId,
    baseUrl: config.baseUrl,
    storagePublicUrl: config.storagePublicUrl,
    collegeId: college.id,
    users: authenticatedUsers,
    assessmentTimeId: assessment.id,
    fileQuestionId: fileQuestion.id,
    algorithms,
    algorithmQuestionIds: algorithms.map((item) => item.questionId),
    workload: {
      file: config.file,
      algorithm: config.algorithm,
    },
    fileFixture: config.file,
    pythonFixtures: algorithms,
  };
}
