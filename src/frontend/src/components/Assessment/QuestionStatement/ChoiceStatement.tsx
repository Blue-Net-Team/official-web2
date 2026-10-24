'use client'

interface ChoiceStatementProps {
  /** 选项文本列表，顺序即 A、B、C… 顺序 */
  options: string[]
  /** 已选中的选项文本，默认空（管理端预览即传空） */
  selectedValues?: string[]
  /** 已提交锁定或已过期时为真，禁用交互与 hover */
  disabled?: boolean
  /** 点击选项回调；传单选/多选的差异由调用方决定。不传即渲染静态列表 */
  onSelect?: (option: string) => void
}

// 与考生端既有实现保持一致（原 OPTION_LABELS 的前 26 项）
const OPTION_LABELS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('')

/**
 * 选择题选项列表的纯展示部分。
 *
 * 选中态的边框/底色/序号圆点/文字样式全部逐项内联保留 —— 这些样式随
 * isSelected 变化，无法由外层包裹复现，所以选中态必须作为 props 传入。
 *
 * 组件本身不感知单选还是多选：单选由调用方在 onSelect 里替换选中项，
 * 多选由调用方在 onSelect 里切换集合。
 */
export default function ChoiceStatement({
  options,
  selectedValues,
  disabled = false,
  onSelect,
}: ChoiceStatementProps) {
  const selected = selectedValues ?? []
  const interactive = Boolean(onSelect) && !disabled

  return (
    <div className="flex flex-col gap-3">
      {options.map((option, index) => {
        const label = OPTION_LABELS[index] || `${index + 1}`
        const isSelected = selected.includes(option)

        return (
          <div
            key={index}
            onClick={() => {
              if (!interactive) return
              onSelect?.(option)
            }}
            className={`flex items-center gap-4 p-4 rounded-xl transition-all duration-200 border ${
              isSelected
                ? 'bg-[#6677ff]/[0.08] border-[#6677ff]/[0.3]'
                : 'bg-white/[0.04] border-white/[0.08]'
            } ${interactive ? 'cursor-pointer hover:bg-white/[0.06]' : ''} ${
              disabled ? 'opacity-90' : ''
            }`}
          >
            <span
              className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-semibold flex-shrink-0 ${
                isSelected ? 'bg-[#6677ff] text-white' : 'bg-white/[0.08] text-white/45'
              }`}
            >
              {label}
            </span>
            <span className={`text-sm ${isSelected ? 'text-white font-medium' : 'text-white/65'}`}>
              {option}
            </span>
          </div>
        )
      })}
    </div>
  )
}
