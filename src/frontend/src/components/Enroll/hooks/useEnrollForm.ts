'use client'

import { useState, useEffect, useCallback } from 'react'
import { Form, App } from 'antd'
import { useSearchParams } from 'next/navigation'
import { enrollService } from '@/apis/services/enroll.service'
import { collegeService } from '@/apis/services/college.service'
import { CreateEnrollmentRequestDTO, Direction } from '@/apis/schema/type'
import type { CollegeDTO } from '@/apis/schema/type'
import { DIRECTIONS } from '../constants'

export function useEnrollForm() {
  const { message: messageApi, modal } = App.useApp()
  const [form] = Form.useForm()
  const searchParams = useSearchParams()
  const [selectedDirection, setSelectedDirection] = useState<Direction>('COMPUTER_VISION')
  const [avatarId, setAvatarId] = useState<number | null>(null)
  const [introLength, setIntroLength] = useState(0)
  const [submitting, setSubmitting] = useState(false)
  const [colleges, setColleges] = useState<CollegeDTO[]>([])
  const [loadingColleges, setLoadingColleges] = useState(true)

  const handleAvatarUploaded = useCallback(
    (fileId: number) => {
      setAvatarId(fileId)
      messageApi.success('头像上传成功')
    },
    [messageApi]
  )

  useEffect(() => {
    const directionFromUrl = searchParams.get('direction') as Direction
    if (directionFromUrl && DIRECTIONS.some((d) => d.key === directionFromUrl)) {
      setSelectedDirection(directionFromUrl)
      form.setFieldsValue({ direction: directionFromUrl })
    }

    const referralFromUrl = searchParams.get('ref')
    if (referralFromUrl) {
      form.setFieldsValue({ internalReferralCode: referralFromUrl.toUpperCase() })
    }
  }, [searchParams, form])

  useEffect(() => {
    const fetchColleges = async () => {
      try {
        const response = await collegeService.getColleges()
        if (response.code === 200 && response.data) {
          setColleges(response.data)
        }
      } catch {
        messageApi.error('获取学院列表失败')
      } finally {
        setLoadingColleges(false)
      }
    }
    fetchColleges()
  }, [messageApi])

  const handleDirectionSelect = useCallback(
    (direction: Direction) => {
      setSelectedDirection(direction)
      form.setFieldsValue({ direction })
    },
    [form]
  )

  const handleIntroChange = useCallback((e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const value = e.target.value
    setIntroLength(value.length)
  }, [])

  const submitEnrollment = useCallback(
    async (forceUpdate = false) => {
      const values = form.getFieldsValue()

      if (introLength < 100) {
        messageApi.error('自我介绍至少需要100字')
        return false
      }

      setSubmitting(true)

      try {
        const data: CreateEnrollmentRequestDTO = {
          avatarId: avatarId!,
          username: values.username,
          studentId: values.studentId,
          email: values.email,
          collegeId: values.collegeId,
          major: values.major,
          gender: values.gender,
          direction: selectedDirection,
          introduction: values.introduction,
          internalReferralCode: values.internalReferralCode,
          forceUpdate,
        }

        const response = forceUpdate
          ? await enrollService.updateEnrollment(data)
          : await enrollService.submitEnrollment(data)

        if (response.code === 201 || response.code === 200) {
          messageApi.success(forceUpdate ? '报名信息更新成功！' : '报名成功！')
          form.resetFields()
          setAvatarId(null)
          setIntroLength(0)
          setSelectedDirection('COMPUTER_VISION')
          return true
        } else {
          messageApi.error(response.msg || '报名失败，请稍后重试')
          return false
        }
      } catch (error: unknown) {
        if (error && typeof error === 'object' && 'response' in error) {
          const err = error as {
            response?: {
              data?: {
                code?: number
                msg?: string
                data?: { status?: string }
              }
            }
          }
          if (err.response?.data?.code === 409) {
            if (err.response.data.data?.status !== 'PENDING') {
              messageApi.error(
                err.response.data.data?.status
                  ? '该报名已审核，无法更新报名信息'
                  : err.response.data.msg || '该报名已审核，无法更新报名信息'
              )
              return false
            }
            modal.confirm({
              title: '该学号已报名',
              content: '是否更新报名信息？',
              okText: '更新',
              cancelText: '取消',
              onOk: () => submitEnrollment(true),
            })
            return false
          }
        }
        messageApi.error('网络错误，请稍后重试')
        return false
      } finally {
        setSubmitting(false)
      }
    },
    [avatarId, introLength, selectedDirection, form, messageApi, modal]
  )

  /** 提交报名；返回是否成功（成功时调用方需重置头像上传组件） */
  const handleSubmit = useCallback(async (): Promise<boolean> => {
    if (!avatarId) {
      messageApi.error('请上传头像')
      return false
    }

    return submitEnrollment(false)
  }, [avatarId, submitEnrollment, messageApi])

  return {
    form,
    selectedDirection,
    handleDirectionSelect,
    avatarId,
    handleAvatarUploaded,
    introLength,
    handleIntroChange,
    colleges,
    loadingColleges,
    submitting,
    handleSubmit,
    messageApi,
  }
}
