# Berry
Berry - Borrowing and Lending Tracker

# 🍓 Berry – Borrowing & Lending Tracker

**Berry** is a simple, strawberry-themed borrowing and lending tracker designed to help users keep track of items they borrow from others and lend to others.

It provides an easy way to record transactions, monitor due dates, and manage returned items through an Android application.

## ✨ Features

- **Add Records** – Record items that you borrow or lend.
- **View Records** – View all your borrowing and lending records.
- **Filter Records** – Separate borrowed and lent items.
- **Track Due Dates** – Keep track of borrowing and return dates.
- **Mark as Returned** – Update the status of returned items.
- **Delete Records** – Remove unwanted records.
- **Dashboard** – View total, borrowed, lent, and overdue item counts.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Application development |
| Android Studio | Android application development |
| XML | Android UI design |
| Spring Boot | Backend development |
| JDBC | Database connectivity |
| MySQL | Database management |

## 🏗️ Architecture

Berry follows a client-server architecture.

```text
Android Application
        |
        | HTTP Requests
        |
Spring Boot Backend
        |
        | JDBC
        |
   MySQL Database
```

The Android application communicates with the Spring Boot backend through REST API endpoints. The backend processes requests and interacts with the MySQL database using JDBC.

## 🎯 Project Objective

The main objective of Berry is to provide a simple and convenient solution for managing borrowed and lent items while maintaining organized records and tracking their return status.

## 🌱 Future Enhancements

- User authentication and account management
- Notifications for approaching due dates
- Improved search and filtering
- Enhanced user interface
- Cloud deployment

---

**Made with 🍓 and Java.**
