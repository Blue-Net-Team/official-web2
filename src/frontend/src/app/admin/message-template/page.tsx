'use client'

import { useCallback, useEffect, useState } from 'react'
import { App, Button, Grid, Spin, Table, Tag } from 'antd'
import { EditOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { MessageTemplateInfoDTO } from '@/apis/schema/type'
import { adminMessageTemplateService } from '@/apis/services/admin-message-template.service'
import MessageTemplateDrawer from './MessageTemplateDrawer'

const { useBreakpoint } = Grid

export default function MessageTemplateManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = useBreakpoint()
  const isMobile = !screens.md

  // Data state
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<MessageTemplateInfoDTO[]>([])

  // Drawer state
  const [drawerVisible, setDrawerVisible] = useState(false)
  const [editingRecord, setEditingRecord] = useState<MessageTemplateInfoDTO | null>(null)

  const fetchData = useCallback(async () => {
    try {
      setLoading(true)
      const response = await adminMessageTemplateService.getList()
      if (response.code === 200) {
        setData(response.data || [])
      } else {
        messageApi.error(`获取数据失败: ${response.msg}`)
      }
    } catch (error) {
      console.error('获取模板列表失败:', error)
      messageApi.error('获取模板列表失败')
    } finally {
      setLoading(false)
    }
  }, [messageApi])

  useEffect(() => {
    fetchData()
  }, [fetchData])

  const handleEdit = (record: MessageTemplateInfoDTO) => {
    setEditingRecord(record)
    setDrawerVisible(true)
  }

  const handleDrawerSuccess = () => {
    setDrawerVisible(false)
    fetchData()
  }

  const handleDrawerCancel = () => {
    setDrawerVisible(false)
  }

  const columns: ColumnsType<MessageTemplateInfoDTO> = [
    {
      title: '模板名称',
      dataIndex: 'name',
      key: 'name',
      width: 180,
      ellipsis: true,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      responsive: ['md'],
      render: (desc: string) => desc || '-',
    },
    {
      title: '可用变量',
      dataIndex: 'variables',
      key: 'variables',
      width: 200,
      responsive: ['lg'],
      render: (variables: string[]) => (
        <div className="flex flex-wrap gap-1">
          {variables.map((v) => (
            <Tag key={v} color="blue">
              {'{{'}
              {v}
              {'}}'}
            </Tag>
          ))}
          {variables.length === 0 && <span className="text-[#999]">无</span>}
        </div>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Button
          type="text"
          icon={<EditOutlined />}
          onClick={() => handleEdit(record)}
          size="small"
        />
      ),
    },
  ]

  return (
    <div>
      <div className="mb-4 flex justify-between items-center">
        <h2 className="m-0">消息模板管理</h2>
      </div>

      <Spin spinning={loading}>
        <Table
          columns={columns}
          dataSource={data}
          rowKey="code"
          pagination={false}
          scroll={{ x: isMobile ? 800 : undefined }}
        />
      </Spin>

      <MessageTemplateDrawer
        open={drawerVisible}
        record={editingRecord}
        onSuccess={handleDrawerSuccess}
        onCancel={handleDrawerCancel}
      />
    </div>
  )
}
