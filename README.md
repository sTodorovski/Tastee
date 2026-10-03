# Tastee

Java/SpringBoot/Kotlin based fast-food ordering application that uses PostgreSQL.

---

## Features

* Customer, delivery driver and restaurant order support for placing fast food orders and restaurant editing.
* Analytics and options for the restaurant managing process
* Notification and map system for delivery drivers.
* Payment and delivery system for the customers.

## Tech stack

* Backend: Java 21 + Spring Framework 4.1.0
* Frontend: Kotlin 2.3.10
* Android Emulator: Pixel 9 with Android 17.0
* Database: PostgreSQL

## Setup

### 1. Prerequisites
Ensure you have **Git** installed.

### 2. Clone the repository
```
git clone https://github.com/sTodorovski/Tastee.git
cd Tastee
```

### 3. Environment variables
Create a `.env` file in the root directory and add the necessary API keys and other data:
* DB_USERNAME - Username for the database admin
* DB_PASSWORD - The database admin's password
* FOURSQUARE_API_KEY - An API key for the Foursquare platform that supplies the project with all the restaurants in North Macedonia
* MAIL_USERNAME - An email
* MAIL_PASSWORD - Automatically generated password for the email
* STRIPE_SECRET_KEY - A secret Stripe key that allows the project to make Stripe transactions
* STRIPE_WEBHOOK_SECRET - A Stripe webhook key that allows the project to send and receive successful/unsuccessful transaction notifications

### 4. Database
Create a PostgreSQL database and connect it to the project.

### 5. Launching
* Run the backend side of the app. The seeders and generators (CoverGenerator, DishSeeder, LogoGenerator, ProfilePictureGenerator, RestaurantSeeder and UserSeeder) will generate the users, dishes and ratings that will seed the database for future use.
* After the database is seeded launch the frontend portion of the app in an Android Emulator (preferable Android Studio).
* **IMPORTANT:** For the Stripe connection to work a Stripe webhook connection must be established first using ```stripe listen --events payment_intent.succeeded --forward-to localhost:8080/stripe/webhook``` in a command prompt.

### 6. Process
There are three types of users:
* **Customer:** A user that places orders and reviews from a list of restaurants. The user can add dishes to their cart and pay with a credit card or cash upon delivery. They can also leave reviews for the restaurants.
* **Delivery driver:** A type of user that accepts orders and delivers them to the customer.
* **Restaurant owner:** A type of user that creates and manages a restaurant with added dishes. The owner can edit the restaurant menu and add new dishes, arrange the opening and closing time for the restaurant and view order/review histories and metrics.
