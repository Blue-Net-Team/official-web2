import { GithubOutlined, SafetyOutlined } from '@ant-design/icons'
import { Footer } from 'antd/lib/layout/layout'

const GITHUB_ORG_URL = 'https://github.com/Blue-Net-Team'
const ICP_URL = 'https://beian.miit.gov.cn/'
const POLICE_RECORD_URL =
  'http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=44081102000111'

export default function AppFooter() {
  return (
    <Footer className="relative z-10 !bg-[#19191c] px-6 py-5 text-white">
      <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 sm:flex-row">
        {/* 左侧：版权 */}
        <span className="text-sm text-white/60">
          © {new Date().getFullYear()} BlueNet Team. All rights reserved.
        </span>

        {/* 右侧：备案与 GitHub */}
        <div className="flex flex-wrap items-center justify-center gap-x-5 gap-y-2 text-xs text-white/50">
          <a
            href={ICP_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="transition-colors duration-200 hover:text-white/80"
          >
            粤ICP备2026040294号-1
          </a>
          <a
            href={POLICE_RECORD_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1 transition-colors duration-200 hover:text-white/80"
          >
            <SafetyOutlined className="text-[#00b96b]" />
            粤公网安备44081102000111号
          </a>
          <a
            href={GITHUB_ORG_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 text-sm text-white/60 transition-colors duration-200 hover:text-white"
            aria-label="BlueNet GitHub 组织"
          >
            <GithubOutlined className="text-lg" />
            <span className="hidden sm:inline">开源仓库</span>
          </a>
        </div>
      </div>
    </Footer>
  )
}
