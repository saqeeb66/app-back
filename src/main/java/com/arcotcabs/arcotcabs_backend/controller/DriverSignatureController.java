package com.arcotcabs.arcotcabs_backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Base64;

@RestController
@RequestMapping("/api/driver/signature")
@CrossOrigin
public class DriverSignatureController {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    public DriverSignatureController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @PostMapping("/{tripId}")
    public ResponseEntity<String> uploadSignature(
            @PathVariable String tripId,
            @org.springframework.web.bind.annotation.RequestBody String base64
    ) {
        try {
            // ✅ SAFE BASE64 HANDLING
            String cleanBase64 = base64;
            if (base64.contains(",")) {
                cleanBase64 = base64.substring(base64.indexOf(",") + 1);
            }

            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

            String key = "signatures/" + tripId + ".png";

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType("image/png")
                            .build(),
                    RequestBody.fromBytes(imageBytes)
            );

            // ✅ SAME URL FORMAT AS YOUR EXISTING OBJECTS
            String url =
                    "https://" + bucket +
                            ".s3." + region +
                            ".amazonaws.com/" + key;

            return ResponseEntity.ok(url);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body("Signature upload failed");
        }
    }
}
