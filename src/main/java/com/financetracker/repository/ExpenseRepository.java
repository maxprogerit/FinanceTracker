package com.financetracker.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financetracker.model.Category;
import com.financetracker.model.Expense;
import com.financetracker.model.User;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserOrderByExpenseDateDesc(User user);
    List<Expense> findByUserAndCategoryOrderByExpenseDateDesc(User user, Category category);
    List<Expense> findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(User user, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT e FROM Expense e WHERE e.user = :user AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate")
    List<Expense> findExpensesByUserAndDateRange(@Param("user") User user, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.category = :category AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate")
    BigDecimal getTotalSpentByUserAndCategoryInDateRange(@Param("user") User user, 
                                                        @Param("category") Category category,
                                                        @Param("startDate") LocalDateTime startDate, 
                                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate GROUP BY e.category")
    List<Object[]> getSpendingByCategory(@Param("user") User user, 
                                       @Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate")
    BigDecimal getTotalSpentByUserInDateRange(@Param("user") User user, 
                                            @Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND DATE(e.expenseDate) = :date")
    List<Expense> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") java.time.LocalDate date);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND DATE(e.expenseDate) = :date")
    BigDecimal getDailyExpensesByUserId(@Param("userId") Long userId, @Param("date") java.time.LocalDate date);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND DATE(e.expenseDate) BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpensesByUserIdAndDateRange(@Param("userId") Long userId, 
                                                   @Param("startDate") java.time.LocalDate startDate, 
                                                   @Param("endDate") java.time.LocalDate endDate);
    
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND DATE(e.expenseDate) BETWEEN :startDate AND :endDate")
    List<Expense> findByUserIdAndDateBetween(@Param("userId") Long userId, 
                                           @Param("startDate") java.time.LocalDate startDate, 
                                           @Param("endDate") java.time.LocalDate endDate);
    
    List<Expense> findByCategory(Category category);
}