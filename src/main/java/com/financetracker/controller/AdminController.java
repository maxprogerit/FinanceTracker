package com.financetracker.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.model.Expense;
import com.financetracker.model.Income;
import com.financetracker.model.User;
import com.financetracker.repository.BudgetRepository;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.ExpenseRepository;
import com.financetracker.repository.IncomeCategoryRepository;
import com.financetracker.repository.IncomeRepository;
import com.financetracker.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private IncomeCategoryRepository incomeCategoryRepository;

    // Get all users with statistics
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> userStats = new ArrayList<>();

            for (User user : users) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("id", user.getId());
                stats.put("username", user.getUsername());
                stats.put("email", user.getEmail());
                stats.put("firstName", user.getFirstName());
                stats.put("lastName", user.getLastName());
                stats.put("createdAt", user.getCreatedAt());
                
                // Calculate user statistics
                List<Expense> expenses = expenseRepository.findByUserOrderByExpenseDateDesc(user);
                List<Income> incomes = incomeRepository.findByUserOrderByIncomeDateDesc(user);
                List<Budget> budgets = budgetRepository.findByUserAndIsActive(user, true);
                
                double totalExpenses = expenses.stream()
                    .mapToDouble(e -> e.getAmount().doubleValue())
                    .sum();
                    
                double totalIncome = incomes.stream()
                    .mapToDouble(i -> i.getAmount().doubleValue())
                    .sum();
                
                stats.put("totalExpenses", totalExpenses);
                stats.put("totalIncome", totalIncome);
                stats.put("balance", totalIncome - totalExpenses);
                stats.put("expenseCount", expenses.size());
                stats.put("incomeCount", incomes.size());
                stats.put("budgetCount", budgets.size());
                
                userStats.add(stats);
            }

            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get user details by ID
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDetails(@PathVariable Long userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            Map<String, Object> details = new HashMap<>();
            
            // Basic user info
            details.put("user", user);
            
            // Financial data
            List<Expense> expenses = expenseRepository.findByUserOrderByExpenseDateDesc(user);
            List<Income> incomes = incomeRepository.findByUserOrderByIncomeDateDesc(user);
            List<Budget> budgets = budgetRepository.findByUserAndIsActive(user, true);
            
            details.put("expenses", expenses);
            details.put("incomes", incomes);
            details.put("budgets", budgets);
            
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get system statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Count totals
            stats.put("totalUsers", userRepository.count());
            stats.put("totalExpenses", expenseRepository.count());
            stats.put("totalIncomes", incomeRepository.count());
            stats.put("totalBudgets", budgetRepository.count());
            stats.put("totalCategories", categoryRepository.count());
            stats.put("totalIncomeCategories", incomeCategoryRepository.count());
            
            // Calculate financial totals
            List<Expense> allExpenses = expenseRepository.findAll();
            List<Income> allIncomes = incomeRepository.findAll();
            
            double systemTotalExpenses = allExpenses.stream()
                .mapToDouble(e -> e.getAmount().doubleValue())
                .sum();
                
            double systemTotalIncome = allIncomes.stream()
                .mapToDouble(i -> i.getAmount().doubleValue())
                .sum();
                
            stats.put("systemTotalExpenses", systemTotalExpenses);
            stats.put("systemTotalIncome", systemTotalIncome);
            stats.put("systemBalance", systemTotalIncome - systemTotalExpenses);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get all categories usage
    @GetMapping("/categories/usage")
    public ResponseEntity<List<Map<String, Object>>> getCategoriesUsage() {
        try {
            List<Category> categories = categoryRepository.findAll();
            List<Map<String, Object>> usage = new ArrayList<>();
            
            for (Category category : categories) {
                Map<String, Object> categoryUsage = new HashMap<>();
                categoryUsage.put("id", category.getId());
                categoryUsage.put("name", category.getName());
                categoryUsage.put("description", category.getDescription());
                categoryUsage.put("iconName", category.getIconName());
                categoryUsage.put("colorCode", category.getColorCode());
                
                List<Expense> expenses = expenseRepository.findByCategory(category);
                List<Budget> budgets = budgetRepository.findByCategory(category);
                
                double totalSpent = expenses.stream()
                    .mapToDouble(e -> e.getAmount().doubleValue())
                    .sum();
                    
                categoryUsage.put("expenseCount", expenses.size());
                categoryUsage.put("totalSpent", totalSpent);
                categoryUsage.put("budgetCount", budgets.size());
                
                usage.add(categoryUsage);
            }
            
            return ResponseEntity.ok(usage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete user (admin only)
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        try {
            if (userId == null || !userRepository.existsById(userId)) {
                return ResponseEntity.notFound().build();
            }
            
            userRepository.deleteById(userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
