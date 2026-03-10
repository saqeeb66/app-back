package com.arcotcabs.arcotcabs_backend.repository;

import com.arcotcabs.arcotcabs_backend.model.Trip;
import com.arcotcabs.arcotcabs_backend.model.enums.TripStatus;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Repository
public class TripRepository {

    private static final String TABLE_NAME = "Trips";

    private final DynamoDbClient dynamoDb;

    public TripRepository(DynamoDbClient dynamoDb) {
        this.dynamoDb = dynamoDb;
    }

    /* ================= CREATE TRIP ================= */

    public String createTrip(Trip trip) {

        if (trip.getUserId() == null || trip.getUserId().isBlank())
            throw new RuntimeException("userId required");

        if (trip.getPickupLocation() == null || trip.getPickupLocation().isBlank())
            throw new RuntimeException("pickupLocation required");

        if (trip.getDropLocation() == null || trip.getDropLocation().isBlank())
            throw new RuntimeException("dropLocation required");

        String tripId = UUID.randomUUID().toString();

        trip.setTripId(tripId);
        trip.setStatus(TripStatus.PENDING);
        trip.setCreatedAt(System.currentTimeMillis());

        Map<String, AttributeValue> item = buildItem(trip);

        dynamoDb.putItem(
                PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(item)
                        .build()
        );

        return tripId;
    }

    /* ================= BUILD ITEM SAFELY ================= */

    private Map<String, AttributeValue> buildItem(Trip trip) {

        Map<String, AttributeValue> item = new HashMap<>();

        putString(item, "tripId", trip.getTripId());
        putString(item, "userId", trip.getUserId());
        putString(item, "userName", trip.getUserName());
        putString(item, "userPhone", trip.getUserPhone());
        putString(item, "pickupLocation", trip.getPickupLocation());
        putString(item, "dropLocation", trip.getDropLocation());
        putString(item, "vehicleType", trip.getVehicleType());

        putString(item, "driverId", trip.getDriverId());
        putString(item, "driverName", trip.getDriverName());
        putString(item, "driverPhone", trip.getDriverPhone());
        putString(item, "driverCarType", trip.getDriverCarType());
        putString(item, "driverCarNumber", trip.getDriverCarNumber());

        putNumber(item, "passengers", trip.getPassengers());
        putNumber(item, "numberOfDays", trip.getNumberOfDays());
        putNumber(item, "createdAt", trip.getCreatedAt());

        if (trip.getStatus() != null) {
            item.put("status", AttributeValue.fromS(trip.getStatus().name()));
        }

        return item;
    }

    /* ================= SAFE HELPERS ================= */

    private void putString(Map<String, AttributeValue> item, String key, String value) {

        if (value != null && !value.trim().isEmpty()) {
            item.put(key, AttributeValue.fromS(value));
        }
    }

    private void putNumber(Map<String, AttributeValue> item, String key, Number value) {

        if (value != null) {
            item.put(key, AttributeValue.fromN(String.valueOf(value)));
        }
    }

}
