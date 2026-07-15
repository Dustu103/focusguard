import { motion } from 'framer-motion'
import { Shield, Clock, Calendar, Users, Lock, Activity, Database, Zap } from 'lucide-react'
import './shared.css'
import './Features.css'

const features = [
  { icon: Shield,   color: '#00E5FF', bg: 'rgba(0,229,255,0.1)',    title: 'App Blocking',       desc: 'Block any app permanently or for a set duration. A confirmation lock stops impulsive unlocks.' },
  { icon: Clock,    color: '#A855F7', bg: 'rgba(168,85,247,0.1)',   title: 'Timer-Based Blocks', desc: 'Set a block from 5 minutes to 24 hours. Auto-unblocks when time is up — no manual steps.' },
  { icon: Calendar, color: '#F59E0B', bg: 'rgba(245,158,11,0.1)',   title: 'Schedule Blocking',  desc: 'Recurring schedules that automatically block social media every evening or during work hours.' },
  { icon: Users,    color: '#22C55E', bg: 'rgba(34,197,94,0.1)',    title: 'Quick Lists',        desc: 'Group apps into lists like "Study Mode" or "Bedtime" and toggle the whole group instantly.' },
  { icon: Lock,     color: '#EF4444', bg: 'rgba(239,68,68,0.1)',    title: 'PIN Protection',     desc: 'Lock any block behind a PIN. Perfect for commitment-based sessions — can\'t cave impulsively.' },
  { icon: Activity, color: '#0EA5E9', bg: 'rgba(14,165,233,0.1)',   title: 'Daily Limits',       desc: 'Set a daily cap per app. FocusGuard auto-blocks when you hit your limit, resets at midnight.' },
  { icon: Database, color: '#6366F1', bg: 'rgba(99,102,241,0.1)',   title: '100% Offline',       desc: 'No account, no server, no cloud. All data lives on your device in an encrypted local database.' },
  { icon: Zap,      color: '#10B981', bg: 'rgba(16,185,129,0.1)',   title: 'Battery Efficient',  desc: 'Service auto-shuts down when no blocks are active — consuming exactly zero battery when idle.' },
]

const container = {
  hidden: {},
  show: { transition: { staggerChildren: 0.08 } }
}
const item = {
  hidden: { opacity: 0, y: 30 },
  show:   { opacity: 1, y: 0, transition: { duration: 0.5, ease: [0.25,0.46,0.45,0.94] } }
}

export default function Features() {
  return (
    <section id="features">
      <div className="glow glow-purple" style={{ width:500,height:500, top:'20%', left:'-8%' }} />
      <div className="section-inner">
        <motion.div
          initial={{ opacity:0, y:20 }} whileInView={{ opacity:1, y:0 }}
          viewport={{ once:true }} transition={{ duration:0.6 }}
        >
          <div className="section-label">What FocusGuard does</div>
          <h2 className="section-title">Every tool you need to<br/><span className="gradient-text">stay focused</span></h2>
        </motion.div>

        <motion.div
          className="features-grid"
          variants={container} initial="hidden"
          whileInView="show" viewport={{ once:true, margin:'-80px' }}
        >
          {features.map(({ icon: Icon, color, bg, title, desc }) => (
            <motion.div key={title} className="feature-card" variants={item}>
              <div className="feature-icon" style={{ background: bg, border: `1px solid ${color}33` }}>
                <Icon size={26} color={color} />
              </div>
              <h3 className="feature-title">{title}</h3>
              <p className="feature-desc">{desc}</p>
            </motion.div>
          ))}
        </motion.div>
      </div>
    </section>
  )
}
