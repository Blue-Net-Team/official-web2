import { useCallback } from 'react'
import type { UserExperience } from '@/apis/schema/type'
import type { ExperienceType } from '@/apis/schema/enumerate'
import { userService } from '@/apis/services/user.service'

export interface UseExperienceActionsReturn {
  addExperience: (type: ExperienceType, data: Omit<UserExperience, 'id'>) => Promise<void>
  updateExperience: (id: string, data: Partial<UserExperience>) => Promise<void>
  deleteExperience: (id: string) => Promise<void>
}

/**
 * 经历 CRUD 操作 Hook
 * @param onSuccess 操作成功后的回调（通常用于刷新数据）
 */
export function useExperienceActions(
  onSuccess?: () => void | Promise<void>
): UseExperienceActionsReturn {
  const addExperience = useCallback(
    async (type: ExperienceType, data: Omit<UserExperience, 'id'>): Promise<void> => {
      const res = await userService.createExperience({ ...data, type })
      if (res.code === 200) {
        await onSuccess?.()
      } else {
        throw new Error(res.msg || '添加失败')
      }
    },
    [onSuccess]
  )

  const updateExperience = useCallback(
    async (id: string, data: Partial<UserExperience>): Promise<void> => {
      const res = await userService.updateExperience(id, data)
      if (res.code === 200) {
        await onSuccess?.()
      } else {
        throw new Error(res.msg || '更新失败')
      }
    },
    [onSuccess]
  )

  const deleteExperience = useCallback(
    async (id: string): Promise<void> => {
      const res = await userService.deleteExperience(id)
      if (res.code === 200) {
        await onSuccess?.()
      } else {
        throw new Error(res.msg || '删除失败')
      }
    },
    [onSuccess]
  )

  return { addExperience, updateExperience, deleteExperience }
}
