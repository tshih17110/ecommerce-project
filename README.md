# E-Commerce REST API

A RESTful e-commerce backend built with Spring Boot, featuring JWT authentication and SQLite persistence.

### Installation

1. Clone the repository
2. Copy the example properties file
   ```
   cp src/main/resources/application.properties.example src/main/resources/application-local.properties
   ```
3. Replace placeholder values in `application-local.properties` with a generated JWT secret
4. Run the application
   ```
   ./mvnw compile spring-boot:run
   ```

### Configuration

Generate a JWT secret by running:

```
openssl rand -base64 32
```

Set the generated value as `jwt.secret` in `application-local.properties`

### Running tests

Run all unit tests:

```
./mvnw test
```

## API Endpoints

### Authentication

| Method | Endpoint            | Access |
| ------ | ------------------- | ------ |
| POST   | /api/users/register | Public |
| POST   | /api/users/login    | Public |

### Users

| Method | Endpoint        | Access       |
| ------ | --------------- | ------------ |
| GET    | /api/users/{id} | Admin, Owner |
| GET    | /api/users      | Admin        |
| PUT    | /api/users/{id} | Admin, Owner |
| DELETE | /api/users/{id} | Admin, Owner |

### Products

| Method | Endpoint           | Access        |
| ------ | ------------------ | ------------- |
| POST   | /api/products      | Admin         |
| GET    | /api/products/{id} | Authenticated |
| GET    | /api/products      | Authenticated |
| PUT    | /api/products/{id} | Admin         |
| DELETE | /api/products/{id} | Admin         |

### Cart

| Method | Endpoint        | Access        |
| ------ | --------------- | ------------- |
| GET    | /api/cart       | Authenticated |
| POST   | /api/cart/items | Authenticated |
| PUT    | /api/cart/items | Authenticated |
| DELETE | /api/cart/items | Authenticated |
| DELETE | /api/cart       | Authenticated |

### Orders

| Method | Endpoint                  | Access        |
| ------ | ------------------------- | ------------- |
| POST   | /api/orders               | Authenticated |
| GET    | /api/orders/{id}          | Admin, Owner  |
| GET    | /api/orders/my-orders     | Authenticated |
| GET    | /api/orders/user/{userId} | Admin         |
| PATCH  | /api/orders/{id}/status   | Admin         |
| PATCH  | /api/orders/{id}/cancel   | Admin, Owner  |

## Unit Tests

Unit tests written in JUnit and Mockito, focusing on service layer

`OrderService`: order creation, stock deduction, status updates, cancellation, and access control

IN PROGRESS: \
`CartService`\
`ProductService` \
`UserService`

## Key Features

- JWT stateless authentication
- Role-based access control (ADMIN, CUSTOMER)
- Automatic cart creation on first item add
- Stock validation on cart and order operations
- Order cancellation with automatic stock restoration
- Paginated responses for users, products, and orders
