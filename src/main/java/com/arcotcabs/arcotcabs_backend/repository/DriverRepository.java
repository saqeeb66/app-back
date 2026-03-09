package com.arcotcabs.arcotcabs_backend.repository;

import com.arcotcabs.arcotcabs_backend.model.Driver;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Repository
public class DriverRepository {

    private static final String TABLE = "Drivers";
    private final DynamoDbClient dynamoDb;

    public DriverRepository(DynamoDbClient dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    public void save(Driver driver) {

        Map<String, AttributeValue> item = new HashMap<>();

        item.put("driverId", AttributeValue.fromS(driver.getDriverId()));
        item.put("userId", AttributeValue.fromS(driver.getUserId()));
        item.put("name", AttributeValue.fromS(driver.getName()));
        item.put("email", AttributeValue.fromS(driver.getEmail()));
        item.put("phone", AttributeValue.fromS(driver.getPhone()));
        item.put("carType", AttributeValue.fromS(driver.getCarType()));
        item.put("carNumber", AttributeValue.fromS(driver.getCarNumber()));

        // ✅ BOOLEAN (FIXED)
        item.put("available", AttributeValue.fromBool(driver.isAvailable()));

        if (driver.getCurrentTripId() != null) {
            item.put("currentTripId", AttributeValue.fromS(driver.getCurrentTripId()));
        }

        dynamoDb.putItem(
                PutItemRequest.builder()
                        .tableName(TABLE)
                        .item(item)
                        .build()
        );
    }

    public List<Driver> findAvailableDrivers() {
        return dynamoDb.scan(
                        ScanRequest.builder().tableName(TABLE).build()
                )
                .items()
                .stream()
                .map(this::fromItem)
                .filter(Driver::isAvailable) // ✅ FIXED
                .toList();
    }

    public Driver findById(String driverId) {
        return dynamoDb.scan(
                        ScanRequest.builder().tableName(TABLE).build()
                )
                .items()
                .stream()
                .map(this::fromItem)
                .filter(d -> driverId.equals(d.getDriverId()))
                .findFirst()
                .orElse(null);
    }

    private Driver fromItem(Map<String, AttributeValue> item) {

        Driver d = new Driver();

        d.setDriverId(getString(item, "driverId"));
        d.setUserId(getString(item, "userId"));
        d.setName(getString(item, "name"));
        d.setEmail(getString(item, "email"));
        d.setPhone(getString(item, "phone"));
        d.setCarType(getString(item, "carType"));
        d.setCarNumber(getString(item, "carNumber"));

        d.setAvailable(getBoolean(item, "available"));
        d.setCurrentTripId(getString(item, "currentTripId"));

        return d;
    }

    /* ================= SAFE HELPERS ================= */

    private String getString(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) && item.get(key).s() != null
                ? item.get(key).s()
                : null;
    }

    private boolean getBoolean(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) && item.get(key).bool() != null
                ? item.get(key).bool()
                : false;
    }


    public boolean existsByEmail(String email) {
        return dynamoDb.scan(
                        ScanRequest.builder().tableName(TABLE).build()
                )
                .items()
                .stream()
                .anyMatch(i -> email.equals(i.get("email").s()));
    }
    public Driver findByEmail(String email) {
        return dynamoDb.scan(
                        ScanRequest.builder()
                                .tableName(TABLE)
                                .build()
                ).items()
                .stream()
                .filter(i -> email.equals(i.get("email").s()))
                .map(this::fromItem)   // same mapper you already use
                .findFirst()
                .orElse(null);
    }


    public boolean existsByPhone(String phone) {
        return dynamoDb.scan(
                        ScanRequest.builder().tableName(TABLE).build()
                )
                .items()
                .stream()
                .anyMatch(i -> phone.equals(i.get("phone").s()));
    }
    public Driver findByUserId(String userId) {
        return dynamoDb.scan(
                        ScanRequest.builder().tableName(TABLE).build()
                )
                .items()
                .stream()
                .map(this::fromItem)
                .filter(d -> userId.equals(d.getUserId()))
                .findFirst()
                .orElse(null);
    }

}
