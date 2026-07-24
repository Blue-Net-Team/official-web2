/**
 * 内推海报模板设计器（开发/运营辅助工具，非业务功能）
 *
 * 用途：
 * 个人主页"我要内推"功能的海报由"静态底图 + 动态元素（内推码、二维码、内推人姓名）"
 * 在 Canvas 上叠加合成。新增/调整海报模板时，通过本页面可视化确定动态元素的坐标、
 * 字号、颜色与二维码尺寸，避免手工测量像素。
 *
 * 使用方式：
 * 1. 拖拽左侧底图上的标注块，或在右侧表单直接输入数值
 * 2. 点击"导出配置"，将 JSON 粘贴到
 *    src/components/Profile/ReferralPoster/poster-templates.ts 的 POSTER_TEMPLATES 中
 * 3. 新模板需同时将底图放入 public/referral-posters/
 *
 * 注意：
 * - 本页面不注册在管理后台导航菜单中，只能通过直接访问 URL 进入
 * - 初始值读取 POSTER_TEMPLATES 第一个模板
 * - 修改配置后需回到个人主页"我要内推"弹窗确认最终合成效果
 *
 * 详见 docs/03-开发指南/03-03-前端开发规范.md「内推海报模板设计器」一节
 */
'use client'

import React, { useCallback, useEffect, useRef, useState } from 'react'
import { Button, InputNumber, Input, App, Card, Typography, Space } from 'antd'
import { QRCodeCanvas } from 'qrcode.react'
import { POSTER_TEMPLATES } from '@/components/Profile/ReferralPoster/poster-templates'
import type { PosterTemplateConfig } from '@/components/Profile/ReferralPoster/poster-templates'

const { Title, Text } = Typography

interface DraggableElement {
  id: string
  label: string
  x: number
  y: number
  size?: number
  fontSize?: number
  color?: string
}

export default function ReferralPosterDesignPage() {
  const { message: messageApi } = App.useApp()
  const containerRef = useRef<HTMLDivElement>(null)
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const qrCodeRef = useRef<HTMLDivElement>(null)

  const [template, setTemplate] = useState<PosterTemplateConfig>(POSTER_TEMPLATES[0])
  const [elements, setElements] = useState<DraggableElement[]>([
    {
      id: 'referralCode',
      label: '内推码',
      x: template.referralCode.x,
      y: template.referralCode.y,
      fontSize: template.referralCode.fontSize,
      color: template.referralCode.color,
    },
    {
      id: 'qrCode',
      label: '二维码',
      x: template.qrCode.x,
      y: template.qrCode.y,
      size: template.qrCode.size,
    },
    {
      id: 'referrer',
      label: '内推人',
      x: template.referrer.x,
      y: template.referrer.y,
      fontSize: template.referrer.fontSize,
      color: template.referrer.color,
    },
  ])

  const [dragging, setDragging] = useState<string | null>(null)
  const [offset, setOffset] = useState({ x: 0, y: 0 })
  const [previewCode] = useState('AB7K9L')
  const [previewName] = useState('张明轩')
  const [shareLink, setShareLink] = useState('')

  useEffect(() => {
    if (typeof window !== 'undefined') {
      setShareLink(`${window.location.origin}/enroll?ref=${previewCode}`)
    }
  }, [previewCode])

  const drawCanvas = useCallback(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    const ctx = canvas.getContext('2d')
    if (!ctx) return

    canvas.width = template.width
    canvas.height = template.height

    // Background
    const bgImage = new Image()
    bgImage.src = template.image
    bgImage.onload = () => {
      ctx.drawImage(bgImage, 0, 0, template.width, template.height)

      elements.forEach((el) => {
        if (el.id === 'qrCode' && qrCodeRef.current) {
          const qrCanvas = qrCodeRef.current.querySelector('canvas')
          if (qrCanvas && el.size) {
            ctx.drawImage(qrCanvas, el.x, el.y, el.size, el.size)
          }
        } else if (el.id === 'referralCode') {
          ctx.fillStyle = el.color || '#000'
          ctx.font = `bold ${el.fontSize}px Arial, sans-serif`
          ctx.textAlign = 'center'
          ctx.textBaseline = 'middle'
          ctx.fillText(previewCode, el.x, el.y)
        } else if (el.id === 'referrer') {
          ctx.fillStyle = el.color || '#000'
          ctx.font = `${el.fontSize}px Arial, sans-serif`
          ctx.textAlign = 'left'
          ctx.textBaseline = 'middle'
          ctx.fillText(previewName, el.x, el.y)
        }
      })
    }
  }, [elements, template, previewCode, previewName])

  useEffect(() => {
    drawCanvas()
  }, [drawCanvas])

  const handleMouseDown = (e: React.MouseEvent, elementId: string) => {
    const rect = containerRef.current?.getBoundingClientRect()
    if (!rect) return

    const el = elements.find((el) => el.id === elementId)
    if (!el) return

    setDragging(elementId)
    setOffset({
      x: e.clientX - rect.left - el.x,
      y: e.clientY - rect.top - el.y,
    })
  }

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!dragging) return

    const rect = containerRef.current?.getBoundingClientRect()
    if (!rect) return

    const newX = e.clientX - rect.left - offset.x
    const newY = e.clientY - rect.top - offset.y

    setElements((prev) => prev.map((el) => (el.id === dragging ? { ...el, x: newX, y: newY } : el)))
  }

  const handleMouseUp = () => {
    setDragging(null)
  }

  const handleConfigChange = (id: string, field: string, value: number | string) => {
    setElements((prev) => prev.map((el) => (el.id === id ? { ...el, [field]: value } : el)))
  }

  const exportConfig = () => {
    const config = {
      id: template.id,
      image: template.image,
      width: template.width,
      height: template.height,
      referralCode: {
        x: elements.find((el) => el.id === 'referralCode')?.x,
        y: elements.find((el) => el.id === 'referralCode')?.y,
        fontSize: elements.find((el) => el.id === 'referralCode')?.fontSize,
        color: elements.find((el) => el.id === 'referralCode')?.color,
        fontFamily: 'Arial, sans-serif',
        align: 'center' as const,
      },
      qrCode: {
        x: elements.find((el) => el.id === 'qrCode')?.x,
        y: elements.find((el) => el.id === 'qrCode')?.y,
        size: elements.find((el) => el.id === 'qrCode')?.size,
      },
      referrer: {
        x: elements.find((el) => el.id === 'referrer')?.x,
        y: elements.find((el) => el.id === 'referrer')?.y,
        fontSize: elements.find((el) => el.id === 'referrer')?.fontSize,
        color: elements.find((el) => el.id === 'referrer')?.color,
        fontFamily: 'Arial, sans-serif',
      },
    }

    const json = JSON.stringify(config, null, 2)
    navigator.clipboard.writeText(json).then(() => {
      messageApi.success('配置已复制到剪贴板')
    })
  }

  const getElementStyle = (el: DraggableElement) => {
    const baseStyle: React.CSSProperties = {
      position: 'absolute',
      left: el.x,
      top: el.y,
      cursor: 'move',
      userSelect: 'none',
      padding: '4px 8px',
      background: 'rgba(102, 119, 255, 0.2)',
      border: '1px solid #6677ff',
      borderRadius: '4px',
      color: '#fff',
      fontSize: '12px',
    }

    if (el.id === 'qrCode' && el.size) {
      return {
        ...baseStyle,
        width: el.size,
        height: el.size,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }
    }

    return baseStyle
  }

  return (
    <div className="min-h-screen bg-[#0a0a0a] text-white p-8">
      <Title level={2} className="!text-white">
        海报模板设计器
      </Title>
      <Text className="text-white/60">拖拽元素调整位置，右侧修改参数，点击导出获取配置</Text>

      <div className="flex gap-8 mt-8">
        {/* Canvas Preview */}
        <div
          ref={containerRef}
          className="relative border border-white/20 rounded-lg overflow-hidden"
          style={{ width: template.width, height: template.height }}
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          onMouseLeave={handleMouseUp}
        >
          <canvas ref={canvasRef} className="w-full h-full" />

          {elements.map((el) => (
            <div
              key={el.id}
              style={getElementStyle(el)}
              onMouseDown={(e) => handleMouseDown(e, el.id)}
            >
              {el.id === 'qrCode' ? (
                <div className="w-full h-full flex items-center justify-center bg-white rounded">
                  <QRCodeCanvas value={shareLink} size={el.size ? el.size - 20 : 150} />
                </div>
              ) : (
                <span>{el.label}</span>
              )}
            </div>
          ))}
        </div>

        {/* Config Panel */}
        <div className="w-96 space-y-6">
          {elements.map((el) => (
            <Card
              key={el.id}
              title={el.label}
              className="!bg-white/[0.05] !border-white/10"
              styles={{ header: { color: '#fff' } }}
            >
              <Space orientation="vertical" className="w-full">
                <div className="flex gap-4">
                  <div>
                    <Text className="text-white/60 text-xs">X</Text>
                    <InputNumber
                      value={el.x}
                      onChange={(v) => handleConfigChange(el.id, 'x', v || 0)}
                      className="w-full"
                    />
                  </div>
                  <div>
                    <Text className="text-white/60 text-xs">Y</Text>
                    <InputNumber
                      value={el.y}
                      onChange={(v) => handleConfigChange(el.id, 'y', v || 0)}
                      className="w-full"
                    />
                  </div>
                </div>

                {el.size !== undefined && (
                  <div>
                    <Text className="text-white/60 text-xs">Size</Text>
                    <InputNumber
                      value={el.size}
                      onChange={(v) => handleConfigChange(el.id, 'size', v || 100)}
                      className="w-full"
                    />
                  </div>
                )}

                {el.fontSize !== undefined && (
                  <div>
                    <Text className="text-white/60 text-xs">Font Size</Text>
                    <InputNumber
                      value={el.fontSize}
                      onChange={(v) => handleConfigChange(el.id, 'fontSize', v || 16)}
                      className="w-full"
                    />
                  </div>
                )}

                {el.color !== undefined && (
                  <div>
                    <Text className="text-white/60 text-xs">Color</Text>
                    <Input
                      value={el.color}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                        handleConfigChange(el.id, 'color', e.target.value)
                      }
                      className="w-full"
                    />
                  </div>
                )}
              </Space>
            </Card>
          ))}

          <Button type="primary" onClick={exportConfig} className="w-full">
            导出配置
          </Button>
        </div>
      </div>

      {/* Hidden QR code for canvas rendering */}
      <div ref={qrCodeRef} className="hidden">
        <QRCodeCanvas value={shareLink} size={500} />
      </div>
    </div>
  )
}
