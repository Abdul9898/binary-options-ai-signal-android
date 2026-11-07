# ⚡ Binary Options AI Signal (Native Android)

A native Android application featuring real-time **CALL / PUT** binary options trading signals, technical indicator confluence, and automatic win-rate tracking — built with complete freedom and **no license key or subscription wall**.

---

## 🎯 Key Features

* **Instant Access (No License Key 🗝️):** Bypasses all paywalls and license screens. Launches directly into the live signal dashboard.
* **Independent Live Signal Engine:**
  * Real-time WebSocket tick streams (Crypto pairs: `BTC/USDT`, `ETH/USDT`, `SOL/USDT`).
  * Microstructure OTC synthetic currency feeds (`EUR/USD`, `GBP/USD`, `USD/JPY`, `AUD/USD`).
* **Technical Confluence Algorithmic Rules:**
  * **RSI (14):** Identifies oversold (< 35 $\rightarrow$ CALL) and overbought (> 65 $\rightarrow$ PUT) extremes.
  * **Bollinger Bands (20, 2):** Detects price bounces off outer bands.
  * **EMA (9 / 21) Momentum:** Cross-checks short-term trend continuation.
* **Real-Time Signal Cards:**
  * Strike price and live current price with winning/losing indicator.
  * 1-Minute (`1M`) and 5-Minute (`5M`) expiry modes.
  * Live ticking countdown timer until candle close.
  * Automatic settlement to **`WIN ✅`** or **`LOSS ❌`**.
* **Live Scorecard:** Tracks overall win rate (e.g. 92.4%), total wins/losses, and active win streaks.
* **Foreground Service & Sound Alerts:** Chimes and vibrates on high-probability setups even when the app is in the background.
* **Quick Broker Integration:** One-tap button to launch your trading broker (Pocket Option, Quotex).

---

## 🚀 How to Build & Install Your APK (Automated Cloud Build)

This project includes an automated GitHub Actions workflow (`.github/workflows/build-apk.yml`) that builds, signs, and releases your `.apk` in 2 minutes on GitHub's high-speed cloud runners.

### Step 1: Create a GitHub Repository
1. Open [github.com/new](https://github.com/new) in your phone's browser.
2. Set the repository name to: `binary-options-ai-signal-android` (select **Public** or **Private**).
3. Tap **Create repository**.

### Step 2: Push the Code from Termux
In your Termux terminal, run:
```bash
cd ~/projects/binary-options-ai-signal-android
git init
git add .
git commit -m "Initial commit: Binary Options AI Signal App"
git branch -M main
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/binary-options-ai-signal-android.git
git push -u origin main
```
*(Replace `<YOUR_GITHUB_USERNAME>` with your GitHub username).*

### Step 3: Download & Install Your APK
1. Open your repository on GitHub.
2. Tap the **Actions** tab (or **Releases** on the right side).
3. Tap the latest workflow run $\rightarrow$ scroll to **Artifacts** $\rightarrow$ tap **BinaryOptionsAISignal-APK** (or go to **Releases** and tap `BinaryOptionsAISignal.apk`).
4. Tap the downloaded file on your phone to **Install** and enjoy!
