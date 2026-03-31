/**
 * ISR (Incremental Static Regeneration) revalidate 配置
 *
 * Next.js 要求 `export const revalidate` 必须是静态数字字面量，
 * 不能使用变量引用。此文件仅作文档参考。
 *
 * 各页面实际 revalidate 值：
 * - Home:          3600s (1h)
 * - Competitions:  3600s (1h)
 * - Lab Environment: 3600s (1h)
 * - Direction/[slug]: 3600s (1h)
 *
 * 环境变量（仅作文档，不影响 revalidate）：
 * - NEXT_PUBLIC_ISR_REVALIDATE: 全局默认值
 * - NEXT_PUBLIC_ISR_HOME / COMPETITIONS / LAB / DIRECTION: 按页面覆盖
 */
export const ISR_REVALIDATE_DEFAULT = 3600
