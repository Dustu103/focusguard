import { motion } from 'framer-motion'
import { useState, useEffect } from 'react'
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
  const [downloads, setDownloads] = useState(1);

  useEffect(() => {
    fetch('/api/counter')
      .then(res => res.json())
      .then(data => {
        if (data && data.count) setDownloads(data.count);
      })
      .catch(console.error);
  }, []);

  const handleDownloadClick = () => {
    // Optimistically update the UI instantly
    setDownloads(prev => prev + 1);

    // Increment count globally without blocking the download
    fetch('/api/counter/up')
      .then(res => res.json())
      .then(data => {
        if (data && data.count) setDownloads(data.count);
      })
      .catch(console.error);
  };

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
          <p className="dl-sub">Join <strong style={{color:'#00E5FF'}}>{downloads.toLocaleString()}</strong> others taking back control of their time.</p>

          <a href="focusguard.apk" download className="btn-primary btn-xl" onClick={handleDownloadClick}>
            <DownloadIcon size={22} /> Download APK – Free
          </a>

          <div className="dl-meta">
            <span>📱 Android 8.0+</span>
            <span>📦 ~15 MB</span>
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
