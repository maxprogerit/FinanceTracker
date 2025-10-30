package com.financetracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BudgetRequest {
    @NotBlank
    private String name;

    @NotNull
    @PositiveOrZero
    private BigDecimal budgetLimit;

    @NotNull
    private Long categoryId;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer notificationThreshold = 80;
    private String periodType = "MONTHLY";

    public BudgetRequest() {}

    public BudgetRequest(String name, BigDecimal budgetLimit, Long categoryId) {
        this.name = name;
        this.budgetLimit = budgetLimit;
        this.categoryId = categoryId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }

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
}