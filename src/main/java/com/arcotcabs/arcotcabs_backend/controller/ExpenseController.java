package com.arcotcabs.arcotcabs_backend.controller;

import com.arcotcabs.arcotcabs_backend.model.Expense;
import com.arcotcabs.arcotcabs_backend.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/driver/expenses")
@CrossOrigin
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    /* ================= DRIVER – ADD EXPENSE ================= */

    @PostMapping
    public void addExpense(
            @RequestBody Expense expense,
            Authentication auth
    ) {
        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        service.addExpense(expense, auth.getName());
    }

    /* ================= VIEW EXPENSES BY TRIP ================= */

    @GetMapping("/{tripId}")
    public List<Expense> byTrip(
            @PathVariable String tripId,
            Authentication auth
    ) {
        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        return service.tripExpenses(tripId);
    }
}
