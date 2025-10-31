package com.financetracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financetracker.model.Income;
import com.financetracker.model.User;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    
    List<Income> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Income> findByUserOrderByIncomeDateDesc(User user);
    
    @Query("SELECT i FROM Income i WHERE i.user.id = :userId AND DATE(i.incomeDate) BETWEEN :startDate AND :endDate")
    List<Income> findByUserIdAndDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT i FROM Income i WHERE i.user.id = :userId AND DATE(i.incomeDate) = :date")
    List<Income> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user.id = :userId")
    Double getTotalIncomeByUserId(@Param("userId") Long userId);
    
    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user.id = :userId AND DATE(i.incomeDate) BETWEEN :startDate AND :endDate")
    Double getTotalIncomeByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user.id = :userId AND DATE(i.incomeDate) = :date")
    Double getDailyIncomeByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT i FROM Income i WHERE i.user.id = :userId AND i.category.id = :categoryId ORDER BY i.createdAt DESC")
    List<Income> findByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);
}