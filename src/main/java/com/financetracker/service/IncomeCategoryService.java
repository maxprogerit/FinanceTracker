package com.financetracker.service;

import com.financetracker.dto.IncomeCategoryDTO;
import com.financetracker.model.IncomeCategory;
import com.financetracker.repository.IncomeCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IncomeCategoryService {
    
    @Autowired
    private IncomeCategoryRepository incomeCategoryRepository;
    
    public List<IncomeCategoryDTO> getAllCategories() {
        return incomeCategoryRepository.findAllByOrderByName().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<IncomeCategoryDTO> getCategoryById(Long id) {
        return incomeCategoryRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public IncomeCategoryDTO createCategory(IncomeCategoryDTO categoryDTO) {
        if (incomeCategoryRepository.existsByName(categoryDTO.getName())) {
            throw new RuntimeException("Category with name '" + categoryDTO.getName() + "' already exists");
        }
        
        IncomeCategory category = new IncomeCategory();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setIconName(categoryDTO.getIconName());
        category.setColorCode(categoryDTO.getColorCode());
        
        IncomeCategory savedCategory = incomeCategoryRepository.save(category);
        return convertToDTO(savedCategory);
    }
    
    public IncomeCategoryDTO updateCategory(Long id, IncomeCategoryDTO categoryDTO) {
        Optional<IncomeCategory> categoryOpt = incomeCategoryRepository.findById(id);
        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        
        IncomeCategory category = categoryOpt.get();
        
        // Check if name is being changed and if new name already exists
        if (!category.getName().equals(categoryDTO.getName()) && 
            incomeCategoryRepository.existsByName(categoryDTO.getName())) {
            throw new RuntimeException("Category with name '" + categoryDTO.getName() + "' already exists");
        }
        
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setIconName(categoryDTO.getIconName());
        category.setColorCode(categoryDTO.getColorCode());
        
        IncomeCategory updatedCategory = incomeCategoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }
    
    public void deleteCategory(Long id) {
        if (!incomeCategoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        incomeCategoryRepository.deleteById(id);
    }
    
    private IncomeCategoryDTO convertToDTO(IncomeCategory category) {
        IncomeCategoryDTO dto = new IncomeCategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIconName(category.getIconName());
        dto.setColorCode(category.getColorCode());
        return dto;
    }
}