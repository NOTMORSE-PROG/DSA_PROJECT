DB NAME: dsa_projects

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE flights (
    id INT AUTO_INCREMENT PRIMARY KEY,
    flight_name VARCHAR(100) NOT NULL,
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    cabin VARCHAR(50) NOT NULL,
    total_seats INT NOT NULL DEFAULT 60,  
    available_seats INT NOT NULL,
    booked_seats TEXT
);

CREATE TABLE tickets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    user_fullname VARCHAR(255),
    booking_id INT UNIQUE,
    flight VARCHAR(255),
    origin VARCHAR(255),
    destination VARCHAR(255),
    departure TIMESTAMP,
    seats_selected VARCHAR(255),
    price DECIMAL(10, 2),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE transaction_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    transaction_type ENUM('BOOKED', 'CANCELLED') NOT NULL,
    flight_name VARCHAR(255),
    seats_selected VARCHAR(255),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
