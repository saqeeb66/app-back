package com.arcotcabs.arcotcabs_backend.service;

import com.arcotcabs.arcotcabs_backend.dto.CreateDriverRequest;
import com.arcotcabs.arcotcabs_backend.dto.DriverResponse;
import com.arcotcabs.arcotcabs_backend.model.Driver;
import com.arcotcabs.arcotcabs_backend.model.Trip;
import com.arcotcabs.arcotcabs_backend.model.User;
import com.arcotcabs.arcotcabs_backend.model.enums.TripStatus;
import com.arcotcabs.arcotcabs_backend.repository.DriverRepository;
import com.arcotcabs.arcotcabs_backend.repository.TripRepository;
import com.arcotcabs.arcotcabs_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class DriverService {

    private final DriverRepository driverRepo;
    private final TripRepository tripRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public DriverService(
            DriverRepository driverRepo,
            TripRepository tripRepo,
            UserRepository userRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.driverRepo = driverRepo;
        this.tripRepo = tripRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /* ================= ADMIN ================= */

    public void createDriverWithUser(CreateDriverRequest req) {

        if (userRepo.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already exists with this email"
            );
        }

        long now = System.currentTimeMillis();

        // 🔹 CREATE LOGIN USER
        User user = new User();
        user.setUserId(req.getEmail());
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole("DRIVER");
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setCreatedAt(now);

        userRepo.save(user);

        // 🔹 CREATE DRIVER
        Driver driver = new Driver();
        driver.setDriverId(UUID.randomUUID().toString());
        driver.setUserId(req.getEmail());
        driver.setName(req.getName());
        driver.setEmail(req.getEmail());
        driver.setPhone(req.getPhone());
        driver.setCarType(req.getCarType());
        driver.setCarNumber(req.getCarNumber());
        driver.setAvailable(true);
        driver.setCurrentTripId(null);

        driverRepo.save(driver);
    }

    /* ================= AVAILABLE DRIVERS ================= */
    /* 🔹 INTERNAL USE (ENTITY) */
    public List<Driver> getAvailableDriverEntities() {
        return driverRepo.findAvailableDrivers();
    }

    /* 🔹 ADMIN API (DTO) */
    public List<DriverResponse> getAvailableDrivers() {
        return driverRepo.findAvailableDrivers()
                .stream()
                .map(driver -> new DriverResponse(
                        driver.getDriverId(),
                        driver.getName(),
                        driver.getEmail(),   // ✅ FIXED (NO getUser())
                        driver.getPhone(),
                        driver.getCarType(),
                        driver.getCarNumber()
                ))
                .toList();
    }

    /* ================= ASSIGN DRIVER ================= */

    public void assignDriverToTrip(String tripId, String driverId) {

        Trip trip = tripRepo.findById(tripId);
        if (trip == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Trip not found"
            );
        }

        if (trip.getStatus() != TripStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Trip already assigned"
            );
        }

        Driver driver = driverRepo.findById(driverId);
        if (driver == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Driver not found"
            );
        }

        if (!driver.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Driver is not available"
            );
        }

        // 🔹 UPDATE TRIP
        trip.setDriverId(driver.getDriverId());
        trip.setDriverName(driver.getName());
        trip.setDriverPhone(driver.getPhone());
        trip.setDriverCarType(driver.getCarType());
        trip.setDriverCarNumber(driver.getCarNumber());
        trip.setStatus(TripStatus.DRIVER_ASSIGNED);

        tripRepo.assignDriver(trip);
        // 🔹 UPDATE DRIVER
        driver.setAvailable(false);
        driver.setCurrentTripId(tripId);
        driverRepo.save(driver);
    }

    /* ================= DRIVER TRIPS ================= */

    public List<Trip> getActiveTripsForDriver(String driverUserId) {

        Driver driver = driverRepo.findByUserId(driverUserId);
        if (driver == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Driver not found"
            );
        }

        return tripRepo.findAll()
                .stream()
                .filter(t ->
                        driver.getDriverId().equals(t.getDriverId()) &&
                                (t.getStatus() == TripStatus.DRIVER_ASSIGNED ||
                                        t.getStatus() == TripStatus.TRIP_STARTED ||
                                        t.getStatus() == TripStatus.TRIP_ON_HOLD)
                )
                .toList();
    }

    public List<Trip> getCompletedTripsForDriver(String driverUserId) {

        Driver driver = driverRepo.findByUserId(driverUserId);
        if (driver == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Driver not found"
            );
        }

        return tripRepo.findAll()
                .stream()
                .filter(t ->
                        driver.getDriverId().equals(t.getDriverId()) &&
                                t.getStatus() == TripStatus.TRIP_COMPLETED
                )
                .toList();
    }

    /* ================= DRIVER STATE ================= */

    public void markDriverAvailableOnHold(String driverUserId) {

        Driver driver = driverRepo.findByUserId(driverUserId);
        if (driver == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Driver not found"
            );
        }

        driver.setAvailable(true);
        driverRepo.save(driver);
    }
    public String getDriverIdByEmail(String email) {
        Driver driver = driverRepo.findByEmail(email);

        if (driver == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Driver profile not found"
            );
        }

        return driver.getDriverId();
    }


    public void markDriverUnavailableOnResume(String driverUserId, String tripId) {

        Driver driver = driverRepo.findByUserId(driverUserId);
        if (driver == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Driver not found"
            );
        }

        driver.setAvailable(false);
        driver.setCurrentTripId(tripId);
        driverRepo.save(driver);
    }

    public void markDriverAvailableOnCompletion(String driverUserId) {

        Driver driver = driverRepo.findByUserId(driverUserId);
        if (driver == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Driver not found"
            );
        }

        driver.setAvailable(true);
        driver.setCurrentTripId(null);
        driverRepo.save(driver);
    }
}
