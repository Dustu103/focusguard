import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { ChevronDown } from 'lucide-react'
import './shared.css'
import './SetupGuide.css'

const steps = [
  {
    num: '01', title: 'Download & Install the APK',
    body: `Tap the Download APK button. Once downloaded, open the file. Android will ask you to allow installation from unknown sources.`,
    tip: { label: '⚙️ How to allow unknown sources:', text: 'Settings → Apps → Special App Access → Install Unknown Apps → select your browser → toggle Allow from this source.' },
  },
  {
    num: '02', title: 'Grant Permissions',
    body: 'FocusGuard needs two special permissions. Both are one-time grants and never shared online.',
    perms: [
      { label: 'Required', name: 'Usage Access',          path: 'Settings → Usage Access → FocusGuard → Enable', opt: false },
      { label: 'Required', name: 'Accessibility Service', path: 'Settings → Accessibility → Installed Apps → FocusGuard → Enable', opt: false },
      { label: 'Optional', name: 'Device Administrator',  path: 'Prevents uninstall without PIN. Recommended for strict sessions.', opt: true },
    ]
  },
  {
    num: '03', title: 'Block Your First App',
    body: 'From the dashboard, tap "Block App". Search for the app you want (e.g. Instagram, TikTok). Choose duration or permanent, then confirm.',
    tip: { label: '💡 Tip:', text: 'Create a Quick List called "Social Media" and add all distracting apps at once. Activate the entire list with one toggle!' },
  },
  {
    num: '04', title: 'Set a Daily Limit (Optional)',
    body: 'Open Daily Limits, find an app (e.g. YouTube), and set a cap like 30 minutes. FocusGuard blocks it automatically after you hit the limit and resets at midnight.',
  },
  {
    num: '05', title: 'Lock it with a PIN',
    body: 'Go to Settings → Set PIN. Once set, unblocking any app requires the PIN — preventing impulsive unlocks.',
    tip: { label: '🔒 Commitment Tip:', text: 'Give your PIN to a trusted friend. This turns FocusGuard into a true commitment device.' },
  },
]

export default function SetupGuide() {
  const [open, setOpen] = useState(0)

  return (
    <section id="setup" style={{ background: 'rgba(11,15,30,0.5)' }}>
      <div className="section-inner">
        <motion.div
          initial={{ opacity:0, y:20 }} whileInView={{ opacity:1, y:0 }}
          viewport={{ once:true }} transition={{ duration:0.6 }}
        >
          <div className="section-label">Getting started</div>
          <h2 className="section-title">Set up in <span className="gradient-text">under 3 minutes</span></h2>
          <p style={{ color:'#64748B', marginBottom:'2.5rem', fontSize:'1rem' }}>Follow these simple steps to get FocusGuard blocking apps on your phone.</p>
        </motion.div>

        <div className="steps-list">
          {steps.map(({ num, title, body, tip, perms }, i) => (
            <motion.div
              key={num}
              className={`step-item ${open === i ? 'open' : ''}`}
              initial={{ opacity:0, x:-20 }} whileInView={{ opacity:1, x:0 }}
              viewport={{ once:true }} transition={{ delay: i * 0.1, duration:0.5 }}
            >
              <button className="step-header" onClick={() => setOpen(open === i ? -1 : i)}>
                <span className="step-num">{num}</span>
                <span className="step-title">{title}</span>
                <motion.span animate={{ rotate: open === i ? 180 : 0 }} transition={{ duration:0.25 }}>
                  <ChevronDown size={18} color="#64748B" />
                </motion.span>
              </button>

              <AnimatePresence initial={false}>
                {open === i && (
                  <motion.div
                    className="step-body"
                    key="body"
                    initial={{ height:0, opacity:0 }}
                    animate={{ height:'auto', opacity:1 }}
                    exit={{ height:0, opacity:0 }}
                    transition={{ duration:0.35, ease:[0.25,0.46,0.45,0.94] }}
                  >
                    <div className="step-body-inner">
                      <p>{body}</p>
                      {tip && (
                        <div className="step-tip">
                          <strong>{tip.label}</strong><br/>
                          <span>{tip.text}</span>
                        </div>
                      )}
                      {perms && (
                        <div className="perms-list">
                          {perms.map(({ label, name, path, opt }) => (
                            <div key={name} className="perm-item">
                              <span className={`perm-badge ${opt ? 'opt' : ''}`}>{label}</span>
                              <div>
                                <strong>{name}</strong><br/>
                                <span className="perm-path">{path}</span>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}
