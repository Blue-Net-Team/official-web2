/**
 * 个人主页侧边栏组件 - 服务端组件
 *
 * 功能：
 * - 展示用户头像、姓名、昵称、角色标签
 * - 展示个人简介、基本信息（学院/专业/年级）
 * - 展示报名方向
 * - 展示统计数据（考核轮次/已完成/平均分）
 *
 * @author BlueNet Team
 */
import type { UserInfo, UserStats } from '@/types/profile'
import { DirectionLabels } from '@/types/profile'
import { API_BASE_URL } from '@/apis/config'
import styles from './styles.module.css'
import { DesktopOutlined, BookOutlined, CalendarOutlined, EditOutlined } from '@ant-design/icons'
import Image from 'next/image'

interface ProfileSidebarProps {
  profile: UserInfo
  stats: UserStats
}

const directionAbbrMap: Record<string, string> = {
  computer_vision: 'CV',
  embedded: 'EM',
  structural_design: 'SD',
}

export default function ProfileSidebar({ profile, stats }: ProfileSidebarProps) {
  const directionAbbr =
    directionAbbrMap[profile.direction] ||
    (profile.direction ? profile.direction.slice(0, 2).toUpperCase() : '-')
  const directionLabel = DirectionLabels[profile.direction] || profile.direction || '-'
  const displayName = profile.nickname || profile.username

  return (
    <aside className={styles.sidebar}>
      <div className={styles.sidebarContent}>
        <div className={styles.avatarSection}>
          <div className={styles.avatarContainer}>
            <div className={styles.avatarRing}>
              <div className={styles.avatarImg}>
                {profile.avatarFileId ? (
                  <Image
                    src={`${API_BASE_URL}/file/download/${profile.avatarFileId}`}
                    alt={displayName}
                    width={120}
                    height={120}
                  />
                ) : (
                  displayName.charAt(0)
                )}
              </div>
            </div>
            <div className={styles.avatarEdit}>
              <EditOutlined />
            </div>
          </div>
          <div>
            <h1 className={styles.memberName}>{displayName}</h1>
            {profile.nickname && <span className={styles.memberNickname}>@{profile.username}</span>}
          </div>
          <span
            className={`${styles.roleBadge} ${
              profile.roleName === 'candidate' ? styles.roleBadgeCandidate : styles.roleBadgeMember
            }`}
          >
            {profile.roleName === 'candidate' ? '考生' : '成员'}
          </span>
        </div>

        {profile.bio && (
          <div className={styles.bioSection}>
            <p className={styles.bioText}>{profile.bio}</p>
          </div>
        )}

        <div className={styles.infoList}>
          {profile.college && (
            <div className={styles.infoItem}>
              <DesktopOutlined />
              <span>{profile.college}</span>
            </div>
          )}
          {profile.major && (
            <div className={styles.infoItem}>
              <BookOutlined />
              <span>{profile.major}</span>
            </div>
          )}
          {profile.grade && (
            <div className={styles.infoItem}>
              <CalendarOutlined />
              <span>{profile.grade}</span>
            </div>
          )}
        </div>

        {profile.direction && (
          <div className={styles.directionSection}>
            <div className={styles.sectionLabel}>报名方向</div>
            <div className={styles.directionItem}>
              <div className={styles.directionIcon}>{directionAbbr}</div>
              <div className={styles.directionInfo}>
                <div className={styles.directionName}>{directionLabel}</div>
              </div>
            </div>
          </div>
        )}

        <div className={styles.statsSection}>
          <div className={styles.statBox}>
            <div className={styles.statNumber}>{stats.assessmentCount}</div>
            <div className={styles.statLabel}>考核轮次</div>
          </div>
          <div className={styles.statBox}>
            <div className={styles.statNumber}>{stats.completedCount}</div>
            <div className={styles.statLabel}>已完成</div>
          </div>
          <div className={styles.statBox}>
            <div className={styles.statNumber}>{stats.averageScore}</div>
            <div className={styles.statLabel}>平均分</div>
          </div>
        </div>
      </div>
    </aside>
  )
}
