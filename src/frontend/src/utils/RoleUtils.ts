/**
 * 获取用户角色等级
 *
 * @param role 用户角色
 * @returns 用户角色等级，-1表示未知角色
 */
export const getRoleLevel = (role: string) => {
  switch (role) {
    case 'SUPER_ADMIN':
      return 3
    case 'DIRECTION_ADMIN':
      return 2
    case 'MEMBER':
      return 1
    case 'CANDIDATE':
      return 0
    default:
      return -1
  }
}
