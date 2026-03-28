import { Footer } from 'antd/lib/layout/layout'
import styles from './style.module.css'

export default function AppFooter() {
  return (
    <Footer className={styles.footer}>
      <span className="darkText">© 2024 Ant Design Demo. All rights reserved.</span>
    </Footer>
  )
}
