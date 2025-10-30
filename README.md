# Personal Finance Tracker

A modern personal finance management application built with Java Spring Boot backend and a responsive web frontend, inspired by Revolut's sleek design.

![Finance Tracker Screenshot](screenshot.png)

## Features

### 📊 **Expense Tracking**
- Add, edit, and categorize daily expenses
- Multiple payment methods support
- Recurring expense tracking
- Real-time expense monitoring

### 💰 **Budget Management**
- Set monthly/yearly budgets by category
- Track spending progress with visual indicators
- Smart notifications when approaching limits
- Budget exceeded alerts

### 📈 **Analytics & Insights**
- Interactive spending charts (Chart.js)
- Category-wise expense breakdown
- Monthly spending trends
- Visual spending analysis

### 🎨 **Modern UI/UX**
- Revolut-inspired design
- Responsive mobile-first approach
- Dark theme with gradient effects
- Smooth animations and transitions
- Glass morphism effects

### 🔐 **Security**
- JWT-based authentication
- Secure password encryption
- Protected API endpoints
- User session management

## Tech Stack

### Backend
- **Java 17** with Spring Boot 3.2
- **Spring Security** for authentication
- **Spring Data JPA** for database operations
- **PostgreSQL** for production database
- **H2** for local development
- **JWT** for secure authentication
- **Maven** for dependency management

### Frontend
- **Vanilla JavaScript** (ES6+)
- **Tailwind CSS** for styling
- **Chart.js** for data visualization
- **Font Awesome** for icons
- **Responsive Design** principles

### Database Schema
```sql
Users → Expenses ← Categories
Users → Budgets ← Categories  
Users → Accounts → Expenses
```

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL (optional, H2 included for development)

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd FinanceTracker
```

2. **Database Setup (Optional)**

For PostgreSQL:
```sql
CREATE DATABASE finance_tracker;
CREATE USER finance_user WITH PASSWORD 'finance_password';
GRANT ALL PRIVILEGES ON DATABASE finance_tracker TO finance_user;
```

For H2 (Development), uncomment these lines in `application.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
```

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access the application**
- Open your browser to `http://localhost:8080`
- For H2 Console: `http://localhost:8080/h2-console`

## API Endpoints

### Authentication
```http
POST /api/auth/signin     # User login
POST /api/auth/signup     # User registration
```

### Expenses
```http
GET    /api/expenses                    # Get user expenses
POST   /api/expenses                    # Add new expense
PUT    /api/expenses/{id}              # Update expense
DELETE /api/expenses/{id}              # Delete expense
GET    /api/expenses/category/{id}     # Get expenses by category
GET    /api/expenses/daterange         # Get expenses by date range
```

### Budgets
```http
GET    /api/budgets                    # Get user budgets
POST   /api/budgets                    # Create new budget
PUT    /api/budgets/{id}              # Update budget
DELETE /api/budgets/{id}              # Delete budget
GET    /api/budgets/nearing-limit     # Get budgets near limit
GET    /api/budgets/exceeded          # Get exceeded budgets
```

### Categories
```http
GET /api/categories        # Get all categories
GET /api/categories/{id}   # Get category by ID
```

## Usage Guide

### Getting Started

1. **Create Account**
   - Click "Sign Up" on the homepage
   - Fill in your details and create an account
   - Sign in with your credentials

2. **Add Your First Expense**
   - Click the "Add Expense" button
   - Enter amount, description, and select category
   - Add optional notes
   - Submit to track the expense

3. **Set Up Budgets**
   - Navigate to the "Budgets" tab
   - Click "Add Budget"
   - Set budget limits for different categories
   - Configure notification thresholds

4. **Monitor Your Spending**
   - View the "Analytics" tab for visual insights
   - Track spending by category
   - Monitor monthly trends
   - Get alerts when approaching budget limits

### Default Categories

The application comes with pre-configured categories:
- 🍽️ Food & Dining
- 🚗 Transport
- 🛍️ Shopping
- 🎬 Entertainment
- 💡 Bills & Utilities
- ⚕️ Healthcare
- 📚 Education
- 🛒 Groceries
- ⛽ Gas & Fuel
- 📈 Investment
- 🎁 Gifts & Donations
- 💰 Other

## Configuration

### Database Configuration
Update `application.properties` for your database:
```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/finance_tracker
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=your-secret-key-here
jwt.expiration=86400000
```

### Security Settings
- JWT tokens expire in 24 hours by default
- Password encryption using BCrypt
- CORS enabled for frontend integration

## Development

### Project Structure
```
src/
├── main/
│   ├── java/com/financetracker/
│   │   ├── config/          # Security & app configuration
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data access layer
│   │   ├── security/       # JWT & authentication
│   │   └── service/        # Business logic
│   └── resources/
│       ├── static/         # Frontend assets
│       └── application.properties
```

### Adding New Features

1. **New Entity**: Create model class with JPA annotations
2. **Repository**: Extend JpaRepository for data access
3. **Service**: Implement business logic
4. **Controller**: Create REST endpoints
5. **DTO**: Define request/response objects
6. **Frontend**: Update JavaScript and HTML

### Testing
```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Deployment

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/finance-tracker-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Production Configuration
- Use PostgreSQL for production
- Configure proper JWT secrets
- Enable HTTPS
- Set up monitoring and logging

## Contributing

We welcome contributions! Here are ways to contribute:

### 🚀 **Feature Ideas**
- **Recurring Expenses**: Automatic expense tracking
- **Multiple Accounts**: Bank account integration
- **Export Data**: PDF/Excel reports
- **Mobile App**: React Native version
- **Notifications**: Email/SMS budget alerts
- **Advanced Analytics**: Predictive spending
- **Bill Reminders**: Automatic bill tracking
- **Savings Goals**: Financial goal tracking

### 🐛 **Bug Reports**
- Use GitHub Issues to report bugs
- Include steps to reproduce
- Provide system information

### 💡 **Enhancement Suggestions**
- UI/UX improvements
- Performance optimizations
- New chart types
- Additional categories

### 🔧 **Development**
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if needed
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- **Documentation**: Check this README and inline code comments
- **Issues**: Use GitHub Issues for bug reports
- **Discussions**: GitHub Discussions for questions
- **Email**: [Add your support email]

## Roadmap

### Version 2.0
- [ ] Mobile responsive improvements
- [ ] Advanced filtering options
- [ ] Data export functionality
- [ ] Multi-currency support

### Version 3.0
- [ ] Bank account integration
- [ ] Automatic transaction categorization
- [ ] Machine learning insights
- [ ] Team/family budgets

---

**Built with ❤️ for better financial management**