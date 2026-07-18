import { motion } from 'framer-motion'
import { Download, ArrowRight } from 'lucide-react'
import './shared.css'
import './Hero.css'
import PhoneMock from './PhoneMock'

const fadeUp = (delay = 0) => ({
  initial: { opacity: 0, y: 30 },
  animate: { opacity: 1, y: 0 },
  transition: { duration: 0.7, delay, ease: [0.25, 0.46, 0.45, 0.94] }
})

export default function Hero() {
  return (
    <section id="hero" className="hero-section">
      {/* Glow blobs */}
      <div className="glow glow-teal"  style={{ width:600, height:600, top:'-10%', left:'-10%' }} />
      <div className="glow glow-purple" style={{ width:500, height:500, top:'30%', right:'-8%' }} />

      <div className="hero-inner">
        {/* Left column */}
        <div className="hero-text">
          <motion.div className="hero-badge" {...fadeUp(0.1)}>
            🔒 100% Free &nbsp;·&nbsp; No Account &nbsp;·&nbsp; No Ads
          </motion.div>

          <motion.h1 className="hero-h1" {...fadeUp(0.2)}>
            Take Back Control<br/>
            <span className="gradient-text">of Your Screen Time</span>
          </motion.h1>

          <motion.p className="hero-sub" {...fadeUp(0.35)}>
            FocusGuard blocks distracting apps, enforces focus timers, and gives you
            the digital detox you actually need — all without a subscription or data collection.
          </motion.p>

          <motion.div className="hero-actions" {...fadeUp(0.45)}>
            <a href="focusguard.apk" download className="btn-primary" onClick={() => fetch('/api/counter/up').catch(()=>{})}>
              <Download size={20} /> Download APK – Free
            </a>
            <a href="#setup" className="btn-outline">
              Setup Guide <ArrowRight size={18} />
            </a>
          </motion.div>

          <motion.p className="hero-note" {...fadeUp(0.55)}>
            Android 8.0+ &nbsp;·&nbsp; ~15 MB &nbsp;·&nbsp; No Play Store needed
          </motion.p>

          {/* Stats row */}
          <motion.div className="hero-stats" {...fadeUp(0.65)}>
            {[
              { value: '8+',      label: 'Features'         },
              { value: '0',       label: 'Data collected'   },
              { value: '100%',    label: 'Free forever'     },
              { value: '<15 MB',   label: 'App size'         },
            ].map(({ value, label }) => (
              <div key={label} className="stat-pill">
                <span className="stat-val">{value}</span>
                <span className="stat-lbl">{label}</span>
              </div>
            ))}
          </motion.div>
        </div>

        {/* Right column — animated phone */}
        <motion.div
          className="hero-phone-wrap"
          initial={{ opacity: 0, x: 60 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.9, delay: 0.3, ease: [0.25, 0.46, 0.45, 0.94] }}
        >
          <PhoneMock />
        </motion.div>
      </div>
    </section>
  )
}
