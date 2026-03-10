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
            throw new IllegalStateException("userId required");

        String tripId = UUID.randomUUID().toString();
        trip.setTripId(tripId);
        trip.setStatus(TripStatus.PENDING);
        trip.setCreatedAt(System.currentTimeMillis());

        dynamoDb.putItem(
                PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(toItem(trip))
                        .build()
        );

        return tripId;
    }

    /* ================= FETCH ALL ================= */

    public List<Trip> findAll() {

        ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        List<Map<String, AttributeValue>> items = dynamoDb.scan(request).items();

        List<Trip> trips = new ArrayList<>();

        for (Map<String, AttributeValue> item : items) {
            trips.add(fromItem(item));
        }

        return trips;
    }

    /* ================= FETCH BY USER ================= */

    public List<Trip> findByUser(String userId) {

        List<Trip> trips = new ArrayList<>();

        for (Trip t : findAll()) {
            if (userId != null && userId.equals(t.getUserId())) {
                trips.add(t);
            }
        }

        trips.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

        return trips;
    }

    /* ================= ASSIGN DRIVER ================= */

    public void assignDriver(Trip trip) {

        Map<String, AttributeValue> key = Map.of(
                "tripId", AttributeValue.fromS(trip.getTripId())
        );

        Map<String, AttributeValue> values = new HashMap<>();

        safeS(values, ":driverId", trip.getDriverId());
        safeS(values, ":driverName", trip.getDriverName());
        safeS(values, ":driverPhone", trip.getDriverPhone());
        safeS(values, ":driverCarType", trip.getDriverCarType());
        safeS(values, ":driverCarNumber", trip.getDriverCarNumber());
        safeS(values, ":status", trip.getStatus().name());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET driverId=:driverId, driverName=:driverName, driverPhone=:driverPhone, driverCarType=:driverCarType, driverCarNumber=:driverCarNumber, #status=:status")
                .expressionAttributeNames(Map.of("#status", "status"))
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= FETCH BY ID ================= */

    public Trip findById(String tripId) {

        Map<String, AttributeValue> key = Map.of(
                "tripId", AttributeValue.fromS(tripId)
        );

        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();

        var response = dynamoDb.getItem(request);

        if (!response.hasItem())
            return null;

        return fromItem(response.item());
    }

    /* ================= UPDATE STATUS ================= */

    public void updateStatus(String tripId, TripStatus status) {

        Map<String, AttributeValue> key = Map.of(
                "tripId", AttributeValue.fromS(tripId)
        );

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET #status = :status")
                .expressionAttributeNames(Map.of("#status", "status"))
                .expressionAttributeValues(
                        Map.of(":status", AttributeValue.fromS(status.name()))
                )
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= SAFE HELPERS ================= */

    private void safeS(Map<String, AttributeValue> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, AttributeValue.fromS(value));
        }
    }

    private void safeN(Map<String, AttributeValue> map, String key, Number value) {
        if (value != null) {
            map.put(key, AttributeValue.fromN(value.toString()));
        }
    }

    /* ================= OBJECT → DYNAMODB ================= */

    private Map<String, AttributeValue> toItem(Trip t) {

        Map<String, AttributeValue> item = new HashMap<>();

        safeS(item, "tripId", t.getTripId());
        safeS(item, "userId", t.getUserId());
        safeS(item, "userName", t.getUserName());
        safeS(item, "userPhone", t.getUserPhone());
        safeS(item, "pickupLocation", t.getPickupLocation());
        safeS(item, "dropLocation", t.getDropLocation());
        safeS(item, "vehicleType", t.getVehicleType());

        safeS(item, "driverName", t.getDriverName());
        safeS(item, "driverPhone", t.getDriverPhone());
        safeS(item, "driverCarType", t.getDriverCarType());
        safeS(item, "driverCarNumber", t.getDriverCarNumber());

        safeN(item, "passengers", t.getPassengers());
        safeN(item, "numberOfDays", t.getNumberOfDays());

        if (t.getStatus() != null)
            item.put("status", AttributeValue.fromS(t.getStatus().name()));

        safeN(item, "createdAt", t.getCreatedAt());

        return item;
    }

    /* ================= DYNAMODB → OBJECT ================= */

    private Trip fromItem(Map<String, AttributeValue> item) {

        Trip t = new Trip();

        if (item.get("tripId") != null)
            t.setTripId(item.get("tripId").s());

        if (item.get("userId") != null)
            t.setUserId(item.get("userId").s());

        if (item.get("userName") != null)
            t.setUserName(item.get("userName").s());

        if (item.get("userPhone") != null)
            t.setUserPhone(item.get("userPhone").s());

        if (item.get("pickupLocation") != null)
            t.setPickupLocation(item.get("pickupLocation").s());

        if (item.get("dropLocation") != null)
            t.setDropLocation(item.get("dropLocation").s());

        if (item.get("vehicleType") != null)
            t.setVehicleType(item.get("vehicleType").s());

        if (item.get("passengers") != null)
            t.setPassengers(Integer.parseInt(item.get("passengers").n()));

        if (item.get("numberOfDays") != null)
            t.setNumberOfDays(Integer.parseInt(item.get("numberOfDays").n()));

        if (item.get("status") != null)
            t.setStatus(TripStatus.valueOf(item.get("status").s()));

        if (item.get("createdAt") != null)
            t.setCreatedAt(Long.parseLong(item.get("createdAt").n()));

        return t;
    }
}
