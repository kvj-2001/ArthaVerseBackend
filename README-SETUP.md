# Billing Application - Setup and Run Guide

## Prerequisites

### Backend Requirements
- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+** (or use H2 for development)

### Frontend Requirements
- **Node.js 16+**
- **npm 8+** or **yarn**

## Database Setup

### Option 1: MySQL (Recommended for Production)
1. Install MySQL and create a database:
```sql
CREATE DATABASE billing_app;
CREATE USER 'billing_user'@'localhost' IDENTIFIED BY 'billing_password';
GRANT ALL PRIVILEGES ON billing_app.* TO 'billing_user'@'localhost';
FLUSH PRIVILEGES;
```

2. Update `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/billing_app
spring.datasource.username=billing_user
spring.datasource.password=billing_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### Option 2: H2 Database (For Development/Testing)
The application is already configured to use H2 for testing. To use H2 for development, create `application-dev.properties`:
```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.h2.console.enabled=true
```

## Running the Backend

### Method 1: Using Maven (Recommended)
```bash
# Navigate to the backend directory
cd c:\Test\BillingBackend

# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Method 2: Using Maven Wrapper
```bash
# On Windows
.\mvnw.cmd spring-boot:run

# On Linux/Mac
./mvnw spring-boot:run
```

### Method 3: Building and Running JAR
```bash
# Build the JAR
mvn clean package -DskipTests

# Run the JAR
java -jar target/billing-backend-0.0.1-SNAPSHOT.jar
```

## Running the Frontend

```bash
# Navigate to the frontend directory
cd c:\Test\BillingBackend\frontend

# Install dependencies
npm install

# Start the development server
npm start
```

The frontend will automatically open at `http://localhost:3000` and proxy API calls to the backend at `http://localhost:8080`.

## Accessing the Application

### URLs
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **H2 Console** (if using H2): http://localhost:8080/h2-console

### Default Admin User
When the application starts for the first time, a default admin user is automatically created:
- **Username**: admin
- **Password**: admin123
- **Email**: admin@billing.app
- **Role**: ADMIN

**Important**: 
- This admin user is only created if no user with username "admin" exists
- Change the default password immediately after first login for security
- You can manage users through the User Management interface once logged in

## Development Workflow

### 1. Start Backend
```bash
cd c:\Test\BillingBackend
mvn spring-boot:run
```

### 2. Start Frontend (in a new terminal)
```bash
cd c:\Test\BillingBackend\frontend
npm start
```

### 3. Access the Application
- Open http://localhost:3000
- Login with admin/admin123
- Start using the application!

## Troubleshooting

### Common Backend Issues

1. **Port 8080 already in use**
   - Add to `application.properties`: `server.port=8081`
   - Update frontend proxy in `package.json`

2. **Database connection errors**
   - Check MySQL is running
   - Verify database credentials
   - Ensure database exists

3. **JWT Secret key issues**
   - The application generates a random JWT secret on startup
   - For production, set: `app.jwt.secret=your-secret-key`

### Common Frontend Issues

1. **Port 3000 already in use**
   - The system will prompt to use a different port
   - Or set `PORT=3001` environment variable

2. **API connection errors**
   - Ensure backend is running on port 8080
   - Check proxy configuration in `package.json`

3. **Module not found errors**
   - Run `npm install` to install dependencies
   - Clear node_modules and reinstall if needed

## Production Deployment

### Backend
```bash
# Build for production
mvn clean package -Dmaven.test.skip=true

# Run with production profile
java -jar target/billing-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Frontend
```bash
# Build for production
npm run build

# Serve the build (using a simple server)
npx serve -s build -l 3000
```

## Testing

### Backend Tests
```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AuthServiceTest
```

### Frontend Tests
```bash
# Run tests
npm test

# Run tests with coverage
npm test -- --coverage
```

## API Documentation

Once the backend is running, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs JSON**: http://localhost:8080/v3/api-docs

## Email Configuration

For email functionality, update `application.properties`:
```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## Environment Variables

You can also use environment variables:
```bash
# Database
export DB_URL=jdbc:mysql://localhost:3306/billing_app
export DB_USERNAME=billing_user
export DB_PASSWORD=billing_password

# JWT
export JWT_SECRET=your-super-secret-jwt-key

# Email
export MAIL_HOST=smtp.gmail.com
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-password
```

## Next Steps

1. **Customize the application** according to your business needs
2. **Configure email settings** for invoice sending
3. **Set up production database** and environment
4. **Customize the frontend** branding and styling
5. **Add additional features** as needed

Enjoy your new billing application! 🚀
