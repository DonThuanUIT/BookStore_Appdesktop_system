# 📚 Book Store Management System

This is a project for the Java Programming course.

A desktop bookstore management application developed using Java.

The system supports:

* User authentication and role management
* Book management
* Category and author management
* Inventory management
* Order processing
* Shopping cart
* Voucher system
* Import management
* Order status tracking

---

# 📊 Database Schema

```mermaid
erDiagram
    USERS ||--o{ ORDERS : places
    USERS ||--o| CARTS : owns
    USERS ||--o{ IMPORTS : manages
    USERS ||--o{ VOUCHER_USAGES : uses

    BOOKS ||--o{ BOOK_AUTHORS : written_by
    AUTHORS ||--o{ BOOK_AUTHORS : writes

    BOOKS ||--o{ BOOK_CATEGORIES : categorized_as
    CATEGORIES ||--o{ BOOK_CATEGORIES : contains

    PUBLISHERS ||--o{ BOOKS : publishes

    BOOKS ||--|| INVENTORIES : stored_in
    BOOKS ||--o{ INVENTORY_LOGS : tracked_by

    ORDERS ||--o{ ORDER_DETAILS : contains
    BOOKS ||--o{ ORDER_DETAILS : purchased

    ORDERS ||--o{ ORDER_STATUS_LOGS : tracks

    IMPORTS ||--o{ IMPORT_DETAILS : includes
    BOOKS ||--o{ IMPORT_DETAILS : imported

    CARTS ||--o{ CART_ITEMS : contains
    BOOKS ||--o{ CART_ITEMS : added_to

    VOUCHERS ||--o{ VOUCHER_USAGES : applied_in
    ORDERS ||--o{ VOUCHER_USAGES : records

    USERS {
        int id PK
        string username UK
        string password_hash
        string email UK
        string full_name
        string phone
        string address
        enum role "ADMIN, STAFF, CUSTOMER"
        boolean is_deleted
    }

    CATEGORIES {
        int id PK
        string name
    }

    AUTHORS {
        int id PK
        string name
    }

    PUBLISHERS {
        int id PK
        string name
    }

    BOOKS {
        int id PK
        string title
        string isbn UK
        int publish_year
        decimal sell_price
        string image_url
        boolean is_deleted
        int publisher_id FK
    }

    BOOK_AUTHORS {
        int book_id FK
        int author_id FK
    }

    BOOK_CATEGORIES {
        int book_id FK
        int category_id FK
    }

    INVENTORIES {
        int book_id PK, FK
        int stock
    }

    INVENTORY_LOGS {
        int id PK
        int book_id FK
        int change_quantity
        enum type "IMPORT, SALE"
    }

    VOUCHERS {
        int id PK
        string code UK
        int discount_percent
        decimal discount_amount
        date expiry_date
        int usage_limit
        int used_count
    }

    ORDERS {
        int id PK
        int user_id FK
        decimal total_amount
        decimal discount
        decimal final_amount
        enum status "PENDING, PAID, SHIPPING, COMPLETED, CANCELLED"
    }

    ORDER_DETAILS {
        int id PK
        int order_id FK
        int book_id FK
        int quantity
        decimal price
    }

    ORDER_STATUS_LOGS {
        int id PK
        int order_id FK
        string status
        timestamp changed_at
    }

    IMPORTS {
        int id PK
        int staff_id FK
        decimal total_cost
    }

    IMPORT_DETAILS {
        int id PK
        int import_id FK
        int book_id FK
        int quantity
        decimal import_price
    }

    CARTS {
        int id PK
        int user_id FK
    }

    CART_ITEMS {
        int id PK
        int cart_id FK
        int book_id FK
        int quantity
    }

    VOUCHER_USAGES {
        int id PK
        int voucher_id FK
        int user_id FK
        int order_id FK
        timestamp used_at
    }
```

---

# 📌 Database Overview

| Module    | Description                                   |
| --------- | --------------------------------------------- |
| Users     | Manage users and roles                        |
| Books     | Manage books, authors, categories, publishers |
| Inventory | Manage stock and inventory logs               |
| Orders    | Handle customer orders                        |
| Imports   | Handle importing books                        |
| Cart      | Shopping cart management                      |
| Voucher   | Discount voucher management                   |

---

# 🔑 Main Relationships

* One publisher can publish many books
* One book can have many authors
* One book can belong to many categories
* One user can create many orders
* One order can contain many order details
* One cart can contain many cart items
* One voucher can be used in many orders

---

## 🛠️ Tech Stack

<div align="center">
  
  <img src="https://img.shields.io/badge/JAVA-17-F8981D?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/JAVAFX-17-FF7800?style=for-the-badge" />
  <img src="https://img.shields.io/badge/SPRING_BOOT-3.2.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img src="https://img.shields.io/badge/MYSQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/SPRING_DATA_JPA-3.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <br>
  <img src="https://img.shields.io/badge/SPRING_SECURITY-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white" />
  <img src="https://img.shields.io/badge/MAVEN-BUILD_SYSTEM-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
  <img src="https://img.shields.io/badge/CLOUDINARY-STORAGE-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white" />
  <img src="https://img.shields.io/badge/JWT-AUTH-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" />

</div>

---


# ✨ Features

* Authentication & Authorization
* Book CRUD
* Category CRUD
* Author CRUD
* Inventory Management
* Cart Management
* Order Management
* Voucher Management
* Import Management
* Soft Delete Support
