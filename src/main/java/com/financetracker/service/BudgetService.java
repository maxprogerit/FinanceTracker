package com.financetracker.service;

import com.financetracker.dto.BudgetRequest;
import com.financetracker.dto.BudgetResponse;
import com.financetracker.model.*;
import com.financetracker.repository.*;
import com.financetracker.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    private User getCurrentUser() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        return userRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public BudgetResponse createBudget(BudgetRequest request) {
        User user = getCurrentUser();
        
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));

        // Check if active budget already exists for this category
        if (budgetRepository.findByUserAndCategoryAndIsActiveTrue(user, category).isPresent()) {
            throw new RuntimeException("Active budget already exists for this category");
        }

        Budget budget = new Budget();
        budget.setName(request.getName());
        budget.setBudgetLimit(request.getBudgetLimit());
        budget.setCategory(category);
        budget.setUser(user);
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setNotificationThreshold(request.getNotificationThreshold());
        budget.setPeriodType(Budget.PeriodType.valueOf(request.getPeriodType()));

        // Calculate current spending for this category
        LocalDateTime startDate = request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime endDate = request.getEndDate() != null ? request.getEndDate() : LocalDateTime.now().withDayOfMonth(1).plusMonths(1).minusDays(1);
        
        BigDecimal currentSpending = expenseRepository.getTotalSpentByUserAndCategoryInDateRange(
            user, category, startDate, endDate);
        budget.setSpentAmount(currentSpending != null ? currentSpending : BigDecimal.ZERO);

        Budget savedBudget = budgetRepository.save(budget);
        return convertToResponse(savedBudget);
    }

    public List<BudgetResponse> getUserBudgets() {
        User user = getCurrentUser();
        List<Budget> budgets = budgetRepository.findByUserAndIsActiveTrue(user);
        return budgets.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public BudgetResponse updateBudget(Long budgetId, BudgetRequest request) {
        User user = getCurrentUser();
        
        Budget budget = budgetRepository.findById(budgetId)
            .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Budget does not belong to user");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));

        budget.setName(request.getName());
        budget.setBudgetLimit(request.getBudgetLimit());
        budget.setCategory(category);
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setNotificationThreshold(request.getNotificationThreshold());
        budget.setPeriodType(Budget.PeriodType.valueOf(request.getPeriodType()));

        Budget savedBudget = budgetRepository.save(budget);
        return convertToResponse(savedBudget);
    }

    public void deleteBudget(Long budgetId) {
        User user = getCurrentUser();
        
        Budget budget = budgetRepository.findById(budgetId)
            .orElseThrow(() -> new RuntimeException("Budget not found"));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Budget does not belong to user");
        }

        budgetRepository.delete(budget);
    }

    public void updateBudgetSpending(Category category, User user, BigDecimal amount) {
        budgetRepository.findByUserAndCategoryAndIsActiveTrue(user, category)
            .ifPresent(budget -> {
                budget.setSpentAmount(budget.getSpentAmount().add(amount));
                budgetRepository.save(budget);
            });
    }

    public List<BudgetResponse> getBudgetsNearingLimit() {
        User user = getCurrentUser();
        List<Budget> budgets = budgetRepository.findBudgetsNearingLimit(user);
        return budgets.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public List<BudgetResponse> getExceededBudgets() {
        User user = getCurrentUser();
        List<Budget> budgets = budgetRepository.findExceededBudgets(user);
        return budgets.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    private BudgetResponse convertToResponse(Budget budget) {
        BudgetResponse response = new BudgetResponse();
        response.setId(budget.getId());
        response.setName(budget.getName());
        response.setBudgetLimit(budget.getBudgetLimit());
        response.setSpentAmount(budget.getSpentAmount());
        response.setRemainingAmount(budget.getRemainingAmount());
        response.setPercentageSpent(budget.getPercentageSpent());
        response.setCategoryName(budget.getCategory().getName());
        response.setCategoryId(budget.getCategory().getId());
        response.setStartDate(budget.getStartDate());
        response.setEndDate(budget.getEndDate());
        response.setNotificationThreshold(budget.getNotificationThreshold());
        response.setPeriodType(budget.getPeriodType().name());
        response.setIsActive(budget.getIsActive());
        response.setIsExceeded(budget.isExceeded());
        response.setIsThresholdReached(budget.isThresholdReached());
        response.setCreatedAt(budget.getCreatedAt());
        return response;
    }
}