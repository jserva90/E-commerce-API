# E-Commerce API Project

## Overview
This project implements a simple e-commerce cart/order flow, mirroring the functionality of a reference API located at https://homework.solutional.ee. It's designed to provide a basic understanding of handling e-commerce operations through RESTful API endpoints, including listing products, creating orders, and managing products within orders.

## Features
- **Product Listing**: Retrieve a list of all available products.
- **Order Management**: Create new orders and update existing ones.
- **Product Management in Orders**: Add products to orders, update quantities, and replace products during assembly.

## Getting Started

### Prerequisites
- JDK 17
- Docker
- PostgreSQL

### Setup with Docker
1. Clone the repository:
`git clone https://github.com/jserva90/E-commerce-API.git`
2. `cd e-commerce-api`
3. `docker-compose up --build -d`

### API Endpoints
#### Products
- GET /api/products: List of all available products.
#### Orders
- POST /api/orders: Create a new order.
- GET /api/orders/:order_id: Get order details.
- PATCH /api/orders/:order_id: Update an order's status to "PAID".
#### Order Products
- GET /api/orders/:order_id/products: Get products in an order.
- POST /api/orders/:order_id/products: Add products to an order.
- PATCH /api/orders/:order_id/products/:product_id: Update product quantity in an order.
- PATCH /api/orders/:order_id/products/:product_id: Add a replacement product in an order.