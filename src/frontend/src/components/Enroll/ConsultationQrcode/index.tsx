'use client'

import React, { useState, useEffect } from 'react'
import { Spin, Popover } from 'antd'
import type { PopoverProps } from 'antd'
import { WechatOutlined } from '@ant-design/icons'
import Image from 'next/image'
import { qrcodeService, ConsultationQrcodeDTO } from '@/apis/services/qrcode.service'
import { API_BASE_URL } from '@/apis/config'
import styles from './styles.module.css'

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
    <div className={styles.popoverContent}>
      <Image
        src={`${API_BASE_URL}/file/download/${fileId}`}
        alt="咨询群二维码"
        width={180}
        height={180}
        className={styles.qrcodeImage}
        unoptimized
      />
      <div className={styles.popoverText}>扫码加入咨询群</div>
    </div>
  )

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <WechatOutlined className={styles.icon} />
        <span className={styles.title}>加入咨询群</span>
      </div>

      {loading ? (
        <div className={styles.loading}>
          <Spin size="small" />
        </div>
      ) : (
        <div className={styles.list}>
          {qrcodes.map((qrcode, index) => (
            <Popover
              key={qrcode.id}
              content={qrcodePopoverContent(qrcode.fileId)}
              trigger="hover"
              placement={popoverPlacement}
            >
              <div className={styles.item}>
                <span className={styles.itemText}>咨询群{index + 1}</span>
                <span className={styles.itemHint}>悬浮预览</span>
              </div>
            </Popover>
          ))}
        </div>
      )}

      <div className={styles.footer}>
        <span>如有疑问，欢迎扫码咨询</span>
      </div>
    </div>
  )
}

export default ConsultationQrcode
