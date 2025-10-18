# VoltReserve – EV Charging Station Booking System ⚡

Android (Pure Kotlin) Mobile Application

![Thumbnail](images/VoltReserve-Mobile.png)

Final Year Assignment – Enterprise Application Development (SE4040)  
Sri Lanka Institute of Information Technology (SLIIT) – 2025

---

## 📖 Overview

VoltReserve is an **EV Charging Station Booking System** with:

- **Web Application (Backoffice + Station Operators)**
- **Mobile Application (EV Owners & Operators – Android with SQLite)**
- **Backend Service (C# .NET Web API + MongoDB Atlas, hosted on IIS)**

The system enables:

- Management of **web users** with roles **Backoffice** and **StationOperator**
- CRUD operations for **EV Owners** (NIC as primary key)
- **Station Management** (create, update, schedules, deactivate if no active bookings)
- **Booking Management** (create/update/cancel with time rules, QR validation, session finalize)

---

## 🏗️ Architecture

### Tech Stack

- **Backend:** ASP.NET Core Web API (C# 9.0+)
- **Database:** MongoDB Atlas (NoSQL)
- **Authentication:** JWT Bearer Tokens
- **Authorization:** Role-based (`Backoffice`, `StationOperator`)
- **Frontend (Web):** React + TailwindCSS/Bootstrap 5
- **Mobile:** Native Android (Java/Kotlin + SQLite)

### High-Level Flow

1. EV Owners create accounts (NIC = PK).
2. Owners make bookings (≤ 7 days ahead, changes/cancellations ≥ 12h before).
3. Backoffice manages users, stations, and schedules.
4. Station Operators monitor bookings and handle onsite QR code validation.
5. JWT-based role enforcement ensures security.

---

## 🔑 Roles & Authorization

- **Backoffice**

    - Can register users (Backoffice/StationOperator)
    - Can manage EV Owners
    - Can create/update/deactivate Stations & schedules
    - Can view/manage bookings

- **Station Operator**
    - Can log in (web/mobile)
    - Can view stations & bookings
    - Can confirm QR codes / finalize sessions

### Authorization Policies

Defined in `Program.cs`:

- `BackofficeOnly`
- `OperatorOrBackoffice`

---
