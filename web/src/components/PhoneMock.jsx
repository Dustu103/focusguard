import { motion } from 'framer-motion'
import './PhoneMock.css'

const blockedApps = [
  { label: 'Instagram',  color: '#E91E63', tag: 'BLOCKED',  tagColor: '#E91E63' },
  { label: 'YouTube',    color: '#FF0000', tag: '14m left', tagColor: '#F59E0B' },
  { label: 'TikTok',    color: '#151515', tag: 'BLOCKED',  tagColor: '#E91E63' },
  { label: 'Twitter/X', color: '#1DA1F2', tag: '2h cap',   tagColor: '#6366F1' },
]

export default function PhoneMock() {
  return (
    <div className="phone-outer">
      {/* Ambient glow behind phone */}
      <div className="phone-ambient" />

      <div className="phone-frame">
        {/* Notch */}
        <div className="phone-notch" />

        <div className="phone-screen">
          {/* Header */}
          <div className="mock-header">
            <div className="mock-app-icon-svg">
              <svg width="28" height="28" viewBox="0 0 108 108" fill="none">
                <rect width="108" height="108" fill="#151A2C"/>
                <path fill="#00E5FF" d="M54,18 L24,31 v25 c0,22 13,42 30,52 c17,-10 30,-30 30,-52 V31 L54,18 z M54,26 L76,35 v21 c0,18 -10,34 -22,43 c-12,-9 -22,-25 -22,-43 V35 L54,26 z"/>
                <path fill="#00E5FF" d="M54,45 c-4.4,0 -8,3.6 -8,8 c0,3.3 2,6.1 4.8,7.3 l-1.8,7.7 h10 l-1.8,-7.7 c2.8,-1.2 4.8,-4 4.8,-7.3 C62,48.6 58.4,45 54,45 z"/>
              </svg>
            </div>
            <div>
              <div className="mock-title">FocusGuard</div>
              <div className="mock-sub">Protection Active 🛡️</div>
            </div>
          </div>

          {/* Stat cards */}
          <div className="mock-stats">
            <div className="mock-stat-card teal">
              <div className="mock-stat-num">3</div>
              <div className="mock-stat-label">Apps Blocked</div>
            </div>
            <div className="mock-stat-card purple">
              <motion.div
                className="mock-stat-num"
                animate={{ opacity: [1, 0.5, 1] }}
                transition={{ duration: 2, repeat: Infinity }}
              >2h 14m</motion.div>
              <div className="mock-stat-label">Focus Saved</div>
            </div>
          </div>

          {/* Blocked app list */}
          <div className="mock-list-label">Active Restrictions</div>
          <div className="mock-list">
            {blockedApps.map(({ label, color, tag, tagColor }, i) => (
              <motion.div
                key={label}
                className="mock-row"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.5 + i * 0.15, duration: 0.5 }}
              >
                <div className="mock-dot" style={{ background: color }} />
                <div className="mock-row-label">{label}</div>
                <div className="mock-tag" style={{ background: tagColor + '22', color: tagColor, border:`1px solid ${tagColor}44` }}>{tag}</div>
              </motion.div>
            ))}
          </div>

          {/* Bottom bar */}
          <div className="mock-bottombar">
            <div className="mock-bb-dot active" />
            <div className="mock-bb-dot" />
            <div className="mock-bb-dot" />
            <div className="mock-bb-dot" />
          </div>
        </div>
      </div>
    </div>
  )
}
