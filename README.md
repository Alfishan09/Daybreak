# Daybreak – Alarm Clock App

### A better way to manage your time. ⏰

Daybreak is a modern Android alarm clock application built with Kotlin and Jetpack Compose. It provides a clean and intuitive experience for creating, managing, and controlling multiple alarms with customizable tones, persistent local storage, and reliable alarm notifications.

---

## ✨ Features

- *Current Time & Date*  
  Displays the current time and date dynamically on the home screen.

- *Multiple Alarms*  
  Create and manage multiple alarms according to your schedule.

- *Time Picker*  
  Set alarm times easily using the native Android time picker.

- *Custom Alarm Tones*  
  Choose an alarm tone from the available device sounds.

- *Alarm List*  
  View all configured alarms in a clean and organized interface.

- *ON/OFF Control*  
  Enable or disable individual alarms whenever required.

- *Delete Alarm*  
  Remove alarms that are no longer needed.

- *Snooze*  
  Snooze a ringing alarm for 5 minutes.

- *Dismiss*  
  Stop a ringing alarm immediately.

- *Continuous Alarm Sound*  
  The selected alarm tone continues playing until the alarm is snoozed or dismissed.

- *Persistent Local Storage*  
  Alarm details are stored locally so they remain available after reopening the application.

- *Foreground Alarm Service*  
  Uses an Android foreground service to handle continuous alarm playback reliably.

- *Clean & Minimal UI*  
  Designed with a modern, user-friendly interface using Jetpack Compose and Material 3.

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| *Kotlin* | Application development |
| *Jetpack Compose* | Declarative UI development |
| *Material 3* | Modern UI components and design |
| *Android AlarmManager* | Alarm scheduling |
| *BroadcastReceiver* | Handling alarm events |
| *Foreground Service* | Continuous alarm playback |
| *SharedPreferences* | Local alarm persistence |
| *Android Notifications* | Alarm alerts and actions |
| *Android Studio* | Development environment |

---

## 📱 How It Works

1. Launch *Daybreak* to view the current time and date.
2. Tap *+ Add Alarm*.
3. Select the desired alarm time.
4. Choose an alarm tone.
5. The alarm is added to the alarm list.
6. Use the *ON/OFF toggle* to control the alarm.
7. When the alarm rings:
    - *Snooze* postpones it for 5 minutes.
    - *Dismiss* stops the alarm immediately.

---

## 🎨 User Interface

Daybreak follows a minimal and clean visual style with:

- Soft neutral background
- Clear typography
- Rounded cards
- Simple alarm controls
- Consistent spacing
- Material 3 components
- User-friendly interaction patterns

---

## 📂 Project Structure

text
Daybreak
│
├── app
│   └── src
│       └── main
│           ├── java
│           │   └── com.example.daybreak
│           │       ├── ui.theme
│           │       ├── AlarmReceiver.kt
│           │       ├── AlarmService.kt
│           │       └── MainActivity.kt
│           │
│           ├── res
│           └── AndroidManifest.xml
│
├── Screenshots
├── README.md
├── build.gradle.kts
└── settings.gradle.kts


---

## 🚀 Getting Started

### Run Locally

1. Clone or download this repository.
2. Open the project in *Android Studio*.
3. Allow Gradle to sync and complete the project setup.
4. Select an *Android Emulator* or connected Android device.
5. Click *Run ▶️* to launch Daybreak.
6. Allow the required notification and alarm permissions.
7. Create an alarm and test the *Snooze* and *Dismiss* actions.

---

## 📸 Screenshots

Screenshots showcasing the application's interface and key features are included in the repository.

---

## 👩‍💻 Developer

*Alfishan Khan*

B.Tech – Computer Science & Engineering
(Artificial Intelligence & Machine Learning)

---

## 🎓 Internship

This project was developed as part of the *Android App Development Internship at Tech Guide*.

---

⭐ *Built with Kotlin, Jetpack Compose, and a focus on clean Android development.*~~~~