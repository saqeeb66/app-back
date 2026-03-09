package com.arcotcabs.arcotcabs_backend.controller;

import com.arcotcabs.arcotcabs_backend.model.Expense;
import com.arcotcabs.arcotcabs_backend.service.ExpenseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/expenses")
@CrossOrigin
public class AdminExpenseController {

    private final ExpenseService expenseService;

    public AdminExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/{tripId}")
    public List<Expense> getExpensesByTrip(@PathVariable String tripId) {
        return expenseService.tripExpenses(tripId);
    }
}
