# RT (Race Tracker) - Frontend

**RT (Race Tracker)** is a physical performance tracking application designed for runners. It implements an offline-first architecture to log, switch between runner profiles, and analyze running metrics on both street and treadmill sessions.

---

## 🚀 Key Features

* **Multi-Profile Management:** Toggle between different runner profiles (e.g., family members or partners) locally. The active profile body weight is dynamically used to accurately calculate MET and calories.
* **Stopwatch Street Run:** A precise stopwatch logging street workouts. Powered by an RxJS `timer(0, 1000)` and verified with `Date.now()` timestamps to prevent tab-throttling drift.
* **Treadmill Run Form:** Post-run form logging configured machine distance, speed, and time. Features a built-in validation preventing treadmill-to-pace speed mismatch greater than 15%.
* **Quick-Log 5.5km:** A mobile-first Floating Action Button (FAB) at the dashboard to quickly bootstrap a street session with the standard 5.5km distance query parameter prefilled.
* **Offline-First Synchronization (Write-to-Local-First):** Sessions are written directly to `IndexedDB` first. When connection is available, the sync cycle triggers.
* **Idempotency Protection:** Employs an HTTP interceptor injecting the `X-Client-UUID` header to guarantee that network retries never generate duplicate runs in the backend.

---

## 🛠 Tech Stack

* **Framework:** Angular 19 (Standalone Components)
* **Local Database:** Dexie.js (IndexedDB wrapper)
* **Reactivity:** RxJS Observables and BehaviourSubjects
* **Styling:** Premium Dark Glassmorphism using Vanilla CSS

---

## 💻 Getting Started

### Prerequisites

* Node.js (v24.15.0+ or compatible)
* NPM

### Install Dependencies

```bash
npm install
```

### Development Server

Run the development server locally:

```bash
npm run start
```

Navigate to `http://localhost:4200/`. The app will automatically reload on any file changes.

### Production Build

To build the project:

```bash
npm run build
```

The build artifacts will be stored in the `dist/frontend` directory.

---

## 🧪 Testing

To execute unit tests:

```bash
npm run test
```
