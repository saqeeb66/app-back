package com.arcotcabs.arcotcabs_backend.controller;

import com.arcotcabs.arcotcabs_backend.service.SignatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminSignatureController {

    private final SignatureService signatureService;

    public AdminSignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @GetMapping("/signature/{tripId}")
    public ResponseEntity<byte[]> getSignature(@PathVariable String tripId) {

        byte[] image = signatureService.getSignature(tripId);

        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(image);
    }
}
