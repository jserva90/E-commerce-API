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

### Choosing custom port
#### Approach 1. Using start.sh script.
`bash start.sh <server-port>` for example bash start.sh 5000
###### If no port is chosen it will default to 8080

#### Approach 2. If you don't want to use the start.sh script.
1. CMD `set APP_PORT=<server-port>`
2. PowerShell `$env:APP_PORT=<server-port>`
3. Linux/macOS `export APP_PORT=<server-port>`

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
  
### API Documentation with Swagger
My E-Commerce API is fully documented using Swagger, which is accessible via SpringDoc OpenAPI integration. Swagger provides a comprehensive and interactive documentation of all available API endpoints, models, and their respective request/response schemas. This allows for easy testing and exploration of the API's capabilities directly through the user interface.

Accessing Swagger UI
To access the Swagger UI and explore the API documentation, ensure the application is running and navigate to:
`http://localhost:<server-port>/swagger-ui.html`