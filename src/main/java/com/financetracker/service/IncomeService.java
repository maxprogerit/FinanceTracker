package com.financetracker.service;

import com.financetracker.dto.IncomeDTO;
import com.financetracker.model.Income;
import com.financetracker.model.IncomeCategory;
import com.financetracker.model.User;
import com.financetracker.repository.IncomeRepository;
import com.financetracker.repository.IncomeCategoryRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IncomeService {
    
    @Autowired
    private IncomeRepository incomeRepository;
    
    @Autowired
    private IncomeCategoryRepository incomeCategoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<IncomeDTO> getAllIncomesByUserId(Long userId) {
        return incomeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<IncomeDTO> getIncomeById(Long id, Long userId) {
        return incomeRepository.findById(id)
                .filter(income -> income.getUser().getId().equals(userId))
                .map(this::convertToDTO);
    }
    
    public List<IncomeDTO> getIncomesByDate(Long userId, LocalDate date) {
        return incomeRepository.findByUserIdAndDate(userId, date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<IncomeDTO> getIncomesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public BigDecimal getTotalIncome(Long userId) {
        Double total = incomeRepository.getTotalIncomeByUserId(userId);
        return total != null ? BigDecimal.valueOf(total) : BigDecimal.ZERO;
    }
    
    public BigDecimal getDailyIncome(Long userId, LocalDate date) {
        Double total = incomeRepository.getDailyIncomeByUserIdAndDate(userId, date);
        return total != null ? BigDecimal.valueOf(total) : BigDecimal.ZERO;
    }
    
    public BigDecimal getIncomeForDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        Double total = incomeRepository.getTotalIncomeByUserIdAndDateRange(userId, startDate, endDate);
        return total != null ? BigDecimal.valueOf(total) : BigDecimal.ZERO;
    }
    
    public IncomeDTO createIncome(IncomeDTO incomeDTO, Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        
        Optional<IncomeCategory> categoryOpt = incomeCategoryRepository.findById(incomeDTO.getCategoryId());
        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Income category not found with id: " + incomeDTO.getCategoryId());
        }
        
        Income income = new Income();
        income.setAmount(incomeDTO.getAmount());
        income.setDescription(incomeDTO.getDescription());
        income.setIncomeDate(incomeDTO.getDate().atStartOfDay());
        income.setUser(userOpt.get());
        income.setCategory(categoryOpt.get());
        
        Income savedIncome = incomeRepository.save(income);
        return convertToDTO(savedIncome);
    }
    
    public IncomeDTO updateIncome(Long id, IncomeDTO incomeDTO, Long userId) {
        Optional<Income> incomeOpt = incomeRepository.findById(id);
        if (incomeOpt.isEmpty()) {
            throw new RuntimeException("Income not found with id: " + id);
        }
        
        Income income = incomeOpt.get();
        if (!income.getUser().getId().equals(userId)) {
            throw new RuntimeException("Income does not belong to the current user");
        }
        
        Optional<IncomeCategory> categoryOpt = incomeCategoryRepository.findById(incomeDTO.getCategoryId());
        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Income category not found with id: " + incomeDTO.getCategoryId());
        }
        
        income.setAmount(incomeDTO.getAmount());
        income.setDescription(incomeDTO.getDescription());
        income.setIncomeDate(incomeDTO.getDate().atStartOfDay());
        income.setCategory(categoryOpt.get());
        
        Income updatedIncome = incomeRepository.save(income);
        return convertToDTO(updatedIncome);
    }
    
    public void deleteIncome(Long id, Long userId) {
        Optional<Income> incomeOpt = incomeRepository.findById(id);
        if (incomeOpt.isEmpty()) {
            throw new RuntimeException("Income not found with id: " + id);
        }
        
        Income income = incomeOpt.get();
        if (!income.getUser().getId().equals(userId)) {
            throw new RuntimeException("Income does not belong to the current user");
        }
        
        incomeRepository.deleteById(id);
    }
    
    private IncomeDTO convertToDTO(Income income) {
        IncomeDTO dto = new IncomeDTO();
        dto.setId(income.getId());
        dto.setAmount(income.getAmount());
        dto.setDescription(income.getDescription());
        dto.setDate(income.getIncomeDate().toLocalDate());
        dto.setCategoryId(income.getCategory().getId());
        dto.setCategoryName(income.getCategory().getName());
        dto.setCategoryIcon(income.getCategory().getIconName());
        dto.setCategoryColor(income.getCategory().getColorCode());
        return dto;
    }
}