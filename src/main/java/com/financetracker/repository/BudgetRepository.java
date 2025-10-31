package com.financetracker.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.model.User;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserAndIsActiveTrue(User user);
    List<Budget> findByUser(User user);
    Optional<Budget> findByUserAndCategoryAndIsActiveTrue(User user, Category category);
    
    List<Budget> findByUserAndIsActive(User user, boolean isActive);
    List<Budget> findByCategory(Category category);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.isActive = true AND " +
           "(b.startDate IS NULL OR b.startDate <= :currentDate) AND " +
           "(b.endDate IS NULL OR b.endDate >= :currentDate)")
    List<Budget> findActiveBudgetsByUserAndDate(@Param("user") User user, 
                                               @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.isActive = true AND " +
           "b.spentAmount >= (b.budgetLimit * b.notificationThreshold / 100)")
    List<Budget> findBudgetsNearingLimit(@Param("user") User user);
    
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.isActive = true AND " +
           "b.spentAmount > b.budgetLimit")
    List<Budget> findExceededBudgets(@Param("user") User user);
}