package com.financetracker.controller;

import com.financetracker.dto.IncomeCategoryDTO;
import com.financetracker.service.IncomeCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/income-categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class IncomeCategoryController {
    
    @Autowired
    private IncomeCategoryService incomeCategoryService;
    
    @GetMapping
    public ResponseEntity<List<IncomeCategoryDTO>> getAllCategories() {
        try {
            List<IncomeCategoryDTO> categories = incomeCategoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<IncomeCategoryDTO> getCategoryById(@PathVariable Long id) {
        try {
            Optional<IncomeCategoryDTO> category = incomeCategoryService.getCategoryById(id);
            if (category.isPresent()) {
                return ResponseEntity.ok(category.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<IncomeCategoryDTO> createCategory(@Valid @RequestBody IncomeCategoryDTO categoryDTO) {
        try {
            IncomeCategoryDTO createdCategory = incomeCategoryService.createCategory(categoryDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<IncomeCategoryDTO> updateCategory(@PathVariable Long id, 
                                                           @Valid @RequestBody IncomeCategoryDTO categoryDTO) {
        try {
            IncomeCategoryDTO updatedCategory = incomeCategoryService.updateCategory(id, categoryDTO);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            incomeCategoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}