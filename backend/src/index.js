// Entry point — Express server setup
const express = require('express')
const app = express()
const PORT = process.env.PORT || 3000

app.use(express.json())

// Routes
app.use('/api/pair', require('./routes/pair'))
app.use('/api/commands', require('./routes/commands'))

app.get('/health', (req, res) => res.json({ status: 'ok' }))

app.listen(PORT, () => console.log(`FocusGuard backend running on port ${PORT}`))
