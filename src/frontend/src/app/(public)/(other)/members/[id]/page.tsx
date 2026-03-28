'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { MemberService } from '@/apis/services/member.service'
import { MemberDetailDTO, TabCounts } from '@/apis/schema/type'
import { MemberProfileSidebar, MemberProfileContent } from '@/components/MemberProfile'
import styles from '@/components/MemberProfile/MemberProfile.module.css'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { Spin } from 'antd'

export default function MemberProfilePage() {
  const params = useParams()
  const router = useRouter()
  const memberId = Number(params.id)

  const [member, setMember] = useState<MemberDetailDTO | null>(null)
  const [tabCounts, setTabCounts] = useState<TabCounts>({
    projects: 0,
    competitions: 0,
    internships: 0,
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState('profile')

  useEffect(() => {
    const fetchMember = async () => {
      if (!memberId || isNaN(memberId)) {
        setError('无效的成员ID')
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        const [memberData, countsData] = await Promise.all([
          MemberService.getMemberById(memberId),
          MemberService.getMemberTabCounts(memberId),
        ])
        setMember(memberData)
        setTabCounts(countsData)
        setError(null)
      } catch (err) {
        console.error('Failed to fetch member:', err)
        setError('成员不存在或加载失败')
      } finally {
        setLoading(false)
      }
    }

    fetchMember()
  }, [memberId])

  // 加载状态
  if (loading) {
    return (
      <div className={styles.pageContainer}>
        <div className={styles.pageBg} />
        <div className={styles.loadingContainer}>
          <Spin size="large" />
        </div>
      </div>
    )
  }

  // 错误状态
  if (error || !member) {
    return (
      <div className={styles.pageContainer}>
        <div className={styles.pageBg} />
        <div className={styles.errorContainer}>
          <div className={styles.errorIcon}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
          </div>
          <h2 className={styles.errorTitle}>{error || '成员不存在'}</h2>
          <p className={styles.errorDesc}>该成员可能已离开团队或ID不正确</p>
          <button className={styles.backButton} onClick={() => router.push('/members')}>
            <ArrowLeftOutlined />
            返回成员列表
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.pageContainer}>
      <div className={styles.pageBg} />
      <main className={styles.mainContent}>
        <MemberProfileSidebar
          member={member}
          activeTab={activeTab}
          onTabChange={setActiveTab}
          tabCounts={tabCounts}
        />
        <MemberProfileContent
          member={member}
          activeTab={activeTab}
          onTabChange={setActiveTab}
          tabCounts={tabCounts}
        />
      </main>
    </div>
  )
}
