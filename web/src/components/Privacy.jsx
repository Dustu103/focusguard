import { motion } from 'framer-motion'
import './shared.css'
import './Privacy.css'

const points = [
  'All data (blocked apps, schedules, PINs) is stored locally on your device only.',
  'The app does not require an internet connection to function.',
  'We do not use analytics, tracking, or advertising SDKs of any kind.',
  'Permissions requested (Usage Access, Accessibility) are used solely to detect and block foreground apps on-device.',
  'Uninstalling the app permanently deletes all local data.',
]

export default function Privacy() {
  return (
    <section id="privacy" style={{ background:'rgba(11,15,30,0.4)' }}>
      <div className="section-inner">
        <motion.div
          className="privacy-card"
          initial={{ opacity:0, y:30 }} whileInView={{ opacity:1, y:0 }}
          viewport={{ once:true }} transition={{ duration:0.6 }}
        >
          <div className="privacy-icon">🔐</div>
          <h2>Privacy Policy</h2>
          <p className="privacy-lead"><strong>FocusGuard does not collect, store, or transmit any personal data.</strong></p>
          <ul className="privacy-list">
            {points.map(p => (
              <li key={p}><span className="tick">✓</span>{p}</li>
            ))}
          </ul>
          <p className="privacy-date">Last updated: July 2026</p>
        </motion.div>
      </div>
    </section>
  )
}
