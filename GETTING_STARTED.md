# Getting Started with Personal Finance Tracker

Welcome to your new Personal Finance Tracker! This guide will help you get up and running quickly.

## Quick Start (Windows)

1. **Double-click `start.bat`** - This will automatically build and run the application
2. **Open your browser** to `http://localhost:8081`
3. **Create an account** and start tracking your expenses!

## Manual Setup

If the batch file doesn't work, follow these steps:

### Prerequisites Check
```bash
# Check Java version (needs Java 17+)
java -version

# Check Maven (needs Maven 3.6+)  
mvn -version
```

### Run the Application
```bash
# Navigate to project directory
cd FinanceTracker

# Clean and run
mvn clean spring-boot:run
```

### Access Points
- **Main Application**: http://localhost:8081
- **H2 Database Console**: http://localhost:8081/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (leave empty)

## First Steps

### 1. Create Your Account
- Go to http://localhost:8081
- Click "Sign Up" 
- Fill in your details
- Sign in with your new credentials

### 2. Add Your First Expense
- Click "Add Expense" button
- Enter amount: `25.50`
- Description: `Coffee and breakfast`
- Category: `Food & Dining`
- Click "Add Expense"

### 3. Set Up a Budget
- Go to "Budgets" tab
- Click "Add Budget"
- Budget Name: `Monthly Food Budget`
- Budget Limit: `300`
- Category: `Food & Dining`
- Click "Create Budget"

### 4. View Analytics
- Go to "Analytics" tab
- See your spending breakdown by category
- Monitor monthly trends

## Default Categories Available

The app comes with 12 pre-configured categories:
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

## Features Overview

### ✅ **What You Can Do**
- Track daily expenses with categories
- Set monthly budgets per category
- Get notifications when approaching limits
- View spending analytics and trends
- Secure login with JWT authentication
- Responsive design for mobile and desktop

### 🔄 **Budget Notifications**
- **Green**: Under 80% of budget
- **Yellow**: 80-100% of budget (warning)
- **Red**: Over budget (exceeded)

### 📊 **Analytics Charts**
- **Doughnut Chart**: Spending breakdown by category
- **Line Chart**: Monthly spending trends
- Real-time updates as you add expenses

## Sample Data to Try

Try adding these sample expenses to see the app in action:

```
Amount: $12.50, Description: "Lunch at cafe", Category: Food & Dining
Amount: $45.00, Description: "Gas fill-up", Category: Gas & Fuel  
Amount: $89.99, Description: "New shoes", Category: Shopping
Amount: $15.00, Description: "Movie ticket", Category: Entertainment
Amount: $120.00, Description: "Electricity bill", Category: Bills & Utilities
```

## Troubleshooting

### Port Already in Use
If port 8081 is busy:
```bash
# Find what's using port 8081
netstat -ano | findstr :8081

# Kill the process (replace PID)
taskkill /PID <process_id> /F

# Or change port in application.properties
server.port=8082
```

### Database Issues
The app uses H2 in-memory database by default, so data resets on restart. For persistent data:

1. Install PostgreSQL
2. Create database: `finance_tracker`  
3. Update `application.properties` with PostgreSQL settings
4. Comment out H2 config, uncomment PostgreSQL config

### Build Failures
```bash
# Clean build
mvn clean install

# Skip tests if needed
mvn clean install -DskipTests

# Update dependencies
mvn clean compile
```

## Next Steps

### Customization Ideas
- Add more expense categories
- Set up multiple budgets per category
- Export your data (feature request!)
- Set up recurring expenses tracking

### Production Deployment  
- Switch to PostgreSQL database
- Configure proper JWT secrets
- Set up HTTPS/SSL
- Use environment variables for config

## API Testing

You can test the API directly using curl or Postman:

```bash
# Register user
curl -X POST http://localhost:8081/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

# Login  
curl -X POST http://localhost:8081/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"testuser","password":"password123"}'

# Get expenses (use token from login)
curl -X GET http://localhost:8081/api/expenses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Need Help?

- **Check the README.md** for detailed documentation
- **Look at browser console** for JavaScript errors  
- **Check application logs** in the terminal
- **Open GitHub Issues** for bugs or questions

## Success Checklist

- [ ] Application starts without errors
- [ ] Can access http://localhost:8081
- [ ] Can create a user account
- [ ] Can add expenses successfully
- [ ] Can create budgets
- [ ] Analytics charts display correctly
- [ ] H2 console accessible (optional)

**Happy expense tracking! 💰📊**