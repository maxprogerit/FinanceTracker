package com.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BudgetResponse {
    private Long id;
    private String name;
    private BigDecimal budgetLimit;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double percentageSpent;
    private String categoryName;
    private Long categoryId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer notificationThreshold;
    private String periodType;
    private Boolean isActive;
    private Boolean isExceeded;
    private Boolean isThresholdReached;
    private LocalDateTime createdAt;

    public BudgetResponse() {}

    public BudgetResponse(Long id, String name, BigDecimal budgetLimit, BigDecimal spentAmount, 
                         String categoryName, Long categoryId) {
        this.id = id;
        this.name = name;
        this.budgetLimit = budgetLimit;
        this.spentAmount = spentAmount;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.remainingAmount = budgetLimit.subtract(spentAmount);
        this.percentageSpent = calculatePercentageSpent();
    }

    private Double calculatePercentageSpent() {
        if (budgetLimit.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return spentAmount.divide(budgetLimit, 4, java.math.RoundingMode.HALF_UP)
                         .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

    public Double getPercentageSpent() { return percentageSpent; }
    public void setPercentageSpent(Double percentageSpent) { this.percentageSpent = percentageSpent; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Integer getNotificationThreshold() { return notificationThreshold; }
    public void setNotificationThreshold(Integer notificationThreshold) { this.notificationThreshold = notificationThreshold; }

    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsExceeded() { return isExceeded; }
    public void setIsExceeded(Boolean isExceeded) { this.isExceeded = isExceeded; }

    public Boolean getIsThresholdReached() { return isThresholdReached; }
    public void setIsThresholdReached(Boolean isThresholdReached) { this.isThresholdReached = isThresholdReached; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}