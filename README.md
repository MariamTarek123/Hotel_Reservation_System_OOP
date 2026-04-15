# 🏨 Hotel Reservation System

## 📌 Objective

This project implements the complete **backend structure** of a Hotel Reservation System using **core Object-Oriented Programming (OOP) principles in Java**.

---

## 🧱 System Design Overview

The system is structured around key entities such as:

* Guests
* Staff (Admin & Receptionist)
* Rooms & Room Types
* Amenities
* Reservations
* Invoices
* In-memory database

All classes follow **encapsulation, inheritance, abstraction, and polymorphism**.

---

## 👤 Guest Module

### Attributes

* `username` (String)
* `password` (String, validated)
* `dateOfBirth` (LocalDate / Date)
* `balance` (double)
* `address` (String)
* `gender` (Enum: `MALE`, `FEMALE`)
* `roomPreferences` (custom structure: preferred type, floor, etc.)

### Behaviors

* Register / Login
* View available rooms
* Make reservations
* View reservations
* Cancel reservations
* Checkout and pay invoice

---

## 🧑‍💼 Staff Module

### Structure

* Abstract parent class: `Staff`
* Subclasses:

  * `Admin`
  * `Receptionist`

### Common Attributes

* `username`
* `password`
* `dateOfBirth`
* `role` (Enum: `ADMIN`, `RECEPTIONIST`)
* `workingHours`

### Behaviors

#### Admin

* Full CRUD operations:

  * Rooms
  * Room Types
  * Amenities
* View all system data (guests, rooms, reservations)

#### Receptionist

* Manage:

  * Check-in
  * Check-out
* View guests and reservations

---

## 🛏️ Room System

### RoomType Class

Represents room categories:

* Single
* Double
* Suite

### Amenity Class

Represents room features:

* WiFi
* TV
* Mini-bar
* etc.

### Room Class

* Associated with:

  * One `RoomType`
  * List of `Amenity` objects

---

## 📅 Reservation System

### Reservation Class

#### Attributes

* Guest reference
* Room reference
* Check-in date
* Check-out date
* Status (Enum):

  * `PENDING`
  * `CONFIRMED`
  * `CANCELLED`
  * `COMPLETED`

---

## 💳 Invoice System

### Invoice Class

#### Attributes

* Total amount
* Payment method (Enum):

  * `CASH`
  * `CREDIT_CARD`
  * `ONLINE`
* Payment date

### Features

* Supports **multiple payment methods** during checkout

---

## ✅ Validation & Error Handling

* All fields are **private** with getters/setters
* Input validation includes:

  * Non-empty usernames
  * Valid date ranges
  * No negative balances
* Custom exceptions:

  * `RoomNotAvailableException`
  * `InvalidPaymentException`

---

## 🗄️ In-Memory Database

### HotelDatabase Class

Acts as the system's data storage using static collections:

```java
ArrayList<Guest> guests;
ArrayList<Room> rooms;
ArrayList<Reservation> reservations;
ArrayList<Invoice> invoices;
```

### Features

* Stores all system data in memory
* Pre-populated with dummy data for testing
* Enables quick testing without external database setup

---

## 🧪 Testing the System

* The system runs directly after launch using preloaded data
* You can simulate:

  * Guest registration & login
  * Room booking
  * Reservation management
  * Payment processing

---

## 🚀 Future Improvements

* Add GUI (JavaFX / Swing)


---


