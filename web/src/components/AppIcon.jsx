/* Reusable SVG App Icon */
export default function AppIcon({ size = 40, rounded = false }) {
  const r = rounded ? size * 0.22 : 0
  return (
    <svg width={size} height={size} viewBox="0 0 108 108" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="108" height="108" rx={r} fill="#151A2C"/>
      <path fill="#00E5FF" d="M54,18 L24,31 v25 c0,22 13,42 30,52 c17,-10 30,-30 30,-52 V31 L54,18 z M54,26 L76,35 v21 c0,18 -10,34 -22,43 c-12,-9 -22,-25 -22,-43 V35 L54,26 z"/>
      <path fill="#00E5FF" d="M54,45 c-4.4,0 -8,3.6 -8,8 c0,3.3 2,6.1 4.8,7.3 l-1.8,7.7 h10 l-1.8,-7.7 c2.8,-1.2 4.8,-4 4.8,-7.3 C62,48.6 58.4,45 54,45 z"/>
    </svg>
  )
}
