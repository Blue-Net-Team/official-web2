import styles from './styles.module.css'
import Image from 'next/image'

interface ProcessCardProps {
  icon: string
  title: string
  description: string
  btn?: React.ReactNode
}

const ProcessCard = ({ icon, title, description, btn }: ProcessCardProps) => {
  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <Image src={icon} alt={title} className={styles.icon} width={32} height={32} />
        <span className={styles.title}>{title}</span>
      </div>
      <p className={styles.description}>{description}</p>
      {btn}
    </div>
  )
}

export default ProcessCard
