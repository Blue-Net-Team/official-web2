/**
 * 考核模块前端独有类型定义
 *
 * 与后端 DTO 对应的类型请使用 @/apis/schema/assessment.dto
 */

/** 题型标签映射 */
export const QuestionTypeLabels: Record<
  import('@/apis/schema/assessment.dto').QuestionType,
  string
> = {
  SINGLE_CHOICE: '单选题',
  MULTIPLE_CHOICE: '多选题',
  FILE_UPLOAD: '文件上传',
  ALGORITHM: '算法题',
}
