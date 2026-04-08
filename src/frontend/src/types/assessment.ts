/**
 * 考核模块前端独有类型定义
 *
 * 与后端 DTO 对应的类型请使用 @/apis/schema/assessment.dto
 */

/** 年级枚举描述映射 */
export const GradeLabels: Record<number, string> = {
  1: '大一',
  2: '大二',
  3: '大三',
}

/** 题型标签映射 */
export const QuestionTypeLabels: Record<
  import('@/apis/schema/assessment.dto').QuestionType,
  string
> = {
  single_choice: '单选题',
  multiple_choice: '多选题',
  file_upload: '文件上传',
  algorithm: '算法题',
}
