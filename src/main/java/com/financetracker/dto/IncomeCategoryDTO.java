package com.financetracker.dto;

import java.math.BigDecimal;

public class IncomeCategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String iconName;
    private String colorCode;
    private BigDecimal totalAmount;
    private Integer count;
    
    // Constructors
    public IncomeCategoryDTO() {}
    
    public IncomeCategoryDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public IncomeCategoryDTO(String name, String description, String iconName, String colorCode) {
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
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}