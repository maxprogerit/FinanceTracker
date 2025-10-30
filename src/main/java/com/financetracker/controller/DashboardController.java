package com.financetracker.controller;

import com.financetracker.dto.ExpenseResponse;
import com.financetracker.dto.IncomeDTO;
import com.financetracker.security.UserPrincipal;
import com.financetracker.service.ExpenseService;
import com.financetracker.service.IncomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {
    
    @Autowired
    private ExpenseService expenseService;
    
    @Autowired
    private IncomeService incomeService;
    
    @GetMapping("/daily-summary")
    public ResponseEntity<Map<String, Object>> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            // Get daily income and expenses
            BigDecimal dailyIncome = incomeService.getDailyIncome(userId, date);
            BigDecimal dailyExpenses = expenseService.getDailyExpensesByUserId(userId, date);
            
            // Get transactions for the day
            List<IncomeDTO> incomes = incomeService.getIncomesByDate(userId, date);
            List<ExpenseResponse> expenses = expenseService.getExpensesByDate(userId, date);
            
            // Calculate net balance for the day
            BigDecimal netBalance = dailyIncome.subtract(dailyExpenses);
            
            Map<String, Object> response = new HashMap<>();
            response.put("date", date);
            response.put("dailyIncome", dailyIncome);
            response.put("dailyExpenses", dailyExpenses);
            response.put("netBalance", netBalance);
            response.put("incomes", incomes);
            response.put("expenses", expenses);
            response.put("incomeCount", incomes.size());
            response.put("expenseCount", expenses.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/monthly-overview")
    public ResponseEntity<Map<String, Object>> getMonthlyOverview(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            // Calculate first and last day of month
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            
            // Get monthly totals
            BigDecimal monthlyIncome = incomeService.getIncomeForDateRange(userId, startDate, endDate);
            BigDecimal monthlyExpenses = expenseService.getTotalExpensesByUserIdAndDateRange(userId, startDate, endDate);
            
            // Get all transactions for the month
            List<IncomeDTO> incomes = incomeService.getIncomesByDateRange(userId, startDate, endDate);
            List<ExpenseResponse> expenses = expenseService.getExpensesByDateRange(userId, startDate, endDate);
            
            // Calculate net balance for the month
            BigDecimal netBalance = monthlyIncome.subtract(monthlyExpenses);
            
            Map<String, Object> response = new HashMap<>();
            response.put("year", year);
            response.put("month", month);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("monthlyIncome", monthlyIncome);
            response.put("monthlyExpenses", monthlyExpenses);
            response.put("netBalance", netBalance);
            response.put("incomes", incomes);
            response.put("expenses", expenses);
            response.put("totalTransactions", incomes.size() + expenses.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/calendar-data")
    public ResponseEntity<Map<String, Object>> getCalendarData(
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            // Calculate first and last day of month
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            
            Map<String, Object> calendarData = new HashMap<>();
            
            // Get data for each day of the month
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                BigDecimal dailyIncome = incomeService.getDailyIncome(userId, date);
                BigDecimal dailyExpenses = expenseService.getDailyExpensesByUserId(userId, date);
                BigDecimal netBalance = dailyIncome.subtract(dailyExpenses);
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("income", dailyIncome);
                dayData.put("expenses", dailyExpenses);
                dayData.put("net", netBalance);
                dayData.put("hasTransactions", dailyIncome.compareTo(BigDecimal.ZERO) > 0 || dailyExpenses.compareTo(BigDecimal.ZERO) > 0);
                
                calendarData.put(date.toString(), dayData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("year", year);
            response.put("month", month);
            response.put("calendarData", calendarData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}