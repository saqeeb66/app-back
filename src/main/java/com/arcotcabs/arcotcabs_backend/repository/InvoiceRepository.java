package com.arcotcabs.arcotcabs_backend.repository;



import com.arcotcabs.arcotcabs_backend.model.Invoice;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;

@Repository
public class InvoiceRepository {

    private static final String TABLE = "Invoices";
    private final DynamoDbClient dynamoDb;

    public InvoiceRepository(DynamoDbClient dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    public void save(Invoice invoice) {

        dynamoDb.putItem(
                PutItemRequest.builder()
                        .tableName(TABLE)
                        .item(Map.of(
                                "invoiceId", AttributeValue.fromS(invoice.getInvoiceId()),
                                "tripId", AttributeValue.fromS(invoice.getTripId()),
                                "totalAmount", AttributeValue.fromN(
                                        String.valueOf(invoice.getTotalAmount()))
                        ))
                        .build()
        );
    }
}
