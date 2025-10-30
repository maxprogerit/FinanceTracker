package com.financetracker.service;

import com.financetracker.dto.ExpenseRequest;
import com.financetracker.dto.ExpenseResponse;
import com.financetracker.model.*;
import com.financetracker.repository.*;
import com.financetracker.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetService budgetService;

    private User getCurrentUser() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        return userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ExpenseResponse addExpense(ExpenseRequest request) {
        User user = getCurrentUser();
        
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));

        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
            
            // Verify account belongs to user
            if (!account.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Account does not belong to user");
            }
        }

        Expense expense = new Expense();
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setCategory(category);
        expense.setUser(user);
        expense.setAccount(account);
        expense.setExpenseDate(request.getExpenseDate() != null ? 
            request.getExpenseDate() : LocalDateTime.now());
        expense.setNotes(request.getNotes());
        expense.setRecurringType(Expense.RecurringType.valueOf(request.getRecurringType()));

        Expense savedExpense = expenseRepository.save(expense);

        // Update budget spending if applicable
        budgetService.updateBudgetSpending(category, user, request.getAmount());

        // Update account balance if account is specified
        if (account != null) {
            account.subtractFromBalance(request.getAmount());
            accountRepository.save(account);
        }

        return convertToResponse(savedExpense);
    }

    public List<ExpenseResponse> getUserExpenses() {
        User user = getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserOrderByExpenseDateDesc(user);
        return expenses.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getUserExpensesByCategory(Long categoryId) {
        User user = getCurrentUser();
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        
        List<Expense> expenses = expenseRepository.findByUserAndCategoryOrderByExpenseDateDesc(user, category);
        return expenses.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public List<ExpenseResponse> getUserExpensesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        User user = getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
            user, startDate, endDate);
        return expenses.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public ExpenseResponse updateExpense(Long expenseId, ExpenseRequest request) {
        User user = getCurrentUser();
        
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new RuntimeException("Expense not found"));

        // Verify expense belongs to user
        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Expense does not belong to user");
        }

        // Store original amount and category for budget updates
        BigDecimal originalAmount = expense.getAmount();
        Category originalCategory = expense.getCategory();

        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));

        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
            
            if (!account.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Account does not belong to user");
            }
        }

        // Update account balances
        if (expense.getAccount() != null) {
            expense.getAccount().addToBalance(originalAmount); // Reverse original transaction
        }
        if (account != null) {
            account.subtractFromBalance(request.getAmount()); // Apply new transaction
        }

        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setCategory(category);
        expense.setAccount(account);
        expense.setExpenseDate(request.getExpenseDate());
        expense.setNotes(request.getNotes());
        expense.setRecurringType(Expense.RecurringType.valueOf(request.getRecurringType()));

        Expense savedExpense = expenseRepository.save(expense);

        // Update budget spending
        if (originalCategory.getId().equals(category.getId())) {
            // Same category, update with difference
            BigDecimal difference = request.getAmount().subtract(originalAmount);
            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                budgetService.updateBudgetSpending(category, user, difference);
            }
        } else {
            // Different categories, reverse original and add new
            budgetService.updateBudgetSpending(originalCategory, user, originalAmount.negate());
            budgetService.updateBudgetSpending(category, user, request.getAmount());
        }

        return convertToResponse(savedExpense);
    }

    public void deleteExpense(Long expenseId) {
        User user = getCurrentUser();
        
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Expense does not belong to user");
        }

        // Reverse budget spending
        budgetService.updateBudgetSpending(expense.getCategory(), user, expense.getAmount().negate());

        // Reverse account transaction
        if (expense.getAccount() != null) {
            expense.getAccount().addToBalance(expense.getAmount());
            accountRepository.save(expense.getAccount());
        }

        expenseRepository.delete(expense);
    }

    private ExpenseResponse convertToResponse(Expense expense) {
        ExpenseResponse response = new ExpenseResponse();
        response.setId(expense.getId());
        response.setAmount(expense.getAmount());
        response.setDescription(expense.getDescription());
        response.setCategoryName(expense.getCategory().getName());
        response.setCategoryId(expense.getCategory().getId());
        response.setExpenseDate(expense.getExpenseDate());
        response.setNotes(expense.getNotes());
        response.setRecurringType(expense.getRecurringType().name());
        response.setCreatedAt(expense.getCreatedAt());
        
        if (expense.getAccount() != null) {
            response.setAccountName(expense.getAccount().getName());
            response.setAccountId(expense.getAccount().getId());
        }
        
        return response;
    }
}