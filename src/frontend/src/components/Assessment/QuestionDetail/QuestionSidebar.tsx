'use client'

import {
  CheckCircleOutlined,
  FileOutlined,
  DeleteOutlined,
  DownloadOutlined,
  SendOutlined,
  ExperimentOutlined,
  LeftOutlined,
  RightOutlined,
} from '@ant-design/icons'
import { fileService } from '@/apis/services/file.service'
import { LANGUAGE_LABELS } from './constants'
import { formatFileSize } from './utils'
import type { QuestionSidebarProps } from './types'

export default function QuestionSidebar({
  timeInfo,
  question,
  questionsList,
  currentIndex,
  questionStatistics,
  passRateText,
  answer,
  isAnswered,
  isResubmitting,
  isExpired,
  isFileUpload,
  isChoiceQuestion,
  isSingleChoice,
  isAlgorithm,
  uploadedFile,
  selectedOption,
  selectedOptions,
  algorithmLanguage,
  algorithmCode,
  pollingJobId,
  pollingFormalJob,
  submitting,
  hasPrev,
  hasNext,
  onPrev,
  onNext,
  onSubmit,
  onResubmitConfirm,
  onCancelResubmit,
  onAlgorithmRun,
  onAlgorithmSubmit,
  onRemoveFile,
  onDownloadFile,
}: QuestionSidebarProps) {
  return (
    <div className="flex flex-col gap-6">
      {/* 答题信息 */}
      <div className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-5 flex flex-col gap-4">
        <h3 className="text-sm font-semibold text-white m-0">答题信息</h3>
        <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
        <div className="flex justify-between items-center">
          <span className="text-[13px] text-white/45">考核轮次</span>
          <span className="text-[13px] text-white/65">
            {timeInfo ? (timeInfo.epoch === 0 ? '最终考核' : `第${timeInfo.epoch}轮考核`) : '-'}
          </span>
        </div>
        <div className="flex justify-between items-center">
          <span className="text-[13px] text-white/45">题目序号</span>
          <span className="text-[13px] text-white/65">
            {currentIndex >= 0 ? `${currentIndex + 1} / ${questionsList.length}` : '-'}
          </span>
        </div>
        <div className="flex justify-between items-center">
          <span className="text-[13px] text-white/45">分值</span>
          <span className="text-[13px] font-semibold text-[#fa8c16]">{question.score} 分</span>
        </div>
        {questionStatistics && passRateText && (
          <>
            <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
            <div className="flex justify-between items-center">
              <span className="text-[13px] text-white/45">题目通过率</span>
              <span className="text-[13px] font-semibold text-[#07c160]">{passRateText}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-[13px] text-white/45">通过 / 提交</span>
              <span className="text-[13px] text-white/65">
                {questionStatistics.acceptedCount} / {questionStatistics.submittedCount}
              </span>
            </div>
          </>
        )}
      </div>

      {/* 文件上传题已上传文件 */}
      {isFileUpload && uploadedFile && !isAnswered && !isExpired && (
        <div className="bg-white/[0.06] border border-[#07c160]/[0.1] rounded-xl p-5 flex flex-col gap-4">
          <div className="flex items-center gap-2">
            <CheckCircleOutlined className="text-base text-[#07c160]" />
            <span className="text-sm font-semibold text-white">已上传文件</span>
          </div>
          <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
          <div className="flex items-center gap-3 p-3.5 rounded-lg bg-white/[0.08]">
            <div className="w-9 h-9 rounded-md bg-[#6677ff]/[0.1] flex items-center justify-center flex-shrink-0">
              <FileOutlined className="text-base text-[#6677ff]" />
            </div>
            <div className="flex flex-col gap-0.5 flex-1 min-w-0">
              <span className="text-[13px] font-medium text-white overflow-hidden text-ellipsis whitespace-nowrap">
                {uploadedFile.name}
              </span>
              <span className="text-[11px] text-white/30">
                {uploadedFile.size ? `${formatFileSize(uploadedFile.size)} · ` : ''}刚刚上传
              </span>
            </div>
            <button
              title="删除文件"
              className="w-7 h-7 rounded-md bg-[#f5222d]/[0.24] border-none flex items-center justify-center cursor-pointer transition-colors duration-200 hover:bg-[#f5222d]/[0.48] flex-shrink-0"
              onClick={onRemoveFile}
            >
              <DeleteOutlined className="text-sm text-[#f5222d]!" />
            </button>
          </div>
        </div>
      )}

      {/* 已提交信息 */}
      {isAnswered && !isResubmitting && (
        <div className="bg-white/[0.06] border border-[#07c160]/[0.1] rounded-xl p-5 flex flex-col gap-4">
          <div className="flex items-center gap-2">
            <CheckCircleOutlined className="text-base text-[#07c160]" />
            <span className="text-sm font-semibold text-white">已提交</span>
          </div>
          <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
          <div className="text-[13px] text-white/45 mb-2">
            提交时间：
            {answer?.submitTime ? new Date(answer.submitTime).toLocaleString('zh-CN') : '-'}
          </div>
          {answer?.fileId && (
            <button
              className="inline-flex items-center gap-1 px-4 py-2 rounded-lg bg-[#6677ff]/[0.15] border-none text-[#6677ff] text-[13px] font-medium cursor-pointer transition-all duration-200 w-fit hover:bg-[#6677ff]/[0.25]"
              onClick={() => onDownloadFile?.(answer.fileId!)}
            >
              <DownloadOutlined className="text-sm" />
              下载已提交的答案
            </button>
          )}
          {isChoiceQuestion && answer?.content && (
            <div className="text-[13px] text-white/65">
              你的答案：
              {isSingleChoice
                ? answer.content
                : (() => {
                    try {
                      return JSON.parse(answer.content).join('、')
                    } catch {
                      return answer.content
                    }
                  })()}
            </div>
          )}
          {isAlgorithm && answer?.language && (
            <div className="text-[13px] text-white/65">
              提交语言：{LANGUAGE_LABELS[answer.language] ?? answer.language}
            </div>
          )}
          {answer?.judgement && !isChoiceQuestion && (
            <>
              <div className="flex items-center justify-between gap-3">
                <span className="text-[13px] text-white/45">得分</span>
                <span className="text-[13px] font-semibold text-[#fa8c16]">
                  {answer.judgement.score} / {answer.judgement.maxScore}
                </span>
              </div>
              {answer.judgement.resultCode && (
                <div className="flex items-center justify-between gap-3">
                  <span className="text-[13px] text-white/45">评判结果</span>
                  <span
                    className={`inline-flex items-center px-2.5 py-1 rounded-md border text-xs font-semibold ${
                      (
                        {
                          AC: 'text-[#07c160] bg-[#07c160]/[0.08] border-[#07c160]/[0.18]',
                          WA: 'text-[#ff4d4f] bg-[#ff4d4f]/[0.08] border-[#ff4d4f]/[0.18]',
                          TLE: 'text-[#fa8c16] bg-[#fa8c16]/[0.08] border-[#fa8c16]/[0.18]',
                          RE: 'text-[#ff4d4f] bg-[#ff4d4f]/[0.08] border-[#ff4d4f]/[0.18]',
                          CE: 'text-[#fa8c16] bg-[#fa8c16]/[0.08] border-[#fa8c16]/[0.18]',
                          MLE: 'text-[#fa8c16] bg-[#fa8c16]/[0.08] border-[#fa8c16]/[0.18]',
                        } as Record<string, string>
                      )[answer.judgement.resultCode] ??
                      'text-white/65 bg-white/[0.08] border-white/[0.12]'
                    }
                    `}
                  >
                    {answer.judgement.resultCode} ·{' '}
                    {(
                      {
                        AC: '通过',
                        WA: '答案错误',
                        TLE: '超时',
                        RE: '运行错误',
                        CE: '编译错误',
                        MLE: '内存超限',
                      } as Record<string, string>
                    )[answer.judgement.resultCode] ?? answer.judgement.resultCode}
                  </span>
                </div>
              )}
            </>
          )}
          {answer?.comments && answer.comments.length > 0 && (
            <div className="flex flex-col gap-3 mt-1">
              <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
              <span className="text-[13px] text-white/45">成员评语</span>
              <div className="flex flex-col gap-2.5">
                {answer.comments.map((comment) => (
                  <div
                    key={comment.id}
                    className="rounded-lg border border-white/[0.06] bg-white/[0.03] px-3 py-2.5 flex flex-col gap-1"
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-[13px] text-white/70">
                        {comment.username ?? `用户 ${comment.userId}`}
                      </span>
                      <span className="text-[11px] text-white/35">
                        {comment.commentTime
                          ? new Date(comment.commentTime).toLocaleString('zh-CN')
                          : ''}
                      </span>
                    </div>
                    {comment.score != null && (
                      <span className="text-[12px] text-[#fa8c16]">
                        参考评分：{comment.score} 分
                      </span>
                    )}
                    <span className="text-[13px] text-white/65 leading-relaxed">
                      {comment.content || <span className="text-white/30">无评论内容</span>}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* 按钮区域 */}
      <div className="flex flex-col gap-3">
        {/* 首次提交 */}
        {(isFileUpload || isChoiceQuestion) && !isAnswered && !isExpired && (
          <button
            className="w-full h-11 rounded-lg border-none text-white text-[15px] font-semibold cursor-pointer flex items-center justify-center gap-2 transition-opacity duration-200 hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed bg-gradient-to-b from-[#6677ff] to-[#4455dd]"
            disabled={
              isFileUpload
                ? !uploadedFile || submitting
                : (isSingleChoice ? !selectedOption : selectedOptions.length === 0) || submitting
            }
            onClick={onSubmit}
          >
            <SendOutlined className="text-base" />
            {submitting ? '提交中...' : '提交答案'}
          </button>
        )}

        {/* 算法题按钮 */}
        {isAlgorithm && !isExpired && (
          <>
            <button
              className="w-full h-11 rounded-lg bg-white/[0.08] border border-white/[0.08] text-white/65 text-[15px] font-semibold cursor-pointer flex items-center justify-center gap-2 transition-opacity duration-200 hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={submitting || !!pollingJobId || !algorithmCode.trim() || !algorithmLanguage}
              onClick={onAlgorithmRun}
            >
              <ExperimentOutlined className="text-base" />
              {pollingJobId && !pollingFormalJob ? '运行中...' : '运行代码'}
            </button>
            <button
              className="w-full h-11 rounded-lg border-none text-white text-[15px] font-semibold cursor-pointer flex items-center justify-center gap-2 transition-opacity duration-200 hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed bg-gradient-to-b from-[#6677ff] to-[#4455dd]"
              disabled={submitting || !!pollingJobId || !algorithmCode.trim() || !algorithmLanguage}
              onClick={onAlgorithmSubmit}
            >
              <SendOutlined className="text-base" />
              {pollingJobId && pollingFormalJob
                ? '判题中...'
                : isAnswered
                  ? '重新提交'
                  : '提交答案'}
            </button>
          </>
        )}

        {/* 重新提交确认 */}
        {(isFileUpload || isChoiceQuestion) && isAnswered && isResubmitting && !isExpired && (
          <>
            <button
              className="w-full h-11 rounded-lg border-none text-white text-[15px] font-semibold cursor-pointer flex items-center justify-center gap-2 transition-opacity duration-200 hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed bg-gradient-to-b from-[#6677ff] to-[#4455dd]"
              disabled={
                isFileUpload
                  ? !uploadedFile || submitting
                  : (isSingleChoice ? !selectedOption : selectedOptions.length === 0) || submitting
              }
              onClick={onResubmitConfirm}
            >
              <SendOutlined className="text-base" />
              {submitting ? '提交中...' : '确认重新提交'}
            </button>
            <button
              className="w-full h-11 rounded-lg bg-white/[0.08] border-none text-white/45 text-[15px] font-semibold cursor-pointer flex items-center justify-center gap-2 transition-opacity duration-200 hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed"
              onClick={onCancelResubmit}
            >
              取消
            </button>
          </>
        )}

        {/* 导航 */}
        <div className="flex gap-3 flex-col sm:flex-row">
          <button
            className="flex-1 h-10 rounded-lg bg-white/[0.08] border-none text-white/45 text-[13px] cursor-pointer flex items-center justify-center gap-1.5 transition-all duration-200 hover:bg-white/[0.08] hover:text-white/65 disabled:opacity-40 disabled:cursor-not-allowed"
            onClick={onPrev}
            disabled={!hasPrev}
          >
            <LeftOutlined className="text-sm" />
            上一题
          </button>
          <button
            className="flex-1 h-10 rounded-lg bg-white/[0.08] border-none text-white/45 text-[13px] cursor-pointer flex items-center justify-center gap-1.5 transition-all duration-200 hover:bg-white/[0.08] hover:text-white/65 disabled:opacity-40 disabled:cursor-not-allowed"
            onClick={onNext}
            disabled={!hasNext}
          >
            下一题
            <RightOutlined className="text-sm" />
          </button>
        </div>
      </div>
    </div>
  )
}
