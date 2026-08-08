'use client'

import { useCallback, useEffect, useState } from 'react'
import { App, Button, Card, Modal, Progress, Spin, Tag, Upload } from 'antd'
import {
  CheckCircleOutlined,
  CloudUploadOutlined,
  DeleteOutlined,
  FileTextOutlined,
  InboxOutlined,
} from '@ant-design/icons'
import type { UploadProps } from 'antd'
import dayjs from 'dayjs'
import { enrollFormService, EnrollFormDTO } from '@/apis/services/enroll-form.service'
import { usePresignedUpload } from '@/hooks/usePresignedUpload'

const { Dragger } = Upload

const ACCEPT_EXTENSIONS = ['.pdf', '.doc', '.docx']

export default function EnrollFormManagementPage() {
  const { message: messageApi } = App.useApp()

  const [currentForm, setCurrentForm] = useState<EnrollFormDTO | null>(null)
  const [loading, setLoading] = useState(true)
  const [deleting, setDeleting] = useState(false)

  const { phase, progress, upload, reset } = usePresignedUpload()
  const uploading = phase === 'preparing' || phase === 'uploading' || phase === 'verifying'

  const loadCurrentForm = useCallback(async () => {
    try {
      setLoading(true)
      const response = await enrollFormService.getCurrent()
      if (response.code === 200) {
        setCurrentForm(response.data ?? null)
      }
    } catch (error) {
      console.error('获取报名表失败:', error)
      messageApi.error('获取报名表失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }, [messageApi])

  useEffect(() => {
    loadCurrentForm()
  }, [loadCurrentForm])

  /** 校验扩展名后走预签名直传，确认成功再调用设置接口完成替换。 */
  const handleUpload: UploadProps['customRequest'] = async ({ file, onError }) => {
    const rawFile = file as File
    const ext = rawFile.name.slice(rawFile.name.lastIndexOf('.')).toLowerCase()
    if (!ACCEPT_EXTENSIONS.includes(ext)) {
      messageApi.error('仅支持 pdf / doc / docx 格式的报名表')
      onError?.(new Error('invalid extension'))
      return
    }

    try {
      const fileId = await upload(rawFile, 'ENROLL_FORM')
      if (fileId == null) return

      const response = await enrollFormService.setEnrollForm(fileId)
      if (response.code === 200) {
        messageApi.success('报名表已更新')
        reset()
        await loadCurrentForm()
      } else {
        messageApi.error(`设置报名表失败: ${response.msg}`)
      }
    } catch (error) {
      console.error('更新报名表失败:', error)
      messageApi.error(error instanceof Error ? error.message : '更新报名表失败，请稍后重试')
    }
  }

  const handleDelete = () => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除当前报名表吗？删除后报名页将不再展示下载入口，此操作不可恢复。',
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          setDeleting(true)
          const response = await enrollFormService.deleteEnrollForm()
          if (response.code === 200) {
            messageApi.success('报名表已删除')
            await loadCurrentForm()
          } else {
            messageApi.error(`删除失败: ${response.msg}`)
          }
        } catch (error) {
          console.error('删除报名表失败:', error)
          messageApi.error('删除报名表失败，请稍后重试')
        } finally {
          setDeleting(false)
        }
      },
    })
  }

  return (
    <div className="flex flex-col gap-6 max-w-[720px]">
      <div>
        <h2 className="m-0">报名表管理</h2>
        <p className="mt-2 mb-0 text-white/45 text-[13px]">
          报名表将在报名页向所有访客提供下载，更新后立即生效。
        </p>
      </div>

      <Spin spinning={loading}>
        <Card
          title={
            <span className="flex items-center gap-2">
              <FileTextOutlined />
              当前报名表
            </span>
          }
        >
          {currentForm ? (
            <div className="flex items-center justify-between flex-wrap gap-3">
              <div className="flex items-center gap-3">
                <Tag icon={<CheckCircleOutlined />} color="success">
                  已上传
                </Tag>
                <span className="text-white/60 text-[13px]">
                  上传时间：{dayjs(currentForm.createdAt).format('YYYY-MM-DD HH:mm')}
                </span>
              </div>
              <Button danger icon={<DeleteOutlined />} loading={deleting} onClick={handleDelete}>
                删除
              </Button>
            </div>
          ) : (
            !loading && <span className="text-white/45">尚未上传报名表</span>
          )}
        </Card>
      </Spin>

      <Card
        title={
          <span className="flex items-center gap-2">
            <CloudUploadOutlined />
            更新报名表
          </span>
        }
      >
        <Dragger
          accept={ACCEPT_EXTENSIONS.join(',')}
          maxCount={1}
          showUploadList={false}
          disabled={uploading}
          customRequest={handleUpload}
        >
          <p className="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
          <p className="ant-upload-hint">仅支持 pdf / doc / docx，上传成功后将自动替换当前报名表</p>
        </Dragger>

        {uploading && (
          <div className="mt-4">
            <Progress percent={Math.round(progress)} status="active" />
            <div className="text-white/45 text-[12px] mt-1">
              {phase === 'preparing' && '正在准备上传...'}
              {phase === 'uploading' && '正在上传文件...'}
              {phase === 'verifying' && '正在校验文件...'}
            </div>
          </div>
        )}
      </Card>
    </div>
  )
}
