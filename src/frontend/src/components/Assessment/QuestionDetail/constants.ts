import type { ProgrammingLanguage } from '@/apis/schema/assessment.dto'

export const OPTION_LABELS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('')

export const LANGUAGE_LABELS: Record<ProgrammingLanguage, string> = {
  python: 'Python',
  c: 'C',
  cpp: 'C++',
  java: 'Java',
  javascript: 'JavaScript',
}

export const RESULT_LABELS: Record<string, string> = {
  AC: '通过',
  WA: '答案错误',
  TLE: '超时',
  RE: '运行错误',
  CE: '编译错误',
  MLE: '内存超限',
}

export const RESULT_COLOR_CLASSES: Record<string, string> = {
  AC: 'text-[#07c160] bg-[#07c160]/[0.08] border-[#07c160]/[0.18]',
  WA: 'text-[#ff4d4f] bg-[#ff4d4f]/[0.08] border-[#ff4d4f]/[0.18]',
  TLE: 'text-[#fa8c16] bg-[#fa8c16]/[0.08] border-[#fa8c16]/[0.18]',
  RE: 'text-[#ff4d4f] bg-[#ff4d4f]/[0.08] border-[#ff4d4f]/[0.18]',
  CE: 'text-[#fa8c16] bg-[#fa8c16]/[0.08] border-[#fa8c16]/[0.18]',
  MLE: 'text-[#fa8c16] bg-[#fa8c16]/[0.08] border-[#fa8c16]/[0.18]',
}
