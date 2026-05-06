'use client'

import { useCallback, useEffect, useState } from 'react'
import { App, Button, Grid, Image, InputNumber, Modal, Select, Spin, Table, Tabs, Tag } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { ConsultationQrcodeDTO, AssessmentQrcodeDTO } from '@/apis/services/qrcode.service'
import { qrcodeService } from '@/apis/services/qrcode.service'
import QrcodeDrawer, { type DrawerMode, type QrcodeType } from './QrcodeDrawer'
import { API_BASE_URL } from '@/apis/config'

const { useBreakpoint } = Grid
const { Option } = Select

const DIRECTION_LABELS: Record<string, string> = {
  COMPUTER_VISION: '计算机视觉',
  STRUCTURAL_DESIGN: '结构设计',
  EMBEDDED: '嵌入式开发',
}

type TabKey = 'consultation' | 'assessment'

export default function QrcodeManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = useBreakpoint()
  const isMobile = !screens.md

  // Tab state
  const [activeTab, setActiveTab] = useState<TabKey>('consultation')

  // Drawer state
  const [drawerVisible, setDrawerVisible] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('create')
  const [editingRecord, setEditingRecord] = useState<ConsultationQrcodeDTO | AssessmentQrcodeDTO | null>(null)

  // Filter state for assessment
  const [filterDirection, setFilterDirection] = useState<string | undefined>()
  const [filterEpoch, setFilterEpoch] = useState<number | undefined>()

  // Data state
  const [consultationData, setConsultationData] = useState<ConsultationQrcodeDTO[]>([])
  const [assessmentData, setAssessmentData] = useState<AssessmentQrcodeDTO[]>([])
  const [loading, setLoading] = useState(false)

  const loadConsultationData = useCallback(async () => {
    try {
      setLoading(true)
      const response = await qrcodeService.getConsultationQrcodesAdmin()
      if (response.code === 200 && response.data) {
        setConsultationData(response.data)
      }
    } catch (error) {
      console.error('加载咨询群二维码失败:', error)
      messageApi.error('加载咨询群二维码失败')
    } finally {
      setLoading(false)
    }
  }, [messageApi])

  const loadAssessmentData = useCallback(async () => {
    try {
      setLoading(true)
      const response = await qrcodeService.getAssessmentQrcodes(filterDirection, filterEpoch)
      if (response.code === 200 && response.data) {
        setAssessmentData(response.data)
      }
    } catch (error) {
      console.error('加载考核群二维码失败:', error)
      messageApi.error('加载考核群二维码失败')
    } finally {
      setLoading(false)
    }
  }, [filterDirection, filterEpoch, messageApi])

  const loadData = useCallback(() => {
    if (activeTab === 'consultation') {
      loadConsultationData()
    } else {
      loadAssessmentData()
    }
  }, [activeTab, loadConsultationData, loadAssessmentData])

  useEffect(() => {
    loadData()
  }, [loadData])

  const handleTabChange = (key: string) => {
    setActiveTab(key as TabKey)
  }

  const handleCreate = () => {
    setDrawerMode('create')
    setEditingRecord(null)
    setDrawerVisible(true)
  }

  const handleEdit = (record: ConsultationQrcodeDTO | AssessmentQrcodeDTO) => {
    setDrawerMode('edit')
    setEditingRecord(record)
    setDrawerVisible(true)
  }

  const handleDelete = (record: ConsultationQrcodeDTO | AssessmentQrcodeDTO) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除该二维码吗？此操作不可恢复。`,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          let response
          if (activeTab === 'consultation') {
            response = await qrcodeService.deleteConsultationQrcode(record.id)
          } else {
            response = await qrcodeService.deleteAssessmentQrcode(record.id)
          }

          if (response.code === 200) {
            messageApi.success('删除成功')
            loadData()
          } else {
            messageApi.error(`删除失败: ${response.msg}`)
          }
        } catch (error) {
          console.error('删除失败:', error)
          messageApi.error('删除失败')
        }
      },
    })
  }

  const handleDrawerSuccess = () => {
    setDrawerVisible(false)
    loadData()
  }

  const handleDrawerCancel = () => {
    setDrawerVisible(false)
  }

  const consultationColumns: ColumnsType<ConsultationQrcodeDTO> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
      responsive: ['md'],
    },
    {
      title: '二维码预览',
      key: 'preview',
      width: 120,
      render: (_, record) => (
        <Image
          width={80}
          src={`${API_BASE_URL}/file/download/${record.fileId}`}
          alt="咨询群二维码"
          className="rounded"
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <div className="flex gap-2">
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            size="small"
          />
          <Button
            type="text"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record)}
            size="small"
          />
        </div>
      ),
    },
  ]

  const assessmentColumns: ColumnsType<AssessmentQrcodeDTO> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
      responsive: ['md'],
    },
    {
      title: '方向',
      dataIndex: 'direction',
      key: 'direction',
      width: 150,
      render: (direction: string | undefined) => (
        <span>{direction ? DIRECTION_LABELS[direction] || direction : '-'}</span>
      ),
    },
    {
      title: '轮次',
      dataIndex: 'epoch',
      key: 'epoch',
      width: 100,
    },
    {
      title: '三方向共用',
      dataIndex: 'isShared',
      key: 'isShared',
      width: 120,
      render: (isShared: boolean | undefined) => (
        <Tag color={isShared ? 'blue' : 'default'}>{isShared ? '是' : '否'}</Tag>
      ),
    },
    {
      title: '二维码预览',
      key: 'preview',
      width: 120,
      render: (_, record) => (
        <Image
          width={80}
          src={`${API_BASE_URL}/file/download/${record.fileId}`}
          alt="考核群二维码"
          className="rounded"
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <div className="flex gap-2">
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            size="small"
          />
          <Button
            type="text"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record)}
            size="small"
          />
        </div>
      ),
    },
  ]

  const tabItems = [
    {
      key: 'consultation',
      label: '咨询群二维码',
      children: (
        <div>
          <div className="mb-4 flex justify-between items-center">
            <div className="flex gap-2"></div>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              新建二维码
            </Button>
          </div>

          <Spin spinning={loading}>
            <Table
              columns={consultationColumns}
              dataSource={consultationData}
              rowKey="id"
              pagination={false}
              scroll={{ x: isMobile ? 800 : undefined }}
            />
          </Spin>
        </div>
      ),
    },
    {
      key: 'assessment',
      label: '考核群二维码',
      children: (
        <div>
          <div className="mb-4 flex justify-between items-center">
            <div className="flex gap-2 flex-wrap">
              <Select
                value={filterDirection}
                onChange={(value) => setFilterDirection(value)}
                placeholder="全部方向"
                allowClear
                className="w-[150px]"
                options={[
                  { value: 'COMPUTER_VISION', label: '计算机视觉' },
                  { value: 'STRUCTURAL_DESIGN', label: '结构设计' },
                  { value: 'EMBEDDED', label: '嵌入式开发' },
                ]}
              />
              <InputNumber
                placeholder="轮次"
                value={filterEpoch}
                onChange={(value) => setFilterEpoch(value ?? undefined)}
                className="w-[100px]"
                min={1}
              />
              <Button
                onClick={() => {
                  setFilterDirection(undefined)
                  setFilterEpoch(undefined)
                }}
              >
                重置
              </Button>
            </div>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
              新建二维码
            </Button>
          </div>

          <Spin spinning={loading}>
            <Table
              columns={assessmentColumns}
              dataSource={assessmentData}
              rowKey="id"
              pagination={false}
              scroll={{ x: isMobile ? 800 : undefined }}
            />
          </Spin>
        </div>
      ),
    },
  ]

  return (
    <div>
      <div className="mb-4">
        <h2 className="m-0">二维码管理</h2>
      </div>

      <Tabs
        activeKey={activeTab}
        onChange={handleTabChange}
        items={tabItems}
      />

      <QrcodeDrawer
        open={drawerVisible}
        mode={drawerMode}
        type={activeTab}
        record={editingRecord}
        onSuccess={handleDrawerSuccess}
        onCancel={handleDrawerCancel}
      />
    </div>
  )
}
