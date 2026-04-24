import { sha256 } from 'js-sha256'

/**
 * @deprecated 前端不应进行密码哈希，密码应由后端直接处理。
 * 保留此函数仅用于向后兼容，新代码请勿使用。
 */
export function hashPassword(password: string): string {
  return sha256(password)
}
