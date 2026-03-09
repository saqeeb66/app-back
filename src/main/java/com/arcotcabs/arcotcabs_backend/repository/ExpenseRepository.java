package com.arcotcabs.arcotcabs_backend.repository;

import com.arcotcabs.arcotcabs_backend.model.Expense;
import com.arcotcabs.arcotcabs_backend.model.enums.ExpenseType;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Repository
public class ExpenseRepository {

    private static final String TABLE = "Expenses";
    private final DynamoDbClient dynamoDb;

    public ExpenseRepository(DynamoDbClient dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    public void addExpense(Expense e) {

        Map<String, AttributeValue> item = new HashMap<>();

        item.put("tripId", AttributeValue.fromS(e.getTripId()));
        item.put("expenseId", AttributeValue.fromS(e.getExpenseId()));
        item.put("type", AttributeValue.fromS(e.getType().name()));
        item.put("amount", AttributeValue.fromN(String.valueOf(e.getAmount())));
        item.put("description", AttributeValue.fromS(e.getDescription()));
        item.put("addedBy", AttributeValue.fromS(e.getAddedBy()));
        item.put("createdAt", AttributeValue.fromN(
                String.valueOf(e.getCreatedAt()))
        );

        dynamoDb.putItem(
                PutItemRequest.builder()
                        .tableName(TABLE)
                        .item(item)
                        .build()
        );
    }

    public List<Expense> getByTrip(String tripId) {

        ScanRequest scan =
                ScanRequest.builder().tableName(TABLE).build();

        return dynamoDb.scan(scan)
                .items()
                .stream()
                .filter(i -> tripId.equals(i.get("tripId").s()))
                .map(this::fromItem)
                .toList();
    }

    private Expense fromItem(Map<String, AttributeValue> i) {

        Expense e = new Expense();
        e.setExpenseId(i.get("expenseId").s());
        e.setTripId(i.get("tripId").s());
        e.setType(ExpenseType.valueOf(i.get("type").s()));
        e.setAmount(Double.parseDouble(i.get("amount").n()));
        e.setDescription(i.get("description").s());
        e.setAddedBy(i.get("addedBy").s());
        e.setCreatedAt(Long.parseLong(i.get("createdAt").n()));

        return e;
    }
}
