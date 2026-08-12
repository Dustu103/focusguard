# FocusGuard

<div align="center">
  <img src="https://img.shields.io/badge/Platform-Android_8.0+-3DDC84.svg?style=flat-square&logo=android" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF.svg?style=flat-square&logo=kotlin" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4.svg?style=flat-square&logo=android" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square" alt="License" />
</div>

<br/>

**FocusGuard** is a free, 100% offline, privacy-first Android application designed to help users manage screen time, block distractions, and reclaim their focus.

Unlike traditional screen-time applications that require expensive subscriptions, force cloud accounts, or sell user data, FocusGuard runs completely locally on your device.

## 🚀 Features

- **App & Domain Blocking:** Hard-blocks distracting apps and websites instantly.
- **Daily App Limits:** Enforces strict, customizable daily usage quotas (e.g., max 1 hour of TikTok per day).
- **Collective Lists (Profiles):** Group multiple distracting apps (e.g., "Social Media") and apply a shared timer or block them with a single tap.
- **Zero Battery Drain Architecture:** Utilizes an innovative O(1) in-memory tracker alongside Android's `UsageStatsManager` to track foreground time with practically zero battery impact.
- **VPN Sinkholing:** Uses a local, on-device `VpnService` to sinkhole DNS requests for distracting domains, preventing apps from loading new content.
- **Strict Mode & PIN Protection:** Includes a secure Parental/Commitment Mode with Device Administrator uninstall protection to prevent circumvention.
- **100% Offline:** Zero data collection. No trackers. All data stays in your local Room Database.

## 🏗 Technical Architecture

Building Digital Wellbeing apps natively on Android involves navigating undocumented APIs and aggressive OEM battery optimizations. 

FocusGuard implements several advanced architectural patterns:
1. **AccessibilityService Engine:** A robust background service that monitors window state changes for real-time foreground app tracking.
2. **In-Memory Usage Tracking:** To bypass the battery-draining necessity of polling `UsageStatsManager` twice a second, FocusGuard fetches a daily baseline exactly once upon app launch, and then increments an in-memory cache every 500ms using a lightweight foreground service.
3. **Local VpnService:** Dynamically routes traffic and blocks internet access for targeted applications using `addDisallowedApplication`.

## 🛠 Building from Source

### Prerequisites
- Android Studio (Jellyfish or newer)
- JDK 17+
- Android SDK (minSdkVersion 26, targetSdkVersion 34)

### Setup Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/Dustu103/focusguard.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle files.
4. Build and run on an emulator or physical device.

## 🌍 Web Infrastructure

The project includes a responsive, animated landing page built with **React, Vite, and Framer Motion**, located in the `web/` directory.

- Features a serverless proxy (`vercel.json`) to bypass browser adblockers (like Brave/uBlock) for anonymous global download tracking.
- Run locally:
  ```bash
  cd web
  npm install
  npm run dev
  ```

## 🤝 Contributing

Contributions are always welcome! If you are interested in reverse-engineering Android OS restrictions, improving the VPN tunneling logic, or bypassing aggressive OEM battery managers (Xiaomi/Samsung), we would love your help.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 🛡 License

Distributed under the MIT License. See `LICENSE` for more information.
