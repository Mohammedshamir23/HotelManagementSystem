# 🏨 Hotel Management System

A Hotel Management System desktop application built using Java Swing and MySQL. It supports user registration, login, room booking, check-in/check-out, and more.

## ✨ Features

- User login and registration (admin and customer)
- Room booking with availability by category
- Check-in and check-out tracking
- View booking status
- Option to request food service
- MySQL database integration

## 🖥️ Technologies Used

- **Java** with **Swing** for the UI
- **MySQL** for database management
- **JDBC** for database connectivity

## 🗃️ Database Setup

1. Create a MySQL database named `hotel_db`.
2. Run the following SQL schema (or import the provided SQL file in `/database` folder):

```sql
CREATE DATABASE hotel_db;
USE hotel_db;

-- Add table creation SQL here (e.g., users, rooms, bookings)
