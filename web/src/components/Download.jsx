import { motion } from 'framer-motion'
import { Download as DownloadIcon, Code, Package, Star } from 'lucide-react'
import './shared.css'
import './Download.css'
import AppIcon from './AppIcon'

const stores = [
  { icon: Code,  label: 'GitHub Releases', href: 'https://github.com' },
  { icon: Package, label: 'F-Droid',          href: 'https://f-droid.org' },
  { icon: Star,    label: 'Aptoide',          href: 'https://aptoide.com' },
  { icon: Star,    label: 'Galaxy Store',     href: 'https://seller.samsungapps.com' },
]

export default function DownloadSection() {
  return (
    <section id="download">
      <div className="glow glow-teal" style={{ width:600,height:600, top:'-20%', left:'30%' }} />
      <div className="section-inner">
        <motion.div
          className="download-card"
          initial={{ opacity:0, scale:0.95 }} whileInView={{ opacity:1, scale:1 }}
          viewport={{ once:true }} transition={{ duration:0.7 }}
        >
          <AppIcon size={72} rounded />
          <h2 className="dl-title">Download FocusGuard</h2>
          <p className="dl-sub">Free forever. No account. No subscription. No ads.</p>

          <a href="focusguard.apk" download className="btn-primary btn-xl">
            <DownloadIcon size={22} /> Download APK – Free
          </a>

          <div className="dl-meta">
            <span>📱 Android 8.0+</span>
            <span>📦 ~4 MB</span>
            <span>🔒 No data collected</span>
            <span>⚡ Battery efficient</span>
          </div>

          <div className="dl-divider"><span>Also available on</span></div>

          <div className="store-row">
            {stores.map(({ icon: Icon, label, href }) => (
              <a key={label} href={href} target="_blank" rel="noreferrer" className="store-btn">
                <Icon size={16} /> {label}
              </a>
            ))}
          </div>
        </motion.div>
      </div>
    </section>
  )
}
