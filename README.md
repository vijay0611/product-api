
# Products API

## Overview
The **Products API** is a RESTful service built with **Spring Boot** to manage products. It supports loading products from an external API, retrieving product data from an in-memory H2 database, and sorting products by price.

### Technologies Used:
- **Spring Boot** for the backend
- **Spring Data JPA** for database interactions
- **Swagger UI** for API documentation
- **H2 Database** (in-memory) for storing products
- **Resilience4j** for retries and circuit-breaking

## Features:
- Load products from an external API into the database.
- Retrieve products by category, ID, or SKU.
- Sort products by price (ascending or descending).
- Swagger UI for API documentation.
- In-memory H2 database for quick testing and development.

## API Endpoints

### 1. **Load Products**  
**Endpoint**: `GET /api/v1/products/load`  
Loads products from an external API into the database.

**Response**:
- **200 OK**: Products loaded successfully.
- **500 Internal Server Error**: Error loading products.

**Example Request**:
```bash
GET http://localhost:8080/api/v1/products/load
```

---

### 2. **Get All Products**  
**Endpoint**: `GET /api/v1/products`  
Retrieve all products or filter by **category**.

**Query Parameter**:  
- `category` (optional): A category to filter products.

**Response**:
- **200 OK**: List of products.
- **400 Bad Request**: Invalid category.
- **500 Internal Server Error**: Error fetching products.

**Example Request**:
```bash
GET http://localhost:8080/api/v1/products?category=Electronics
```

---

### 3. **Get Product by ID**  
**Endpoint**: `GET /api/v1/products/{id}`  
Retrieve a product by its ID.

**Response**:
- **200 OK**: Product details.
- **404 Not Found**: Product not found.

**Example Request**:
```bash
GET http://localhost:8080/api/v1/products/1
```

---

### 4. **Get Product by SKU**  
**Endpoint**: `GET /api/v1/products/sku/{sku}`  
Retrieve a product by its SKU.

**Response**:
- **200 OK**: Product details.
- **404 Not Found**: Product not found.

**Example Request**:
```bash
GET http://localhost:8080/api/v1/products/sku/SKU123
```

---

### 5. **Get Sorted Products by Price**  
**Endpoint**: `GET /api/v1/products/sorted`  
Retrieve products sorted by price.

**Query Parameter**:  
- `direction`: `asc` or `desc` for price sorting.

**Response**:
- **200 OK**: Sorted list of products.
- **400 Bad Request**: Invalid sort direction.

**Example Request**:
```bash
GET http://localhost:8080/api/v1/products/sorted?direction=asc
```

---

## H2 Database Console
You can access the H2 Database Console at:

```
http://localhost:8080/database-console
```

**Default Credentials**:
- **Username**: `admin`
- **Password**: `admin123`

## Running the Application

1. Build and run the project:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

2. Access the application at: `http://localhost:8080`.

