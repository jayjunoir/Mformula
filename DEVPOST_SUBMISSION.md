# DEVPOST SUBMISSION — MFORMULA

---

### 1. App Store / Google Play Scheme URL
**Published Store URL:**
`https://play.google.com/store/apps/details?id=com.mformula`

---

### 2. Judge Access Note
> **Note for Hackathon Judges:**
> While the production package is indexed under the Google Play Store ID `com.mformula`, the standalone signed release APK (`androidApp-release.apk`) and Android App Bundle (`androidApp-release.aab`) are available for direct download on GitHub Releases or from the repository build deliverables:
> • **Signed Release APK:** `androidApp/build/outputs/apk/release/androidApp-release.apk`
> • **Signed Release AAB:** `androidApp/build/outputs/bundle/release/androidApp-release.aab`

---

### 3. In-App Purchase / Promo Code Field Text
> **In-App Purchase & Testing Information:**
> MFORMULA powers its in-app archive unlocks using the official **RevenueCat Purchases SDK**. For hackathon judging and testing, all premium features, past 14-day daily archives, and game modes are 100% unlocked and freely accessible without any mandatory paywalls or promo codes required.

---

### 4. Complete Project Description

## Inspiration
MFORMULA was inspired by classic word ladders and the tactile satisfaction of vintage arcade hardware. We wanted to transform standard text-based word puzzles into a kinetic, high-contrast, physical playfield using a bold **Neo-Brutalist design language** — complete with hardware letter dials, heavy drop shadows, CRT monitor scanlines, and particle confetti explosions.

## Core Game Mechanics
• **Single-Letter Mutation Rules:** Players morph a 4-letter starting word into a target mission word by changing exactly one letter per step. Every intermediate word must exist in the curated dictionary.
• **Bidirectional BFS Solver & Par Algorithm:** Powered by a non-trivial Bidirectional Breadth-First Search (BFS) algorithm that computes optimal shortest paths and branching decoy paths to set realistic golf-style Par scores (4 to 6 moves).
• **Deterministic Daily Seeding:** PRNG deterministic seeding generates a unique daily puzzle synchronized globally across all timezones based on epoch days.
• **Tactile Error Feedback Controller:** Submitting an invalid or unvisited word triggers a physical horizontal shake offset combined with an electric red border pulse and haptic vibration feedback.
• **Wordle-Style Share Generator:** Formats solution steps into a green/white tile ASCII matrix ready for instant system clipboard sharing.

## Architecture & Tech Stack
• **UI Framework:** Jetpack Compose Multiplatform (Material 3) with custom Canvas rendering for dot matrix backgrounds, scanline vignettes, and 75-chit confetti explosions.
• **Cross-Platform Shared Logic:** Kotlin Multiplatform (KMP) shared UI and domain layers.
• **In-App Purchases:** Integrated with the official RevenueCat Purchases SDK (`com.revenuecat.purchases:purchases`).
• **Local Storage:** Multiplatform Settings with `InMemorySettings` pre-flight fallback.
• **OWASP Mobile Security:** Hardened with R8 code minification, resource shrinking, disabled cleartext traffic, and zero dangerous runtime permissions.

---

### 5. Asset Verification Summary

• **App Icon:** $1024 \times 1024$ PNG (`ic_launcher.png` / Vector Adaptive Icon)
• **Screenshot:** $1179 \times 2556$ PNG (Zero device bezels/frames)
