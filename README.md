# FocusGuard

A premium, highly secure Android application designed for powerful self-control and distraction blocking.

## ✅ Completed Features & Implementations

### 1. Core Blocking Infrastructure
* **Accessibility Service Engine:** A robust background service that actively monitors foreground apps and instantly draws a system-level overlay (block screen) over prohibited applications.
* **Real-time Expiration Service:** A high-frequency polling coroutine within the background service that automatically forces restriction timers to expire exactly when they reach zero, seamlessly lifting blocks.
* **Usage Stats Integration:** Accurately tracks foreground activity to ensure immediate detection of newly launched apps.

### 2. High-Fidelity UI/UX & Dashboard
* **Dynamic Live Dashboard:** A stunning, premium dark-mode interface featuring real-time ticking countdown timers for active restrictions.
* **Mini-App Previews:** High-quality, dynamically loaded icon grids that visually display which apps are contained within a Quick List directly on the Dashboard.
* **Seamless Onboarding Flow:** A highly polished multi-step onboarding journey that clearly explains and requests necessary system permissions (Accessibility, Usage Access, Device Admin) sequentially.
* **Glassmorphism & Gradients:** Utilized advanced Compose animations, gradients, and spring physics to create a tactile and responsive user experience.

### 3. Advanced Security & Tamper Prevention
* **Device Administrator Lock:** Leveraged Android's Device Admin API to prevent the application from being uninstalled or force-stopped by the user while a focus session is active.
* **PIN Protection System:** A secure, persistent PIN gate required for sensitive actions such as disabling Device Admin, changing app modes, or attempting to unblock an app early.
* **Settings Loop Prevention:** Built logic to safely allow intentional disabling of Device Admin via the in-app Settings (with a PIN) without triggering aggressive re-enable loops.

### 4. Self-Focus & Quick Lists
* **Individual App Blocking:** Select specific apps to block for a custom duration (hours and minutes).
* **Quick Lists (App Groups):** Group multiple distracting apps into a named list (e.g., "Social Media") and block the entire cohort with a single tap.
* **Commitment Lock:** Designed around the philosophy of self-focus; once a timer is set, the user cannot easily quit or bypass the block until the time expires.
* **Smart App Dependencies:** Added custom logic to handle underlying dependencies (e.g., blocking the Google Search app engine automatically if the Gemini app is selected).

### 5. Future Foundations Laid
* **Parental Control Mode:** Laid the architectural groundwork and Mode Selection UI for a distinct "Parental Control" paradigm featuring permanent blocks (currently gated as "Coming Soon").
* **Local VPN Service:** Initiated the core structure for `FocusVpnService` to intercept and block specific website domains in the future.
* **Daily Limits & Schedules:** Reserved UI space and structural planning for time-of-day scheduling and daily app usage limits.
