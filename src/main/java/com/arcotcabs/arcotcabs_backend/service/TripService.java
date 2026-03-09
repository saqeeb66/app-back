package com.arcotcabs.arcotcabs_backend.service;

import com.arcotcabs.arcotcabs_backend.model.Trip;
import com.arcotcabs.arcotcabs_backend.model.enums.TripStatus;
import com.arcotcabs.arcotcabs_backend.repository.TripRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TripService {

    private final TripRepository repository;

    public TripService(TripRepository repository) {
        this.repository = repository;
    }

    /* ================= USER ================= */

    public String bookTrip(Trip trip) {
        trip.setStatus(TripStatus.PENDING);
        trip.setCreatedAt(System.currentTimeMillis());
        return repository.createTrip(trip);
    }

    public List<Trip> getUserTrips(String userId) {
        return repository.findByUser(userId);
    }

    /* ================= ADMIN ================= */

    public List<Trip> getAllTrips() {
        return repository.findAll();
    }

    public void assignDriver(
            String tripId,
            String driverId,
            String driverName,
            String driverPhone,
            String carType,
            String carNumber
    ) {

        Trip trip = getTripById(tripId);

        if (trip.getStatus() != TripStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trip is not in PENDING state"
            );
        }

        trip.setDriverId(driverId);
        trip.setDriverName(driverName);
        trip.setDriverPhone(driverPhone);
        trip.setDriverCarType(carType);
        trip.setDriverCarNumber(carNumber);
        trip.setStatus(TripStatus.DRIVER_ASSIGNED);

        repository.updateStatus(tripId, TripStatus.DRIVER_ASSIGNED);
    }

    /* ================= DRIVER ================= */

    /* ===== START TRIP ===== */

    public void startTrip(
            String tripId,
            String driverId,
            String startLocation,
            Double startKm,
            String startOdometerImageUrl
    ) {

        Trip trip = getTripById(tripId);

        if (!driverId.equals(trip.getDriverId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not assigned to this trip"
            );
        }

        if (trip.getStatus() != TripStatus.DRIVER_ASSIGNED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trip cannot be started"
            );
        }

        trip.setStartLocation(startLocation);
        trip.setStartKm(startKm);
        trip.setStartTime(System.currentTimeMillis());
        trip.setOdometerImageUrl(startOdometerImageUrl);
        trip.setStatus(TripStatus.TRIP_STARTED);

        repository.updateStartTrip(trip);
    }

    /* ===== HOLD TRIP ===== */

    public void holdTrip(String tripId) {

        Trip trip = getTripById(tripId);

        if (trip.getStatus() != TripStatus.TRIP_STARTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only started trips can be put on hold"
            );
        }

        repository.updateStatus(tripId, TripStatus.TRIP_ON_HOLD);
    }

    /* ===== RESUME TRIP ===== */

    public void resumeTrip(String tripId) {

        Trip trip = getTripById(tripId);

        if (trip.getStatus() != TripStatus.TRIP_ON_HOLD) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only held trips can be resumed"
            );
        }

        repository.updateStatus(tripId, TripStatus.TRIP_STARTED);
    }

    /* ===== END TRIP ===== */

    public void endTrip(
            String tripId,
            String endLocation,
            Double endKm,
            String endOdometerImageUrl,
            String signatureUrl
    ) {

        Trip trip = getTripById(tripId);

        if (trip.getStatus() != TripStatus.TRIP_STARTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trip cannot be ended"
            );
        }

        trip.setEndLocation(endLocation);
        trip.setEndKm(endKm);
        trip.setEndTime(System.currentTimeMillis());
        trip.setEndOdometerImageUrl(endOdometerImageUrl);
        trip.setSignatureUrl(signatureUrl);
        trip.setStatus(TripStatus.TRIP_COMPLETED);

        repository.updateEndTrip(trip);
    }

    /* ================= COMMON ================= */

    public Trip getTripById(String tripId) {

        Trip trip = repository.findById(tripId);

        if (trip == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Trip not found"
            );
        }

        return trip;
    }
}