import { sha256 } from 'js-sha256'

/**
 * 前端密码哈希 - SHA256
 * 后端登录接口要求密码经过 SHA256 哈希后传输
 */
export function hashPassword(password: string): string {
  return sha256(password)
}
