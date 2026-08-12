# Contributing to FocusGuard

Thank you for your interest in contributing! This guide will get you set up quickly.

---

## Getting Started

### Android App
1. Install [Android Studio](https://developer.android.com/studio) (Jellyfish or newer)
2. Clone the repo: `git clone https://github.com/Dustu103/focusguard.git`
3. Open the root project in Android Studio
4. Sync Gradle and run on a physical device (emulators don't support UsageStatsManager well)

### Backend
1. Install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
2. `cd backend && docker-compose up --build`
3. API runs at `http://localhost:3000`
4. Test the health check: `curl http://localhost:3000/health`

---

## How to Pick an Issue

1. Go to the [Issues tab](https://github.com/Dustu103/focusguard/issues)
2. Filter by `good first issue` if you are new to the codebase
3. Filter by `backend` or `android` based on your skills
4. **Comment on the issue** saying you want to work on it before you start — this prevents duplicate work

---

## Pull Request Rules

Every PR **must** include the following or it will not be reviewed:

### 1. One Test File
- Android feature → add a test in `app/src/test/` or `app/src/androidTest/`
- Backend feature → add a test in `backend/tests/`
- The test file must be named after the feature (e.g. `PairControllerTest.js`)

### 2. Visual Proof (Screenshot or Screen Recording)
- Android changes → attach a screen recording (`.mp4` or `.gif`) showing the feature working
- Backend changes → attach a screenshot of the API response (Postman, curl, or browser)
- **No visual proof = PR will not be merged**

### 3. Describe Every Change
- Fill out the PR template completely
- List every file you changed and what you did

### 4. Clean Code
- No leftover `Log.d()`, `console.log()`, or `TODO` comments
- Kotlin: 4-space indent. JavaScript: 2-space indent.
- Build must pass with zero errors

---

## Branch Naming

| Type | Format | Example |
|---|---|---|
| New feature | `feature/short-name` | `feature/remote-pairing-ui` |
| Bug fix | `fix/short-name` | `fix/timer-not-resetting` |
| Backend | `backend/short-name` | `backend/pair-endpoint` |
| Documentation | `docs/short-name` | `docs/contributing-guide` |

---

## Commit Messages

Use a simple format:
```
[scope] short description

Example:
[android] Add RemoteCommandSyncService polling loop
[backend] Implement POST /api/pair endpoint
[ui] Add +5 minute grace button to ChildBlockActivity
```

---

## Questions?

Open a [Discussion](https://github.com/Dustu103/focusguard/discussions) or comment on the relevant issue. We respond within 48 hours.
