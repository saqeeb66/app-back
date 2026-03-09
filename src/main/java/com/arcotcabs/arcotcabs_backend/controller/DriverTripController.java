package com.arcotcabs.arcotcabs_backend.controller;

import com.arcotcabs.arcotcabs_backend.dto.EndTripRequest;
import com.arcotcabs.arcotcabs_backend.dto.StartTripRequest;
import com.arcotcabs.arcotcabs_backend.model.Trip;
import com.arcotcabs.arcotcabs_backend.service.DriverService;
import com.arcotcabs.arcotcabs_backend.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/driver/trips")
@CrossOrigin
public class DriverTripController {

    private final TripService tripService;
    private final DriverService driverService;

    public DriverTripController(
            TripService tripService,
            DriverService driverService
    ) {
        this.tripService = tripService;
        this.driverService = driverService;
    }

    /* ================= GET ACTIVE TRIPS ================= */

    @GetMapping
    public List<Trip> getDriverTrips(Authentication auth) {

        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        return driverService.getActiveTripsForDriver(auth.getName());
    }

    /* ================= GET COMPLETED TRIPS ================= */

    @GetMapping("/completed")
    public List<Trip> getCompletedTrips(Authentication auth) {

        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        return driverService.getCompletedTripsForDriver(auth.getName());
    }

    /* ================= START TRIP ================= */

    @PostMapping("/{tripId}/start")
    public void startTrip(
            @PathVariable String tripId,
            @RequestBody StartTripRequest request,
            Authentication auth
    ) {

        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        if (request.startLocation == null ||
                request.startKm == null ||
                request.startOdometerImageUrl == null ||
                request.startOdometerImageUrl.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start location, KM and odometer image are required"
            );
        }

        String driverId = driverService.getDriverIdByEmail(auth.getName());

        tripService.startTrip(
                tripId,
                driverId,
                request.startLocation,
                request.startKm,
                request.startOdometerImageUrl
        );
    }

    /* ================= END TRIP ================= */

    @PostMapping("/{tripId}/end")
    public void endTrip(
            @PathVariable String tripId,
            @RequestBody EndTripRequest request,
            Authentication auth
    ) {

        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        if (request.endLocation == null ||
                request.endKm == null ||
                request.endOdometerImageUrl == null ||
                request.signatureUrl == null ||
                request.endOdometerImageUrl.isBlank() ||
                request.signatureUrl.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End location, KM, odometer image and signature are required"
            );
        }

        tripService.endTrip(
                tripId,
                request.endLocation,
                request.endKm,
                request.endOdometerImageUrl,
                request.signatureUrl
        );

        driverService.markDriverAvailableOnCompletion(auth.getName());
    }

    /* ================= HOLD TRIP ================= */

    @PostMapping("/{tripId}/hold")
    public void holdTrip(
            @PathVariable String tripId,
            Authentication auth
    ) {

        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        tripService.holdTrip(tripId);
        driverService.markDriverAvailableOnHold(auth.getName());
    }

    /* ================= RESUME TRIP ================= */

    @PostMapping("/{tripId}/resume")
    public void resumeTrip(
            @PathVariable String tripId,
            Authentication auth
    ) {

        if (auth == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login required"
            );
        }

        tripService.resumeTrip(tripId);
        driverService.markDriverUnavailableOnResume(auth.getName(), tripId);
    }
}
