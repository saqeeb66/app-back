package com.arcotcabs.arcotcabs_backend.model.enums;

public enum TripStatus {

        PENDING,            // User booked, admin not assigned
        DRIVER_ASSIGNED,    // Admin assigned driver
        TRIP_STARTED,       // Driver started trip
        TRIP_ON_HOLD,       // Driver paused trip (important)
        TRIP_COMPLETED,     // Trip finished
        CANCELLED
}
