package com.financetracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "incomes")
public class Income {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Positive
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    
    @NotBlank
    private String description;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private IncomeCategory category;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
    
    @Column(name = "income_date")
    private LocalDateTime incomeDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    private String notes;
    
    @Column(name = "recurring_type")
    @Enumerated(EnumType.STRING)
    private RecurringType recurringType = RecurringType.NONE;
    
    public enum RecurringType {
        NONE, DAILY, WEEKLY, MONTHLY, YEARLY
    }
    
    // Constructors
    public Income() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.incomeDate = LocalDateTime.now();
    }
    
    public Income(BigDecimal amount, String description, IncomeCategory category, User user) {
        this();
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.user = user;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public IncomeCategory getCategory() { return category; }
    public void setCategory(IncomeCategory category) { this.category = category; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    
    public LocalDateTime getIncomeDate() { return incomeDate; }
    public void setIncomeDate(LocalDateTime incomeDate) { this.incomeDate = incomeDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public RecurringType getRecurringType() { return recurringType; }
    public void setRecurringType(RecurringType recurringType) { this.recurringType = recurringType; }
}