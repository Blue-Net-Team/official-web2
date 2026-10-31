/**
 * AI 判题草稿映射的编译期契约。
 * 运行行为由确定性 Backend Mock 的 Playwright 链路覆盖。
 */
import type { JudgeConfigDraftDTO } from '@/apis/schema/assessment.dto'
import {
  hasJudgeDraftContent,
  mapJudgeConfigDraftToForm,
  missingJudgeDraftSourceFields,
} from './judgeConfigDraft'

const draft: JudgeConfigDraftDTO = {
  examples: [
    { input: '1\n1\n', expectedOutput: '1\n' },
    { input: '2\n1 2\n', expectedOutput: '3\n' },
  ],
  runTestCases: [
    { input: '1\n0\n', expectedOutput: '0\n' },
    { input: '2\n2 3\n', expectedOutput: '5\n' },
  ],
  generatorLanguage: 'python',
  generatorSource: 'print(1)',
  testcases: Array.from({ length: 6 }, (_, index) => ({
    caseNo: index + 1,
    category: 'NORMAL',
    generatorArgs: { n: index + 1, seed: index + 100 },
    weight: 1,
    hidden: true,
    sample: false,
    description: `用例 ${index + 1}`,
  })),
}

const patch = mapJudgeConfigDraftToForm(draft)
const generatorArgs: string = patch.testcases[0].generatorArgs
const generatedLanguage: 'python' = patch.generatorLanguage
const missing: string[] = missingJudgeDraftSourceFields({ algorithmContent: '求和' })
const shouldConfirm: boolean = hasJudgeDraftContent(patch)

// 人工字段通过展开 patch 保留，草稿契约不包含标准解或 Benchmark 字段。
const merged = {
  standardSolutions: [{ language: 'python', source: '人工标准解' }],
  primaryStandardLanguage: 'python',
  benchmarkRepeatTimes: 5,
  ...patch,
}
const manualSource: string = merged.standardSolutions[0].source
const manualBenchmark: number = merged.benchmarkRepeatTimes

void generatorArgs
void generatedLanguage
void missing
void shouldConfirm
void manualSource
void manualBenchmark
