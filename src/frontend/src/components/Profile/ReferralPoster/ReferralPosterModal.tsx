'use client'

import React, { useCallback, useEffect, useRef, useState } from 'react'
import { Modal, Button, App, Input } from 'antd'
import { QRCodeCanvas } from 'qrcode.react'
import { CopyOutlined, DownloadOutlined } from '@ant-design/icons'
import { POSTER_TEMPLATES } from './poster-templates'

interface ReferralPosterModalProps {
  open: boolean
  onClose: () => void
  username: string
  referralCode: string
}

const ENROLL_PATH = '/enroll'

const ReferralPosterModal: React.FC<ReferralPosterModalProps> = ({
  open,
  onClose,
  username,
  referralCode,
}) => {
  const { message: messageApi } = App.useApp()
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const qrCodeRef = useRef<HTMLDivElement>(null)
  const [generating, setGenerating] = useState(false)
  const [shareLink, setShareLink] = useState('')

  useEffect(() => {
    if (open) {
      const origin = window.location.origin
      setShareLink(`${origin}${ENROLL_PATH}?ref=${referralCode}`)
    }
  }, [open, referralCode])

  const handleCopyLink = useCallback(() => {
    navigator.clipboard
      .writeText(shareLink)
      .then(() => messageApi.success('链接已复制'))
      .catch(() => messageApi.error('复制失败，请手动复制'))
  }, [shareLink, messageApi])

  const drawPoster = useCallback(async () => {
    if (!canvasRef.current || !shareLink) return

    const template = POSTER_TEMPLATES[0]
    const canvas = canvasRef.current
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    canvas.width = template.width
    canvas.height = template.height

    // Draw background
    const bgImage = new Image()
    bgImage.src = template.image
    await new Promise<void>((resolve, reject) => {
      bgImage.onload = () => resolve()
      bgImage.onerror = () => reject(new Error('Failed to load poster template'))
    })
    ctx.drawImage(bgImage, 0, 0, template.width, template.height)

    // Draw QR code
    if (qrCodeRef.current) {
      const qrCanvas = qrCodeRef.current.querySelector('canvas')
      if (qrCanvas) {
        ctx.drawImage(
          qrCanvas,
          template.qrCode.x,
          template.qrCode.y,
          template.qrCode.size,
          template.qrCode.size
        )
      }
    }

    // Draw referral code
    ctx.fillStyle = template.referralCode.color
    ctx.font = `bold ${template.referralCode.fontSize}px ${template.referralCode.fontFamily || 'Arial, sans-serif'}`
    ctx.textAlign = template.referralCode.align || 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(referralCode, template.referralCode.x, template.referralCode.y)

    // Draw referrer name
    ctx.fillStyle = template.referrer.color
    ctx.font = `${template.referrer.fontSize}px ${template.referrer.fontFamily || 'Arial, sans-serif'}`
    ctx.textAlign = 'left'
    ctx.textBaseline = 'middle'
    ctx.fillText(username, template.referrer.x, template.referrer.y)
  }, [referralCode, username, shareLink])

  useEffect(() => {
    if (open && shareLink) {
      drawPoster().catch((err) => {
        console.error('Poster drawing failed:', err)
      })
    }
  }, [open, shareLink, drawPoster])

  const handleDownloadPoster = useCallback(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    canvas.toBlob((blob) => {
      if (!blob) {
        messageApi.error('海报生成失败，请稍后重试')
        return
      }
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.download = `蓝网内推海报_${referralCode}.png`
      link.href = url
      link.click()
      URL.revokeObjectURL(url)
      messageApi.success('海报已生成并下载')
    }, 'image/png')
  }, [referralCode, messageApi])

  return (
    <Modal
      open={open}
      onCancel={onClose}
      footer={null}
      centered
      width={420}
      className="[&_.ant-modal-content]:bg-[#1a1a1a] [&_.ant-modal-content]:border [&_.ant-modal-content]:border-white/10"
    >
      <div className="text-center py-4">
        <h3 className="text-xl font-bold text-white mb-2">我要内推</h3>
        <p className="text-sm text-white/60 mb-6">分享你的专属内推码，邀请优秀人才加入蓝网</p>

        <div className="bg-white/[0.05] rounded-xl p-6 mb-6">
          <div className="text-xs text-white/40 mb-2">我的内推码</div>
          <div className="text-3xl font-bold text-[#6677ff] tracking-widest mb-4">
            {referralCode}
          </div>

          <div className="flex gap-2">
            <Input
              value={shareLink}
              readOnly
              className="!bg-white/[0.03] !border-white/10 !text-white/70 !rounded-lg text-xs"
            />
            <Button
              type="primary"
              icon={<CopyOutlined />}
              onClick={handleCopyLink}
              className="!rounded-lg"
            >
              复制链接
            </Button>
          </div>
        </div>

        <div className="bg-white/[0.05] rounded-xl p-6 mb-6">
          <div className="text-xs text-white/40 mb-3">内推海报</div>
          <div className="flex justify-center mb-4">
            {/* Hidden QR code for canvas rendering */}
            <div ref={qrCodeRef} className="hidden">
              <QRCodeCanvas value={shareLink} size={POSTER_TEMPLATES[0].qrCode.size} />
            </div>
            <canvas
              ref={canvasRef}
              className="max-w-full h-auto rounded-lg shadow-lg"
              style={{ maxHeight: 300 }}
            />
          </div>
          <Button
            type="primary"
            icon={<DownloadOutlined />}
            onClick={handleDownloadPoster}
            className="w-full !rounded-lg"
          >
            下载海报
          </Button>
        </div>
      </div>
    </Modal>
  )
}

export default ReferralPosterModal
