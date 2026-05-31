const { chromium } = require('playwright');

const AVATAR_PATH = 'E:/code/code_project/bluenet_web2.2/develop/test-avatar.jpg';
const BASE_URL = 'http://localhost:3000';

const candidates = [
  { name: 'cv_2024_b', studentId: '2024501002001', email: 'test-cv24b@example.com', direction: '计算机视觉', directionValue: 'COMPUTER_VISION', major: '计算机科学与技术', grade: '2024' },
  { name: 'cv_2025_a', studentId: '2025501001001', email: 'test-cv25a@example.com', direction: '计算机视觉', directionValue: 'COMPUTER_VISION', major: '计算机科学与技术', grade: '2025' },
  { name: 'cv_2025_b', studentId: '2025501002001', email: 'test-cv25b@example.com', direction: '计算机视觉', directionValue: 'COMPUTER_VISION', major: '计算机科学与技术', grade: '2025' },
  { name: 'sd_2024_a', studentId: '2024502001001', email: 'test-sd24a@example.com', direction: '结构设计', directionValue: 'STRUCTURAL_DESIGN', major: '土木工程', grade: '2024' },
  { name: 'sd_2024_b', studentId: '2024502002001', email: 'test-sd24b@example.com', direction: '结构设计', directionValue: 'STRUCTURAL_DESIGN', major: '土木工程', grade: '2024' },
  { name: 'sd_2025_a', studentId: '2025502001001', email: 'test-sd25a@example.com', direction: '结构设计', directionValue: 'STRUCTURAL_DESIGN', major: '土木工程', grade: '2025' },
  { name: 'sd_2025_b', studentId: '2025502002001', email: 'test-sd25b@example.com', direction: '结构设计', directionValue: 'STRUCTURAL_DESIGN', major: '土木工程', grade: '2025' },
  { name: 'emb_2024_a', studentId: '2024503001001', email: 'test-em24a@example.com', direction: '嵌入式开发', directionValue: 'EMBEDDED', major: '电子信息工程', grade: '2024' },
  { name: 'emb_2024_b', studentId: '2024503002001', email: 'test-em24b@example.com', direction: '嵌入式开发', directionValue: 'EMBEDDED', major: '电子信息工程', grade: '2024' },
  { name: 'emb_2025_a', studentId: '2025503001001', email: 'test-em25a@example.com', direction: '嵌入式开发', directionValue: 'EMBEDDED', major: '电子信息工程', grade: '2025' },
  { name: 'emb_2025_b', studentId: '2025503002001', email: 'test-em25b@example.com', direction: '嵌入式开发', directionValue: 'EMBEDDED', major: '电子信息工程', grade: '2025' },
  { name: 'reject_test', studentId: '2025502003001', email: 'test-reject@example.com', direction: '结构设计', directionValue: 'STRUCTURAL_DESIGN', major: '土木工程', grade: '2025' },
];

const introduction = '我对该方向有着浓厚的兴趣，在校期间自学了相关基础知识，完成了多个课程项目。我希望加入蓝网团队，与志同道合的同学一起学习成长，在实践中提升自己的技术能力，为团队贡献自己的力量。我愿意投入大量时间钻研技术，积极参与团队活动。';

async function enrollCandidate(page, candidate, index) {
  console.log(`[${index + 1}/${candidates.length}] 报名: ${candidate.name} (${candidate.direction})`);

  await page.goto(`${BASE_URL}/enroll`);
  await page.waitForLoadState('networkidle');

  // 填写姓名
  await page.locator('#username').fill(candidate.name);

  // 填写学号
  await page.locator('#studentId').fill(candidate.studentId);

  // 选择性别
  await page.locator('#gender').click();
  await page.locator('.ant-select-item', { hasText: '男' }).click();

  // 填写邮箱
  await page.locator('#email').fill(candidate.email);

  // 选择学院
  await page.locator('#collegeId').click();
  await page.locator('.ant-select-item', { hasText: '软件学院' }).click();

  // 填写专业
  await page.locator('#major').fill(candidate.major);

  // 选择方向
  await page.locator(`input[type=radio][value="${candidate.directionValue}"]`).check();

  // 填写自我介绍
  await page.locator('#introduction').fill(introduction);

  // 上传头像
  const fileInput = page.locator('input[type=file]');
  await fileInput.setInputFiles(AVATAR_PATH);

  // 等待上传完成（出现预览图）
  await page.waitForTimeout(2000);

  // 提交
  await page.locator('button[type=submit]').click();

  // 等待提交完成（页面跳转或成功提示）
  await page.waitForTimeout(3000);

  console.log(`  ✓ ${candidate.name} 报名提交完成`);
}

(async () => {
  const browser = await chromium.launch({ headless: false, slowMo: 100 });
  const context = await browser.newContext({ viewport: { width: 1280, height: 720 } });
  const page = await context.newPage();

  console.log('开始批量报名...');
  console.log(`共 ${candidates.length} 个考生`);

  for (let i = 0; i < candidates.length; i++) {
    try {
      await enrollCandidate(page, candidates[i], i);
    } catch (err) {
      console.error(`  ✗ ${candidates[i].name} 报名失败:`, err.message);
    }
    // 每个报名间隔 1 秒
    await page.waitForTimeout(1000);
  }

  console.log('批量报名完成');
  await browser.close();
})();
