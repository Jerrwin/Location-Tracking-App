# LoTa Location Tracking App 📍

[![Java](https://img.shields.io/badge/Language-Java-orange)](https://www.java.com/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-blue)](https://firebase.google.com/)
[![Android Studio](https://img.shields.io/badge/IDE-Android%20Studio-green)](https://developer.android.com/studio)

A **location tracking Android application** for employees built with **Java** and **Firebase**.  
The app allows admins (reporting authorities) to track employee locations, view reports, and manage profiles efficiently.  

---

## Features

### Employee
- Secure login
- View personal profile
- Track login/logout activity

### Admin / Reporting Authority
- Dashboard to view all employees
- View employee location reports
- Access employee details
- Track login/logout history

---

## Tech Stack

- **Language:** Java  
- **Backend:** Firebase Realtime Database  
- **Authentication:** Firebase Authentication  
- **IDE:** Android Studio  
- **Minimum SDK:** 26 (Android 8.0 Oreo)  
- **Target SDK:** 34  

---

## Installation

1. Clone the repository:

```bash
git clone https://github.com/Jerrwin/Location-Tracking-App.git
```
2. Open the project in Android Studio.
3. Add your Firebase google-services.json file inside the app/ folder.
4. Sync Gradle and build the project.
5. Run the app on an emulator or a real device.

---

## Project Structure

    com.example.lota
    ├── activities          # Employee/Admin activities
    ├── adapters            # RecyclerView adapters
    ├── services            # Background services
    ├── helpers             # Helper classes
    └── res                 # Layouts, drawables, mipmaps, values, XML

---

## Contributing

Feel free to fork the repo, submit issues, or create pull requests.  
Maintain the project structure and coding style.
