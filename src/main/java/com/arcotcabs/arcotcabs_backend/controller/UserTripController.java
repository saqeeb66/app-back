package com.arcotcabs.arcotcabs_backend.controller;

import com.arcotcabs.arcotcabs_backend.model.Trip;
import com.arcotcabs.arcotcabs_backend.model.enums.TripStatus;
import com.arcotcabs.arcotcabs_backend.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/user/trips")
@CrossOrigin
public class UserTripController {

    private final TripService service;

    public UserTripController(TripService service) {
        this.service = service;
    }

    /* ================= USER – BOOK TRIP ================= */

    @PostMapping
    public String bookTrip(
            @RequestBody Trip trip,
            Authentication auth
    ) {
        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        // SECURITY: user cannot fake identity
        trip.setUserId(auth.getName());
        trip.setStatus(TripStatus.PENDING);
        trip.setCreatedAt(System.currentTimeMillis());

        return service.bookTrip(trip);
    }

    /* ================= USER – VIEW MY TRIPS ================= */

    @GetMapping
    public List<Trip> myTrips(Authentication auth) {
        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        return service.getUserTrips(auth.getName());
    }

    /* ================= USER – VIEW SINGLE TRIP ================= */
    @GetMapping("/active")
    public Trip getActiveTrip(Authentication auth) {
        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        return service.getUserTrips(auth.getName())
                .stream()
                .filter(t ->
                        t.getStatus() == TripStatus.PENDING ||
                                t.getStatus() == TripStatus.DRIVER_ASSIGNED ||
                                t.getStatus() == TripStatus.TRIP_STARTED ||
                                t.getStatus() == TripStatus.TRIP_ON_HOLD
                )
                .findFirst()
                .orElse(null);
    }


    @GetMapping("/{tripId}")
    public Trip getTripById(
            @PathVariable String tripId,
            Authentication auth
    ) {
        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        Trip trip = service.getTripById(tripId);

        // SECURITY: user can only see own trip
        if (!auth.getName().equals(trip.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied"
            );
        }

        return trip;
    }
}
