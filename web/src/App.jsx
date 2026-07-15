import Navbar     from './components/Navbar'
import Hero       from './components/Hero'
import Features   from './components/Features'
import SetupGuide from './components/SetupGuide'
import Download   from './components/Download'
import Privacy    from './components/Privacy'
import Footer     from './components/Footer'

export default function App() {
  return (
    <>
      <Navbar />
      <Hero />
      <Features />
      <SetupGuide />
      <div className="bottom-split">
        <Download />
        <Privacy />
      </div>
      <Footer />
    </>
  )
}
