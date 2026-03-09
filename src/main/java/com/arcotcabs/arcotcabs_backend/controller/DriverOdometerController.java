package com.arcotcabs.arcotcabs_backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@RestController
@RequestMapping("/api/driver/odometer")
@CrossOrigin
public class DriverOdometerController {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    public DriverOdometerController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /* ================= START ODOMETER UPLOAD ================= */

    @PostMapping("/start/{tripId}")
    public ResponseEntity<String> uploadStartOdometer(
            @PathVariable String tripId,
            @RequestParam("file") MultipartFile file
    ) {

        try {

            String key = "odometers/start/" + tripId + ".jpg";

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            String url =
                    "https://" + bucket +
                            ".s3." + region +
                            ".amazonaws.com/" + key;

            return ResponseEntity.ok(url);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body("Start odometer upload failed");
        }
    }

    /* ================= END ODOMETER UPLOAD ================= */

    @PostMapping("/end/{tripId}")
    public ResponseEntity<String> uploadEndOdometer(
            @PathVariable String tripId,
            @RequestParam("file") MultipartFile file
    ) {

        try {

            String key = "odometers/end/" + tripId + ".jpg";

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            String url =
                    "https://" + bucket +
                            ".s3." + region +
                            ".amazonaws.com/" + key;

            return ResponseEntity.ok(url);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body("End odometer upload failed");
        }
    }

    /* ================= GET START ODOMETER ================= */

    @GetMapping("/start/{tripId}")
    public ResponseEntity<byte[]> getStartOdometer(
            @PathVariable String tripId
    ) {

        try {

            String key = "odometers/start/" + tripId + ".jpg";

            byte[] imageBytes = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            ).readAllBytes();

            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(imageBytes);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /* ================= GET END ODOMETER ================= */

    @GetMapping("/end/{tripId}")
    public ResponseEntity<byte[]> getEndOdometer(
            @PathVariable String tripId
    ) {

        try {

            String key = "odometers/end/" + tripId + ".jpg";

            byte[] imageBytes = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            ).readAllBytes();

            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(imageBytes);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
