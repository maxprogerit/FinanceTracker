package com.financetracker.repository;

import com.financetracker.model.IncomeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeCategoryRepository extends JpaRepository<IncomeCategory, Long> {
    
    Optional<IncomeCategory> findByName(String name);
    
    List<IncomeCategory> findAllByOrderByName();
    
    boolean existsByName(String name);
}