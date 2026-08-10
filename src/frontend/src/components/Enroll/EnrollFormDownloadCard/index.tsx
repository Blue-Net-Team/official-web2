'use client'

import React, { useState, useEffect } from 'react'
import { Spin } from 'antd'
import { FileTextOutlined, DownloadOutlined } from '@ant-design/icons'
import { enrollFormService, EnrollFormDTO } from '@/apis/services/enroll-form.service'
import { API_BASE_URL } from '@/apis/config'

/**
 * 报名表下载卡片。
 * <p>
 * 公开接口返回当前报名表时展示下载按钮与提示文案，无报名表时不渲染。
 * </p>
 */
const EnrollFormDownloadCard: React.FC = () => {
  const [form, setForm] = useState<EnrollFormDTO | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchForm = async () => {
      try {
        const response = await enrollFormService.getCurrent()
        if (response.code === 200 && response.data) {
          setForm(response.data)
        }
      } catch (error) {
        console.error('获取报名表失败:', error)
      } finally {
        setLoading(false)
      }
    }
    fetchForm()
  }, [])

  if (!loading && !form) {
    return null
  }

  return (
    <div className="p-5 bg-white/[0.03] border border-[#6677ff]/20 rounded-xl max-md:p-4">
      <div className="flex items-center gap-2 mb-4">
        <FileTextOutlined className="text-xl text-[#6677ff]" />
        <span className="text-base font-medium text-white">报名表</span>
      </div>

      {loading ? (
        <div className="flex justify-center p-5">
          <Spin size="small" />
        </div>
      ) : (
        form && (
          <>
            <a
              href={`${API_BASE_URL}/file/download/${form.fileId}`}
              className="flex items-center justify-center gap-2 w-full px-4 py-2.5 bg-[#6677ff]/20 border border-[#6677ff]/40 rounded-lg text-sm text-white transition-all duration-300 hover:bg-[#6677ff]/30 hover:border-[#6677ff]/60"
            >
              <DownloadOutlined />
              <span>下载报名表</span>
            </a>
            <div className="mt-4 pt-3 border-t border-white/10 text-center text-xs text-white/40 leading-5">
              <span>填写完成后请下载打印本报名表，并在面试时带到实验室</span>
            </div>
          </>
        )
      )}
    </div>
  )
}

export default EnrollFormDownloadCard
