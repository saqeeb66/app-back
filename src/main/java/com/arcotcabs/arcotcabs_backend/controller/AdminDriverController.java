package com.arcotcabs.arcotcabs_backend.controller;

import com.arcotcabs.arcotcabs_backend.dto.CreateDriverRequest;
import com.arcotcabs.arcotcabs_backend.dto.DriverResponse;
import com.arcotcabs.arcotcabs_backend.service.DriverService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/drivers")
@CrossOrigin
public class AdminDriverController {

    private final DriverService service;

    public AdminDriverController(DriverService service) {
        this.service = service;
    }

    /* ================= CREATE DRIVER ================= */

    @PostMapping("/add")
    public void createDriver(@RequestBody CreateDriverRequest req) {
        service.createDriverWithUser(req);
    }

    /* ================= AVAILABLE DRIVERS ================= */

    @GetMapping("/available")
    public List<DriverResponse> availableDrivers() {
        return service.getAvailableDrivers();
    }

    /* ================= ASSIGN DRIVER ================= */

    @PostMapping("/assign/{tripId}/{driverId}")
    public void assignDriver(
            @PathVariable String tripId,
            @PathVariable String driverId
    ) {
        service.assignDriverToTrip(tripId, driverId);
    }
}
