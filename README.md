# Comprehensive Billing Application Backend

A full-featured billing application built with Spring Boot, featuring user management, product management, invoice generation, and comprehensive reporting.

## Features

### Backend Features
- **User Management**
  - User registration with email verification
  - Admin approval workflow
  - Role-based access control (Admin, User)
  - JWT authentication
  - Dynamic database creation per user

- **Product Management**
  - Full CRUD operations
  - Bulk product upload via CSV
  - Product categorization
  - Inventory tracking
  - Low stock alerts

- **Invoice Management**
  - Create, read, update, delete invoices
  - PDF generation
  - Invoice status tracking (draft, sent, paid, overdue)
  - Automatic invoice numbering

- **Reporting System**
  - Daily, monthly, yearly sales reports
  - Custom date range reports
  - Product performance analytics
  - Export to PDF/Excel formats

- **Technical Features**
  - RESTful API design
  - Comprehensive validation
  - Exception handling
  - Request/Response DTOs
  - API documentation with Swagger
  - Unit and integration tests

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.1
- **Database**: MySQL 8.0
- **Authentication**: Spring Security with JWT
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **PDF Generation**: iText
- **Excel Export**: Apache POI
- **CSV Processing**: Apache Commons CSV
- **Testing**: JUnit 5, Mockito, H2 Database
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd billing-backend
```

### 2. Database Setup

1. Install MySQL and create a database:
```sql
CREATE DATABASE billing_app;
```

2. Update `src/main/resources/application.properties` with your database credentials:
```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Email Configuration

Update the email configuration in `application.properties`:
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

For Gmail, you'll need to:
1. Enable 2-factor authentication
2. Generate an app-specific password
3. Use the app password in the configuration

### 4. Build and Run

```bash
# Build the application
mvn clean compile

# Run tests
mvn test

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### 5. Access API Documentation

Once the application is running, you can access:
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- API Docs JSON: `http://localhost:8080/api/v3/api-docs`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `GET /api/auth/verify-email` - Verify email
- `GET /api/auth/pending-users` - Get pending users (Admin)
- `PUT /api/auth/approve-user/{id}` - Approve user (Admin)
- `PUT /api/auth/reject-user/{id}` - Reject user (Admin)

### Products
- `GET /api/products` - Get all products (paginated)
- `POST /api/products` - Create product
- `GET /api/products/{id}` - Get product by ID
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `GET /api/products/search` - Search products
- `GET /api/products/categories` - Get all categories
- `GET /api/products/low-stock` - Get low stock products
- `POST /api/products/bulk-upload` - Bulk upload via CSV

### Invoices
- `GET /api/invoices` - Get all invoices (paginated)
- `POST /api/invoices` - Create invoice
- `GET /api/invoices/{id}` - Get invoice by ID
- `PUT /api/invoices/{id}` - Update invoice
- `DELETE /api/invoices/{id}` - Delete invoice
- `GET /api/invoices/search` - Search invoices
- `GET /api/invoices/status/{status}` - Get invoices by status
- `GET /api/invoices/overdue` - Get overdue invoices
- `PATCH /api/invoices/{id}/status` - Update invoice status
- `GET /api/invoices/{id}/pdf` - Generate invoice PDF

### Reports
- `GET /api/reports/daily/{date}` - Daily sales report
- `GET /api/reports/monthly/{year}/{month}` - Monthly sales report
- `GET /api/reports/yearly/{year}` - Yearly sales report
- `GET /api/reports/custom` - Custom date range report
- `GET /api/reports/product-performance` - Product performance report
- `GET /api/reports/daily/{date}/excel` - Export daily report to Excel
- `GET /api/reports/monthly/{year}/{month}/excel` - Export monthly report to Excel

## Usage Examples

### 1. User Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
  }'
```

### 2. User Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

### 3. Create Product (with JWT token)
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "code": "PROD001",
    "name": "Sample Product",
    "description": "A sample product",
    "price": 29.99,
    "quantity": 100,
    "minStockLevel": 10,
    "category": "Electronics",
    "unit": "pcs"
  }'
```

### 4. Create Invoice
```bash
curl -X POST http://localhost:8080/api/invoices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "invoiceDate": "2024-01-15",
    "dueDate": "2024-02-15",
    "customerName": "ABC Company",
    "customerEmail": "contact@abc.com",
    "customerPhone": "+1234567890",
    "customerAddress": "123 Business St, City, State",
    "taxAmount": 5.99,
    "discountAmount": 0,
    "notes": "Thank you for your business",
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "unitPrice": 29.99,
        "description": "Sample Product"
      }
    ]
  }'
```

## CSV Bulk Upload Format

For bulk product upload, use the following CSV format:

```csv
code,name,description,price,quantity,minStockLevel,category,unit
PROD001,Product 1,Description 1,29.99,100,10,Electronics,pcs
PROD002,Product 2,Description 2,49.99,50,5,Electronics,pcs
```

## Testing

Run tests with different profiles:

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run integration tests
mvn test -Dtest=**/*IT
```

## Development Guidelines

### Code Structure
- **Entity**: JPA entities representing database tables
- **Repository**: Data access layer using Spring Data JPA
- **Service**: Business logic layer
- **Controller**: REST API endpoints
- **DTO**: Data Transfer Objects for API requests/responses
- **Exception**: Custom exception classes
- **Config**: Configuration classes
- **Security**: Security-related classes (JWT, authentication)

### Best Practices
1. Use DTOs for API communication
2. Implement proper validation using Bean Validation
3. Handle exceptions gracefully with custom error responses
4. Write comprehensive unit and integration tests
5. Use proper HTTP status codes
6. Document APIs with Swagger annotations
7. Follow REST conventions
8. Implement proper logging

## Deployment

### Production Configuration

1. Update `application-prod.properties`:
```properties
spring.profiles.active=prod
spring.datasource.url=jdbc:mysql://production-host:3306/billing_app
spring.jpa.hibernate.ddl-auto=validate
logging.level.com.billing=INFO
```

2. Build JAR file:
```bash
mvn clean package -DskipTests
```

3. Run in production:
```bash
java -jar target/billing-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker Deployment

Create `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/billing-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build and run:
```bash
docker build -t billing-backend .
docker run -p 8080:8080 billing-backend
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, email support@billing-app.com or create an issue in the repository.
