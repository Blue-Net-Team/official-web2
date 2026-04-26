'use client'

import { useEffect, useState } from 'react'
import { Button, Descriptions, Divider, Drawer, Form, Image, Input, Spin, Upload } from 'antd'
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons'
import type {
  EquipmentDTO,
  CreateEquipmentRequestDTO,
  UpdateEquipmentRequestDTO,
} from '@/apis/schema/type'
import { fileService } from '@/apis/services/file.service'
import { API_BASE_URL } from '@/apis/config'
import { adminEquipmentService } from '@/apis/services/admin-equipment.service'

export type DrawerMode = 'view' | 'edit' | 'create'

interface EquipmentDrawerProps {
  open: boolean
  equipment: EquipmentDTO | null
  mode: DrawerMode
  onClose: () => void
  onSuccess: () => void
  onDelete: (equipment: EquipmentDTO) => void
  onEdit: () => void
}

export default function EquipmentDrawer({
  open,
  equipment,
  mode,
  onClose,
  onSuccess,
  onDelete,
  onEdit,
}: EquipmentDrawerProps) {
  const [form] = Form.useForm<CreateEquipmentRequestDTO>()
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [imageFileId, setImageFileId] = useState<number | null>(null)

  useEffect(() => {
    if (open) {
      if (mode === 'create') {
        form.resetFields()
        setImageFileId(null)
      } else if (equipment) {
        form.setFieldsValue({
          name: equipment.name,
          brand: equipment.brand ?? undefined,
          description: equipment.description ?? undefined,
          imageFileId: equipment.imageFileId ?? undefined,
        })
        setImageFileId(equipment.imageFileId)
      }
    }
  }, [open, mode, equipment, form])

  const handleSave = async () => {
    try {
      const values = await form.validateFields()
      setSaving(true)
      const data: CreateEquipmentRequestDTO | UpdateEquipmentRequestDTO = {
        ...values,
        imageFileId: imageFileId ?? null,
      }
      if (mode === 'create') {
        await adminEquipmentService.create(data as CreateEquipmentRequestDTO)
      } else if (equipment) {
        await adminEquipmentService.update(equipment.id, data as UpdateEquipmentRequestDTO)
      }
      onSuccess()
    } finally {
      setSaving(false)
    }
  }

  const handleUpload = async (file: File) => {
    setUploading(true)
    try {
      const res = await fileService.upload(file, 'NORMAL_IMG')
      if (res.code === 200 && res.data) {
        setImageFileId(res.data.id)
        form.setFieldValue('imageFileId', res.data.id)
      }
    } finally {
      setUploading(false)
    }
    return false
  }

  const title =
    mode === 'create' ? '新建设备' : mode === 'edit' ? '编辑设备' : (equipment?.name ?? '设备详情')

  return (
    <Drawer
      title={title}
      placement="right"
      width={480}
      open={open}
      onClose={onClose}
      styles={{ body: { padding: 0 } }}
      footer={
        <div className="flex justify-end gap-2">
          {mode === 'view' && equipment ? (
            <>
              <Button danger icon={<DeleteOutlined />} onClick={() => onDelete(equipment)}>
                删除
              </Button>
              <Button type="primary" icon={<EditOutlined />} onClick={onEdit}>
                编辑
              </Button>
            </>
          ) : (
            <>
              <Button onClick={onClose}>取消</Button>
              <Button type="primary" loading={saving} onClick={handleSave}>
                {mode === 'create' ? '创建' : '保存'}
              </Button>
            </>
          )}
        </div>
      }
    >
      {mode === 'view' && equipment ? (
        <DetailView equipment={equipment} />
      ) : (
        <FormView
          form={form}
          imageFileId={imageFileId}
          uploading={uploading}
          onUpload={handleUpload}
        />
      )}
    </Drawer>
  )
}

/** 查看模式 */
function DetailView({ equipment }: { equipment: EquipmentDTO }) {
  return (
    <div className="p-4">
      <Descriptions column={1} size="small">
        <Descriptions.Item label="名称">{equipment.name}</Descriptions.Item>
        {equipment.brand && <Descriptions.Item label="品牌">{equipment.brand}</Descriptions.Item>}
        {equipment.description && (
          <Descriptions.Item label="描述">{equipment.description}</Descriptions.Item>
        )}
      </Descriptions>

      {equipment.imageFileId && (
        <>
          <Divider>图片</Divider>
          <Image
            src={`${API_BASE_URL}/file/download/${equipment.imageFileId}`}
            alt={equipment.name}
            width={200}
            className="rounded-lg"
          />
        </>
      )}
    </div>
  )
}

/** 编辑/创建模式 */
function FormView({
  form,
  imageFileId,
  uploading,
  onUpload,
}: {
  form: ReturnType<typeof Form.useForm<CreateEquipmentRequestDTO>>[0]
  imageFileId: number | null
  uploading: boolean
  onUpload: (file: File) => void
}) {
  return (
    <div className="p-4">
      <Form form={form} layout="vertical" size="middle">
        <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入设备名称' }]}>
          <Input placeholder="设备名称" maxLength={100} />
        </Form.Item>

        <Form.Item name="brand" label="品牌">
          <Input placeholder="设备品牌" maxLength={100} />
        </Form.Item>

        <Form.Item name="description" label="描述">
          <Input.TextArea placeholder="设备描述" maxLength={500} showCount rows={3} />
        </Form.Item>

        <Form.Item name="sortOrder" label="排序权重">
          <Input type="number" placeholder="数值越大越靠前" />
        </Form.Item>

        <Form.Item name="imageFileId" hidden>
          <Input />
        </Form.Item>

        <Form.Item label="图片">
          <Upload accept="image/*" showUploadList={false} beforeUpload={onUpload}>
            {imageFileId ? (
              <Spin spinning={uploading}>
                <Image
                  src={`${API_BASE_URL}/file/download/${imageFileId}`}
                  alt="设备图片"
                  width={200}
                  className="rounded-lg"
                  preview={false}
                />
              </Spin>
            ) : (
              <button
                title="上传图片"
                className="flex items-center justify-center w-[200px] h-[120px] rounded-lg border border-dashed border-white/20 hover:border-white/40 transition-colors"
                type="button"
              >
                <PlusOutlined className="text-lg text-white/40" />
              </button>
            )}
          </Upload>
        </Form.Item>
      </Form>
    </div>
  )
}
