package com.arcotcabs.arcotcabs_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;

@Service
public class SignatureService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public SignatureService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public byte[] getSignature(String tripId) {
        try {
            String key = "signatures/" + tripId + ".png";

            ResponseInputStream<?> stream =
                    s3Client.getObject(
                            GetObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(key)
                                    .build()
                    );

            return stream.readAllBytes();

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to read signature"
            );
        }
    }
}
