import type {
  AlgorithmExample,
  AlgorithmTestCase,
  JudgeConfigDraftDTO,
} from '@/apis/schema/assessment.dto'

export interface JudgeDraftSourceValues {
  algorithmContent?: string
  inputDescription?: string
  outputDescription?: string
  constraints?: string
}

export interface JudgeDraftTargetValues {
  examples?: AlgorithmExample[]
  runTestCases?: AlgorithmTestCase[]
  generatorSource?: string
  testcases?: unknown[]
}

export interface JudgeConfigDraftFormPatch {
  examples: AlgorithmExample[]
  runTestCases: AlgorithmTestCase[]
  generatorLanguage: 'python'
  generatorSource: string
  testcases: {
    caseNo: number
    category: string
    generatorArgs: string
    weight: number
    hidden: boolean
    sample: boolean
    description: string
  }[]
}

const SOURCE_LABELS: [keyof JudgeDraftSourceValues, string][] = [
  ['algorithmContent', '题干'],
  ['inputDescription', '输入说明'],
  ['outputDescription', '输出说明'],
  ['constraints', '数据范围'],
]

const CATEGORIES = new Set([
  'SAMPLE',
  'NORMAL',
  'EDGE',
  'EMPTY',
  'MINIMUM',
  'MAXIMUM',
  'LARGE',
  'RANDOM',
  'WORST_CASE',
  'SPECIAL',
  'REGRESSION',
])

export function missingJudgeDraftSourceFields(values: JudgeDraftSourceValues): string[] {
  return SOURCE_LABELS.filter(([field]) => !values[field]?.trim()).map(([, label]) => label)
}

export function hasJudgeDraftContent(values: JudgeDraftTargetValues): boolean {
  return Boolean(
    values.examples?.length ||
    values.runTestCases?.length ||
    values.generatorSource?.trim() ||
    values.testcases?.length
  )
}

function assertCompleteDraft(draft: JudgeConfigDraftDTO): void {
  const valid =
    draft.examples.length >= 2 &&
    draft.examples.length <= 3 &&
    draft.runTestCases.length >= 2 &&
    draft.runTestCases.length <= 4 &&
    draft.generatorLanguage === 'python' &&
    draft.generatorSource.trim().length > 0 &&
    draft.testcases.length >= 6 &&
    draft.testcases.length <= 10 &&
    draft.testcases.every(
      (testcase, index) =>
        testcase.caseNo === index + 1 &&
        CATEGORIES.has(testcase.category) &&
        testcase.generatorArgs !== null &&
        typeof testcase.generatorArgs === 'object' &&
        !Array.isArray(testcase.generatorArgs) &&
        testcase.weight === 1 &&
        testcase.hidden === true &&
        testcase.sample === false &&
        testcase.description.trim().length > 0
    )
  if (!valid) throw new Error('AI 返回的判题草稿不完整或格式无效')
}

export function mapJudgeConfigDraftToForm(draft: JudgeConfigDraftDTO): JudgeConfigDraftFormPatch {
  assertCompleteDraft(draft)
  return {
    examples: draft.examples,
    runTestCases: draft.runTestCases,
    generatorLanguage: draft.generatorLanguage,
    generatorSource: draft.generatorSource,
    testcases: draft.testcases.map((testcase) => ({
      ...testcase,
      generatorArgs: JSON.stringify(testcase.generatorArgs, null, 2),
    })),
  }
}
