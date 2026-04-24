'use client'

import { useCallback, useEffect, useState } from 'react'
import { App, Button, Grid, Spin, Switch, Table, Tag } from 'antd'
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

  const handleToggle = async (record: MessageTemplateInfoDTO) => {
    try {
      const newEnabled = !record.enabled
      const response = await adminMessageTemplateService.toggle(record.code, newEnabled)
      if (response.code === 200) {
        messageApi.success(newEnabled ? '已启用' : '已禁用')
        fetchData()
      } else {
        messageApi.error(`操作失败: ${response.msg}`)
      }
    } catch (error) {
      console.error('切换模板状态失败:', error)
      messageApi.error('操作失败')
    }
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
      title: '模板编码',
      dataIndex: 'code',
      key: 'code',
      width: 180,
      responsive: ['md'],
      render: (code: string) => <Tag>{code}</Tag>,
    },
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
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
          {variables.map((v) => (
            <Tag key={v} color="blue">
              {'{{'}
              {v}
              {'}}'}
            </Tag>
          ))}
          {variables.length === 0 && <span style={{ color: '#999' }}>无</span>}
        </div>
      ),
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      render: (enabled: boolean) => (
        <Tag color={enabled ? 'green' : 'red'}>{enabled ? '启用' : '禁用'}</Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 160,
      render: (_, record) => (
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            size="small"
          />
          <Switch
            checked={record.enabled}
            onChange={() => handleToggle(record)}
            checkedChildren="启"
            unCheckedChildren="禁"
            size="small"
          />
        </div>
      ),
    },
  ]

  return (
    <div>
      <div
        style={{
          marginBottom: 16,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}
      >
        <h2 style={{ margin: 0 }}>消息模板管理</h2>
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
