import { useState, useEffect } from 'react'
import { Download } from 'lucide-react'
import './shared.css'
import AppIcon from './AppIcon'

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false)

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 30)
    window.addEventListener('scroll', onScroll)
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  return (
    <nav className={`navbar ${scrolled ? 'scrolled' : ''}`}>
      <div className="nav-inner">
        <a href="#hero" className="nav-logo">
          <AppIcon size={32} />
          FocusGuard
        </a>
        <div className="nav-links">
          <a href="#features">Features</a>
          <a href="#setup">Setup</a>
          <a href="#download">Download</a>
          <a href="#privacy">Privacy</a>
          <a href="#help">Help</a>
        </div>
        <a href="focusguard.apk" download className="btn-nav nav-links a" onClick={() => fetch('https://api.counterapi.dev/v1/focusguard/downloads/up').catch(()=>{})} style={{
          background:'linear-gradient(135deg,#00E5FF,#00B8CC)',
          color:'#000', fontWeight:700, borderRadius:10,
          padding:'0.45rem 1.2rem', textDecoration:'none',
          display:'flex', alignItems:'center', gap:6, fontSize:'0.88rem',
          transition:'all 0.2s', flexShrink:0
        }}>
          <Download size={14}/> Download Free
        </a>
      </div>
    </nav>
  )
}
