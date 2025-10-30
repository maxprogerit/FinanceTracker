package com.financetracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "income_categories")
public class IncomeCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(unique = true)
    private String name;
    
    private String description;
    
    @Column(name = "icon_name")
    private String iconName;
    
    @Column(name = "color_code")
    private String colorCode;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Income> incomes = new HashSet<>();
    
    // Constructors
    public IncomeCategory() {
        this.createdAt = LocalDateTime.now();
    }
    
    public IncomeCategory(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    public IncomeCategory(String name, String description, String iconName, String colorCode) {
        this(name, description);
        this.iconName = iconName;
        this.colorCode = colorCode;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Set<Income> getIncomes() { return incomes; }
    public void setIncomes(Set<Income> incomes) { this.incomes = incomes; }
}