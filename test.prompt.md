Create a comprehensive Java Spring Boot Billing Application with the following specifications:

Backend Requirements:
1. Project Setup:
   - Use Maven for dependency management and build automation
   - Spring Boot latest stable version
   - MySQL database integration
   - Spring Security implementation
   - Swagger/OpenAPI documentation

2. Core Features:
   a. Product Management:
      - Full CRUD operations for products
      - Bulk product updates via CSV file upload
      - Product categorization and inventory tracking
   
   b. Invoice Management:
      - Create, read, update, and delete invoices
      - Generate invoice PDFs
      - Invoice status tracking (draft, sent, paid, overdue)
      - Invoice numbering system
   
   c. Reporting System:
      - Daily sales reports
      - Monthly revenue analysis
      - Yearly financial summaries
      - Product performance metrics
      - Custom date range reports
      - Export reports in PDF/Excel formats

3. User Management:
   - Role-based access control (Admin, User)
   - User registration with email verification
   - Admin approval workflow for new users
   - Dynamic database creation per approved user
   - Secure password handling

4. API Design:
   - RESTful API endpoints following best practices
   - Proper HTTP status codes and error handling
   - Request/Response DTOs
   - API versioning
   - CORS configuration for React frontend

5. Technical Requirements:
   - Comprehensive unit tests with minimum 80% coverage
   - Integration tests for critical workflows
   - Logging using SLF4J/Logback
   - Exception handling with custom error responses
   - Request validation
   - Performance optimization
   - Database indexing and optimization

Frontend Requirements (React):
1. User Interface:
   - Responsive dashboard
   - Product management interface
   - Invoice creation and management
   - Report generation and visualization
   - User management console for admins

2. Features:
   - Real-time data updates
   - Form validation
   - File upload interface
   - Data export functionality
   - Search and filter capabilities
   - Pagination for large datasets

3. Security:
   - JWT authentication
   - Protected routes
   - Session management
   - Secure API communication

Documentation Requirements:
1. API documentation with examples
2. Database schema documentation
3. Setup and deployment guides
4. User manual
5. Development guidelines

Please provide a scalable, maintainable solution following clean code principles and industry best practices.