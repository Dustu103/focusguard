# FocusGuard Backend

Minimal Node.js/Express REST API for remote parental control.

## Stack
- **Runtime:** Node.js 18+
- **Framework:** Express
- **Database:** PostgreSQL (hosted free on Render)

## Folder Structure

```
backend/
├── src/
│   ├── config/
│   │   └── db.js              # PostgreSQL connection
│   ├── controllers/
│   │   ├── pairController.js  # Pairing logic
│   │   └── commandController.js # Command push/poll logic
│   ├── middleware/
│   │   └── auth.js            # Device token validation
│   ├── models/
│   │   ├── device.js          # Device schema
│   │   └── command.js         # Command schema
│   ├── routes/
│   │   ├── pair.js            # POST /api/pair
│   │   └── commands.js        # POST/GET /api/commands
│   └── index.js               # Express entry point
├── .env.example
├── .gitignore
└── package.json
```

## API Endpoints

| Method | Endpoint | Who | Purpose |
|---|---|---|---|
| POST | `/api/pair` | Parent | Generate a 6-digit pairing code |
| POST | `/api/pair/confirm` | Child | Enter code to link devices |
| POST | `/api/commands` | Parent | Send block/unblock command |
| GET | `/api/commands/:deviceId` | Child | Poll for pending commands |
| POST | `/api/commands/:id/ack` | Child | Acknowledge command done |

## Deploy on Render

1. Create a new **Web Service** on [render.com](https://render.com)
2. Connect your GitHub repo, set root directory to `backend/`
3. Set Build Command: `npm install`
4. Set Start Command: `npm start`
5. Add a **PostgreSQL** database on Render (free tier)
6. Copy the `DATABASE_URL` from Render into your environment variables
