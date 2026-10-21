CREATE DATABASE IF NOT EXISTS bookstore;
USE bookstore;

CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL
);

CREATE TABLE users (
    username VARCHAR(100) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

INSERT INTO users (username, password, role)
VALUES ('admin', 'admin123', 'Admin');

INSERT INTO users (username, password, role)
VALUES ('student', 'user123', 'User');

INSERT INTO books (title, author, price, quantity)
VALUES ('Java Basics', 'John Smith', 29.99, 10);


SELECT * FROM users;
SELECT * FROM books;