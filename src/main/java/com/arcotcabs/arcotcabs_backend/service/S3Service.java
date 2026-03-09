package com.arcotcabs.arcotcabs_backend.service;


import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

    private static final String BUCKET = "arcot-cabs-storage2026";
    private final S3Client s3;

    public S3Service(S3Client s3) {
        this.s3 = s3;
    }

    public String uploadInvoice(String invoiceId, byte[] pdfBytes) {

        String key = "invoices/" + invoiceId + ".pdf";

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET)
                        .key(key)
                        .contentType("application/pdf")
                        .build(),
                RequestBody.fromBytes(pdfBytes)
        );

        return "https://" + BUCKET +
                ".s3.ap-south-1.amazonaws.com/" + key;
    }
}
