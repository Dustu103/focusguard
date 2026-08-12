# FocusGuard Backend

<div align="center">
  <img src="https://img.shields.io/badge/Node.js-18+-339933.svg?style=flat-square&logo=node.js" alt="Node.js" />
  <img src="https://img.shields.io/badge/Express-4.x-000000.svg?style=flat-square&logo=express" alt="Express" />
  <img src="https://img.shields.io/badge/PostgreSQL-15-4169E1.svg?style=flat-square&logo=postgresql" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Docker-ready-2496ED.svg?style=flat-square&logo=docker" alt="Docker" />
  <img src="https://img.shields.io/badge/Deploy-Render-46E3B7.svg?style=flat-square" alt="Render" />
</div>

<br/>

Minimal REST API backend for **FocusGuard's remote Parental Control** feature. Enables a parent's device to securely pair with a child's device and send real-time block/unblock commands — without any third-party cloud dependency.

> **This is intentionally minimal.** No authentication frameworks, no ORMs. Just Express + PostgreSQL + plain SQL. Easy to read, easy to contribute to.

---

## Architecture

```
Parent Phone ──► POST /api/commands ──► Render (This Server) ──► PostgreSQL
                                                │
Child Phone  ──► GET  /api/commands/:id  ◄──────┘  (polls every 30s)
```

The child's app polls the server every 30 seconds for new commands. When it receives and executes a command (e.g., block Instagram), it calls the `/ack` endpoint to mark it as done. No websockets, no push notifications — simple, reliable, and works behind any firewall.

---

## API Endpoints

| Method | Endpoint | Who calls it | Purpose |
|---|---|---|---|
| `POST` | `/api/pair` | Parent app | Generate a 6-digit pairing code |
| `POST` | `/api/pair/confirm` | Child app | Enter code to link devices |
| `POST` | `/api/commands` | Parent app | Send a block/unblock command |
| `GET` | `/api/commands/:deviceId` | Child app | Poll for pending commands |
| `POST` | `/api/commands/:id/ack` | Child app | Acknowledge command was executed |
| `GET` | `/health` | Anyone | Health check |

---

## Folder Structure

```
backend/
├── src/
│   ├── config/
│   │   └── db.js                  # PostgreSQL connection pool
│   ├── controllers/
│   │   ├── pairController.js      # Device pairing logic
│   │   └── commandController.js   # Command push, poll, and ack logic
│   ├── middleware/
│   │   └── auth.js                # Device token validation
│   ├── models/
│   │   ├── device.js              # Device schema { deviceId, role, pairedWith }
│   │   └── command.js             # Command schema { type, packageName, status }
│   ├── routes/
│   │   ├── pair.js                # /api/pair routes
│   │   └── commands.js            # /api/commands routes
│   └── index.js                   # Express entry point
├── .dockerignore
├── .env.example                   # Environment variable template
├── .gitignore
├── Dockerfile                     # Production image (node:18-alpine)
├── docker-compose.yml             # Local dev: API + PostgreSQL together
└── package.json
```

---

## 🐳 Running Locally with Docker

The easiest way to get started. No need to install PostgreSQL separately.

```bash
# 1. Clone the repo
git clone https://github.com/Dustu103/focusguard.git
cd focusguard/backend

# 2. Copy the environment template
cp .env.example .env

# 3. Start everything (API + PostgreSQL)
docker-compose up --build
```

The API will be running at **http://localhost:3000**

Verify it's working:
```bash
curl http://localhost:3000/health
# → { "status": "ok" }
```

To stop:
```bash
docker-compose down

# To also delete the database volume:
docker-compose down -v
```

---

## 🚀 Deploying to Render (Free Tier)

Render provides free hosting for Node.js web services and a free PostgreSQL database — perfect for this project.

1. Fork this repository.
2. Go to [render.com](https://render.com) and create a new **Web Service**.
3. Connect your forked GitHub repository.
4. Set the **Root Directory** to `backend/`.
5. Set **Build Command**: `npm install`
6. Set **Start Command**: `npm start`
7. Create a free **PostgreSQL** database on Render.
8. Copy the `DATABASE_URL` from the database dashboard into your Web Service's **Environment Variables**.
9. Add a `JWT_SECRET` environment variable with a strong random string.

Your API will be live at `https://your-service-name.onrender.com`.

---

## ⚙️ Environment Variables

Copy `.env.example` to `.env` and fill in the values:

| Variable | Description |
|---|---|
| `PORT` | Port the server listens on (default: `3000`) |
| `DATABASE_URL` | PostgreSQL connection string |
| `JWT_SECRET` | Secret key for signing device tokens |

---

## 🤝 Contributing

This backend is intentionally simple so that new contributors can get up to speed quickly. If you want to improve it:

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'Add your feature'`
4. Push and open a Pull Request.

Ideas for contributions:
- Implement the actual route handlers in `controllers/`
- Add database migration scripts
- Add rate limiting to prevent command spam
- Write integration tests

---

## 📄 License

MIT — see the root [`LICENSE`](../LICENSE) file for details.
