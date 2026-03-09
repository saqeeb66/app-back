package com.arcotcabs.arcotcabs_backend.service;

import com.arcotcabs.arcotcabs_backend.model.Driver;
import com.arcotcabs.arcotcabs_backend.model.Expense;
import com.arcotcabs.arcotcabs_backend.model.Trip;
import com.arcotcabs.arcotcabs_backend.model.enums.TripStatus;
import com.arcotcabs.arcotcabs_backend.repository.DriverRepository;
import com.arcotcabs.arcotcabs_backend.repository.ExpenseRepository;
import com.arcotcabs.arcotcabs_backend.repository.TripRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final TripRepository tripRepo;
    private final DriverRepository driverRepo;

    public ExpenseService(
            ExpenseRepository expenseRepo,
            TripRepository tripRepo,
            DriverRepository driverRepo
    ) {
        this.expenseRepo = expenseRepo;
        this.tripRepo = tripRepo;
        this.driverRepo = driverRepo;
    }

    public void addExpense(Expense expense, String driverUserId) {

        // 🔹 Fetch trip
        Trip trip = tripRepo.findById(expense.getTripId());
        if (trip == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Trip not found"
            );
        }

        // 🔹 Trip must be active
        if (trip.getStatus() != TripStatus.TRIP_STARTED &&
                trip.getStatus() != TripStatus.TRIP_ON_HOLD) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot add expense to inactive trip"
            );
        }

        // 🔹 Resolve driver using logged-in userId (email)
        Driver driver = driverRepo.findByUserId(driverUserId);
        if (driver == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Driver profile not found"
            );
        }

        // 🔹 Ensure this driver is assigned to the trip
        if (!driver.getDriverId().equals(trip.getDriverId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only assigned driver can add expenses"
            );
        }

        // 🔹 Save expense
        expense.setExpenseId(UUID.randomUUID().toString());
        expense.setAddedBy(driverUserId);
        expense.setCreatedAt(System.currentTimeMillis());

        expenseRepo.addExpense(expense);
    }

    public List<Expense> tripExpenses(String tripId) {
        return expenseRepo.getByTrip(tripId);
    }
}
