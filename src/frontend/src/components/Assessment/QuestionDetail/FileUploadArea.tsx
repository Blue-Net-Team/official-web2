'use client'

import {
  FileOutlined,
  DeleteOutlined,
  InboxOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  RedoOutlined,
} from '@ant-design/icons'
import { Upload } from 'antd'
import { formatFileSize } from './utils'
import type { FileUploadAreaProps } from './types'

const PHASE_LABELS: Record<string, string> = {
  preparing: '正在准备...',
  uploading: '正在上传...',
  verifying: '正在校验...',
  completed: '上传完成',
  error: '上传失败',
}

export default function FileUploadArea({
  uploadPhase,
  uploadedFile,
  uploadProgress,
  presignedPhase,
  isExpired,
  answer,
  dropHintText,
  draggerProps,
  onResubmit,
  onRemoveFile,
  onSetUploadedFile,
}: FileUploadAreaProps) {
  const renderUploadedFileRow = (meta: string, onRemove: () => void) => (
    <div className="flex items-center justify-between p-3.5 rounded-lg bg-white/[0.08]">
      <div className="flex items-center gap-3">
        <div className="w-9 h-9 rounded-md bg-[#6677ff]/[0.1] flex items-center justify-center flex-shrink-0">
          <FileOutlined className="text-base text-[#6677ff]" />
        </div>
        <div className="flex flex-col gap-0.5">
          <span className="text-[13px] font-medium text-white">{uploadedFile!.name}</span>
          <span className="text-[11px] text-white/30">
            {uploadedFile!.size ? formatFileSize(uploadedFile!.size) : ''} · {meta}
          </span>
        </div>
      </div>
      <button
        title="删除文件"
        className="w-7 h-7 rounded-md bg-[#f5222d]/[0.24] border-none flex items-center justify-center cursor-pointer transition-colors duration-200 hover:bg-[#f5222d]/[0.48] flex-shrink-0"
        onClick={onRemove}
      >
        <DeleteOutlined className="text-sm text-[#f5222d]!" />
      </button>
    </div>
  )

  const showProgress =
    presignedPhase != null &&
    presignedPhase !== 'idle' &&
    (uploadProgress > 0 || presignedPhase === 'preparing')

  const renderProgressBar = () =>
    showProgress ? (
      <div className="mt-4">
        <div className="flex items-center justify-between mb-1.5">
          <span className="text-xs text-white/50">{PHASE_LABELS[presignedPhase] || ''}</span>
          {presignedPhase !== 'verifying' && presignedPhase !== 'completed' && (
            <span className="text-xs text-white/50">{Math.round(uploadProgress)}%</span>
          )}
        </div>
        <div className="h-1 rounded-[2px] bg-white/[0.04] overflow-hidden">
          <div
            className={
              presignedPhase === 'verifying'
                ? 'h-full rounded-[2px] animate-pulse'
                : 'h-full rounded-[2px] transition-[width] duration-300 ease-out'
            }
            style={{
              width: `${uploadProgress}%`,
              background: 'linear-gradient(90deg, #6677ff, #8594ff)',
            }}
          />
        </div>
      </div>
    ) : null

  switch (uploadPhase) {
    case 'answered':
      return (
        <div className="flex items-center gap-4 p-5 rounded-[10px] bg-[#07c160]/[0.06] border border-[#07c160]/[0.12]">
          <CheckCircleOutlined className="text-[32px] text-[#07c160]" />
          <div className="flex-1">
            <p className="text-base font-semibold text-[#07c160] mb-1">已提交答案</p>
            <p className="text-[13px] text-white/45 m-0">
              提交时间：
              {answer?.submitTime ? new Date(answer.submitTime).toLocaleString('zh-CN') : '-'}
            </p>
          </div>
          {!isExpired && (
            <button
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-[#fa8c16]/[0.1] border border-[#fa8c16]/[0.19] text-[#fa8c16] text-xs font-medium cursor-pointer transition-all duration-200 flex-shrink-0 hover:bg-[#fa8c16]/[0.19]"
              onClick={() => {
                onResubmit()
                onSetUploadedFile(null)
              }}
            >
              <RedoOutlined className="text-sm" />
              重新提交
            </button>
          )}
        </div>
      )
    case 'resubmit_uploaded':
      return (
        <>
          {renderUploadedFileRow('已上传（新文件）', () => onSetUploadedFile(null))}
          {renderProgressBar()}
        </>
      )
    case 'resubmitting':
      return (
        <>
          <Upload.Dragger {...draggerProps}>
            <p className="text-[36px] text-white/30 m-0">
              <InboxOutlined />
            </p>
            <p className="mt-3 text-sm text-white/65">上传新文件替换已提交的答案</p>
            <p className="mt-2 text-xs text-white/30">{dropHintText}</p>
          </Upload.Dragger>
          {renderProgressBar()}
        </>
      )
    case 'expired':
      return (
        <div className="flex items-center gap-4 p-5 rounded-[10px] bg-[#ff4d4f]/[0.06] border border-[#ff4d4f]/[0.12]">
          <WarningOutlined className="text-[32px] text-[#ff4d4f]" />
          <div>
            <p className="text-base font-semibold text-[#ff4d4f] mb-1">考核已结束</p>
            <p className="text-[13px] text-white/45 m-0">
              {uploadedFile ? '考核时间已到，答案已自动提交' : '考核已结束，未提交答案'}
            </p>
          </div>
        </div>
      )
    case 'uploaded':
      return (
        <>
          {renderUploadedFileRow('已上传', onRemoveFile)}
          {renderProgressBar()}
        </>
      )
    case 'idle':
    default:
      return (
        <>
          <Upload.Dragger {...draggerProps}>
            <p className="text-[36px] text-white/30 m-0">
              <InboxOutlined />
            </p>
            <p className="mt-3 text-sm text-white/65">拖拽文件到此处，或点击选择文件</p>
            <p className="mt-2 text-xs text-white/30">{dropHintText}</p>
          </Upload.Dragger>
          {renderProgressBar()}
        </>
      )
  }
}
