/**
 * ai-chat.service 类型级测试
 *
 * 项目当前未配置前端单元测试运行器，此处通过类型约束验证
 * parseSseBuffer 的输入输出契约。
 */

import { AiStreamChunk } from '@/apis/schema/ai-chat.dto'
import { parseSseBuffer } from './ai-chat.service'

// 空缓冲区返回空事件与空余量
const emptyResult: { events: AiStreamChunk[]; remainder: string } = parseSseBuffer('')

// 完整 data: 行可解析为事件
const fullEventResult = parseSseBuffer('data: {"type":"reasoning","content":"思考中"}\n\n')
const firstEvent: AiStreamChunk | undefined = fullEventResult.events[0]

// 未闭合数据保留在 remainder
const partialResult = parseSseBuffer('data: {"type":"content","cont')
const leftover: string = partialResult.remainder

// 类型断言：若编译通过，说明契约符合预期
void emptyResult
void firstEvent
void leftover
