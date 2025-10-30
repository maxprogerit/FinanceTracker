package com.financetracker.service;

import com.financetracker.model.Category;
import com.financetracker.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultCategories();
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

    private void createCategory(String name, String description, String iconName, String colorCode) {
        if (!categoryRepository.existsByName(name)) {
            Category category = new Category(name, description, iconName, colorCode);
            categoryRepository.save(category);
        }
    }
}