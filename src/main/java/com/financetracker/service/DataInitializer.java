package com.financetracker.service;

import com.financetracker.model.Category;
import com.financetracker.model.IncomeCategory;
import com.financetracker.model.User;
import com.financetracker.model.Role;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.IncomeCategoryRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private IncomeCategoryRepository incomeCategoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultCategories();
        initializeDefaultIncomeCategories();
        initializeAdminUser();
    }

    private void initializeDefaultCategories() {
        if (categoryRepository.count() == 0) {
            // Create default categories with icons and colors similar to Revolut
            createCategory("Food & Dining", "Restaurant and food expenses", "🍽️", "#FF6B6B");
            createCategory("Transport", "Transportation and travel", "🚗", "#4ECDC4");
            createCategory("Shopping", "Retail and online purchases", "🛍️", "#45B7D1");
            createCategory("Entertainment", "Movies, games, and fun", "🎬", "#96CEB4");
            createCategory("Bills & Utilities", "Monthly bills and utilities", "💡", "#FECA57");
            createCategory("Healthcare", "Medical and health expenses", "⚕️", "#FF9FF3");
            createCategory("Education", "Learning and education", "📚", "#54A0FF");
            createCategory("Groceries", "Food and household items", "🛒", "#5F27CD");
            createCategory("Gas & Fuel", "Vehicle fuel costs", "⛽", "#FF9F43");
            createCategory("Investment", "Stocks and investment", "📈", "#1DD1A1");
            createCategory("Gifts & Donations", "Charitable giving", "🎁", "#F0932B");
            createCategory("Other", "Miscellaneous expenses", "💰", "#6C5CE7");
        }
    }

    private void initializeDefaultIncomeCategories() {
        if (incomeCategoryRepository.count() == 0) {
            // Create default income categories
            createIncomeCategory("Salary", "Regular salary income", "💼", "#10AC84");
            createIncomeCategory("Freelance", "Freelance work income", "🖥️", "#00D2D3");
            createIncomeCategory("Investment", "Investment returns and dividends", "📊", "#5F27CD");
            createIncomeCategory("Business", "Business income and profits", "🏢", "#FF6348");
            createIncomeCategory("Rental", "Rental property income", "🏠", "#FFA502");
            createIncomeCategory("Side Hustle", "Side gig earnings", "🚀", "#3742FA");
            createIncomeCategory("Bonus", "Work bonuses and incentives", "🎯", "#2ED573");
            createIncomeCategory("Gift", "Gifts and monetary presents", "🎁", "#FF4757");
            createIncomeCategory("Refund", "Tax refunds and cashbacks", "💳", "#1E90FF");
            createIncomeCategory("Other", "Other sources of income", "💰", "#A4B0BE");
        }
    }

    private void createCategory(String name, String description, String iconName, String colorCode) {
        if (!categoryRepository.existsByName(name)) {
            Category category = new Category(name, description, iconName, colorCode);
            categoryRepository.save(category);
        }
    }
    
    private void createIncomeCategory(String name, String description, String iconName, String colorCode) {
        if (!incomeCategoryRepository.existsByName(name)) {
            IncomeCategory incomeCategory = new IncomeCategory(name, description, iconName, colorCode);
            incomeCategoryRepository.save(incomeCategory);
        }
    }
    
    private void initializeAdminUser() {
        // Create default admin user if no admin exists
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@financetracker.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setRole(Role.ADMIN);
            
            userRepository.save(admin);
            System.out.println("Default admin user created:");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
        }
    }
}