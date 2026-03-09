package com.arcotcabs.arcotcabs_backend.controller;

import com.arcotcabs.arcotcabs_backend.model.Trip;
import com.arcotcabs.arcotcabs_backend.model.enums.TripStatus;
import com.arcotcabs.arcotcabs_backend.service.DriverService;
import com.arcotcabs.arcotcabs_backend.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/trips")
@CrossOrigin
public class AdminTripController {

    private final TripService tripService;
    private final DriverService driverService;

    public AdminTripController(
            TripService tripService,
            DriverService driverService
    ) {
        this.tripService = tripService;
        this.driverService = driverService;
    }

    // 🔹 Admin books a new trip
    @PostMapping
    public String createTrip(
            @RequestBody Trip trip,
            Authentication auth
    ) {
        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        // Set default values
        trip.setStatus(TripStatus.PENDING);
        trip.setCreatedAt(System.currentTimeMillis());

        return tripService.bookTrip(trip);
    }

    // 🔹 Admin sees all trips
    @GetMapping
    public List<Trip> allTrips() {
        return tripService.getAllTrips();
    }

    // 🔹 Admin assigns driver to trip
    @PostMapping("/{tripId}/assign/{driverId}")
    public void assignDriver(
            @PathVariable String tripId,
            @PathVariable String driverId
    ) {
        driverService.assignDriverToTrip(tripId, driverId);
    }

    // 🔹 Admin views trip details
    @GetMapping("/{tripId}")
    public Trip getTripById(@PathVariable String tripId) {
        return tripService.getTripById(tripId);
    }
}
