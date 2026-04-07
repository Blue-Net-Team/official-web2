import { Members } from '@/components/Members'

export default function MembersPage() {
  return (
    <div className="w-full min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
      <div className="fixed top-0 left-0 w-full h-full bg-[radial-gradient(ellipse_80%_50%_at_20%_40%,rgba(102,119,255,0.15)_0%,transparent_50%),radial-gradient(ellipse_60%_40%_at_80%_60%,rgba(255,107,53,0.1)_0%,transparent_50%),radial-gradient(ellipse_50%_30%_at_50%_100%,rgba(47,39,176,0.2)_0%,transparent_50%)] z-0 pointer-events-none" />
      <main className="w-full pt-16 min-h-screen relative z-1">
        <section className="w-full py-20 md:py-[60px] px-16 md:px-10 text-center max-sm:py-12 max-sm:px-5">
          <h1 className="text-5xl md:text-[36px] max-sm:text-[28px] font-bold text-white mb-4 bg-gradient-to-br from-white to-white/80 bg-clip-text text-transparent">
            团队成员
          </h1>
          <p className="text-lg md:text-base max-sm:text-sm text-white/50 max-w-[600px] mx-auto leading-relaxed">
            汇聚各方向的技术精英，共同推动科技创新与发展
          </p>
        </section>
        <Members />
      </main>
    </div>
  )
}
