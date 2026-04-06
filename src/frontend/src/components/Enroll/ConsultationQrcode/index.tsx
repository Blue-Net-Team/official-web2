'use client'

import React, { useState, useEffect } from 'react'
import { Spin, Popover } from 'antd'
import type { PopoverProps } from 'antd'
import { WechatOutlined } from '@ant-design/icons'
import Image from 'next/image'
import { qrcodeService, ConsultationQrcodeDTO } from '@/apis/services/qrcode.service'
import { API_BASE_URL } from '@/apis/config'

interface ConsultationQrcodeProps {
  popoverPlacement?: PopoverProps['placement']
}

const ConsultationQrcode: React.FC<ConsultationQrcodeProps> = ({ popoverPlacement = 'right' }) => {
  const [qrcodes, setQrcodes] = useState<ConsultationQrcodeDTO[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchQrcodes = async () => {
      try {
        const response = await qrcodeService.getConsultationQrcodes()
        if (response.code === 200 && response.data) {
          setQrcodes(response.data)
        }
      } catch (error) {
        console.error('获取咨询群列表失败:', error)
      } finally {
        setLoading(false)
      }
    }
    fetchQrcodes()
  }, [])

  if (!loading && qrcodes.length === 0) {
    return null
  }

  const qrcodePopoverContent = (fileId: number) => (
    <div className="text-center">
      <Image
        src={`${API_BASE_URL}/file/download/${fileId}`}
        alt="咨询群二维码"
        width={180}
        height={180}
        className="rounded-lg"
        unoptimized
      />
      <div className="mt-2 text-sm text-white/85">扫码加入咨询群</div>
    </div>
  )

  return (
    <div className="p-5 bg-white/[0.03] border border-[#6677ff]/20 rounded-xl max-md:p-4">
      <div className="flex items-center gap-2 mb-4">
        <WechatOutlined className="text-xl text-[#6677ff]" />
        <span className="text-base font-medium text-white">加入咨询群</span>
      </div>

      {loading ? (
        <div className="flex justify-center p-5">
          <Spin size="small" />
        </div>
      ) : (
        <div className="flex flex-wrap gap-3 max-md:flex-col">
          {qrcodes.map((qrcode, index) => (
            <Popover
              key={qrcode.id}
              content={qrcodePopoverContent(qrcode.fileId)}
              trigger="hover"
              placement={popoverPlacement}
            >
              <div className="flex items-center gap-2 px-4 py-2.5 bg-[#6677ff]/10 border border-[#6677ff]/20 rounded-lg cursor-pointer transition-all duration-300 hover:bg-[#6677ff]/20 hover:border-[#6677ff]/40 max-md:justify-between">
                <span className="text-sm text-white">咨询群{index + 1}</span>
                <span className="text-xs text-white/40">悬浮预览</span>
              </div>
            </Popover>
          ))}
        </div>
      )}

      <div className="mt-4 pt-3 border-t border-white/10 text-center text-xs text-white/40">
        <span>如有疑问，欢迎扫码咨询</span>
      </div>
    </div>
  )
}

export default ConsultationQrcode
