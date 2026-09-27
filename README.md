# 💰 TapSpend

> **Track it. Tap it. Done.**

TapSpend is a modern, speed-first Android expense-tracking application designed for **everyone** — students, salaried employees, freelancers, business owners, and families.

---

## ⚡ Core Philosophy: 3 Seconds to Record

```text
[ Notification ] ➔ [ 💵 Amount Keypad ] ➔ [ 🏷️ Category ] ➔ [ Optional Note ] ➔ [ Done ]
```

- **Zero manual date or time entry** (auto-detected).
- **Zero forced account creations or cloud logins** (100% offline & local).
- **Default currency**: **PKR (Rs)**.

---

## 📱 Platform Architecture

This project is built natively for **Android**:
- **UI Framework:** Kotlin + Jetpack Compose + Material 3
- **Local Database:** Room SQLite (Offline-First)
- **Preferences:** Jetpack DataStore
- **Architecture:** Clean Architecture + MVVM + Coroutines / Flows
- **Notifications:** Android Notification Manager with low-priority ongoing notification and direct `PendingIntent` action buttons.
- **Web Demo & Prototype:** Interactive single-page mobile app in `web-demo/` with luxury dark fintech glassmorphism.

---

## 🧠 Smart Natural Language Input

Users can type natural expressions into the search bar:
- `250 biryani` ➔ **Rs 250 · 🍔 Food**
- `500 petrol` ➔ **Rs 500 · 🚕 Travel**
- `80 chai` ➔ **Rs 80 · ☕ Coffee**
- `1200 groceries` ➔ **Rs 1,200 · 🛒 Shopping**

---

## 📊 Universal Dashboard & Optional Income

1. **Pure Expense Tracking (Default):**
   - Today's Spending
   - This Week
   - This Month
   - Total All-Time Spending
   - Category spending breakdown

2. **Optional Cash-Flow Mode:**
   - Supports **Monthly**, **Weekly**, **Daily**, or **Irregular / Freelance** income.
   - Computes **Income ➔ Spent ➔ Remaining** without locking the user into rigid accounting.

---

## 🚀 How to Run

### Android Studio:
1. Open the repository root in Android Studio (Giraffe / Hedgehog or newer).
2. Sync Gradle files (`build.gradle.kts`).
3. Run on an Android device or emulator running Android 8.0 (API 26) through Android 15 (API 35).

### Web Interactive Prototype:
```bash
cd web-demo
python3 -m http.server 3000
```
Open `http://localhost:3000` in your browser.
