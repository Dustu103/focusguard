import { Download } from 'lucide-react'
import './shared.css'
import './Footer.css'
import AppIcon from './AppIcon'

export default function Footer() {
  return (
    <footer className="footer" id="help">
      <div className="footer-inner">
        <div className="footer-top">
          <a href="#hero" className="nav-logo footer-logo">
            <AppIcon size={28} />
            FocusGuard
          </a>
          <div className="footer-links">
            <a href="#features">Features</a>
            <a href="#setup">Setup Guide</a>
            <a href="#download">Download</a>
            <a href="#privacy">Privacy Policy</a>
          </div>
          <a href="focusguard.apk" download className="btn-primary footer-dl">
            <Download size={16}/> Download APK
          </a>
        </div>
        <div className="footer-divider" />
        <div className="footer-bottom">
          <div className="footer-contact">
            <h4>Need Help?</h4>
            <p>Email: <a href="mailto:arnabpramanik102018@gmail.com">arnabpramanik102018@gmail.com</a></p>
            <p>Phone: [8695204336(only whatsapp)]</p>
          </div>
        </div>
      </div>
    </footer>
  )
}
