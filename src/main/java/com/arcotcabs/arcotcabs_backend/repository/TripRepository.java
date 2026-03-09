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

        System.out.println("========== CREATE TRIP DEBUG ==========");
        System.out.println("Incoming Trip Object = " + trip);

        if (trip.getUserId() == null || trip.getUserId().isBlank())
            throw new IllegalStateException("userId is required");

        if (trip.getPickupLocation() == null || trip.getPickupLocation().isBlank())
            throw new IllegalStateException("pickupLocation is required");

        if (trip.getDropLocation() == null || trip.getDropLocation().isBlank())
            throw new IllegalStateException("dropLocation is required");

        if (trip.getVehicleType() == null || trip.getVehicleType().isBlank())
            throw new IllegalStateException("vehicleType is required");

        String tripId = UUID.randomUUID().toString();
        trip.setTripId(tripId);
        trip.setStatus(TripStatus.PENDING);
        trip.setCreatedAt(System.currentTimeMillis());

        Map<String, AttributeValue> item = toItem(trip);

        System.out.println("DynamoDB Item Map = " + item);

        // detect invalid attributes
        for (Map.Entry<String, AttributeValue> entry : item.entrySet()) {
            AttributeValue val = entry.getValue();
            if (val == null) {
                System.out.println("⚠ NULL attribute detected -> " + entry.getKey());
            }
        }

        dynamoDb.putItem(
                PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(item)
                        .build()
        );

        System.out.println("Trip successfully inserted: " + tripId);
        System.out.println("=======================================");

        return tripId;
    }

    /* ================= FETCH ALL ================= */

    public List<Trip> findAll() {

        ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        return dynamoDb.scan(request)
                .items()
                .stream()
                .map(this::fromItem)
                .toList();
    }

    /* ================= FETCH BY USER ================= */

    public List<Trip> findByUser(String userId) {

        return findAll().stream()
                .filter(t -> userId != null && userId.equals(t.getUserId()))
                .sorted((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()))
                .toList();
    }

    /* ================= ASSIGN DRIVER ================= */

    public void assignDriver(Trip trip) {

        Map<String, AttributeValue> key = Map.of(
                "tripId", AttributeValue.fromS(trip.getTripId())
        );

        Map<String, AttributeValue> values = new HashMap<>();

        putS(values, ":driverId", trip.getDriverId());
        putS(values, ":driverName", trip.getDriverName());
        putS(values, ":driverPhone", trip.getDriverPhone());
        putS(values, ":driverCarType", trip.getDriverCarType());
        putS(values, ":driverCarNumber", trip.getDriverCarNumber());

        if (trip.getStatus() != null)
            values.put(":status", AttributeValue.fromS(trip.getStatus().name()));

        System.out.println("Assign Driver Values = " + values);

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression(
                        "SET #driverId = :driverId, #driverName = :driverName, " +
                        "#driverPhone = :driverPhone, #driverCarType = :driverCarType, " +
                        "#driverCarNumber = :driverCarNumber, #status = :status"
                )
                .expressionAttributeNames(Map.of(
                        "#driverId", "driverId",
                        "#driverName", "driverName",
                        "#driverPhone", "driverPhone",
                        "#driverCarType", "driverCarType",
                        "#driverCarNumber", "driverCarNumber",
                        "#status", "status"
                ))
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

    /* ================= SAFE STRING PUT ================= */

    private void putS(Map<String, AttributeValue> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, AttributeValue.fromS(value));
        } else {
            System.out.println("Skipped empty field -> " + key);
        }
    }

    /* ================= MAPPER ================= */

    private Map<String, AttributeValue> toItem(Trip t) {

        Map<String, AttributeValue> item = new HashMap<>();

        if (t.getTripId() != null && !t.getTripId().isBlank())
            item.put("tripId", AttributeValue.fromS(t.getTripId()));

        if (t.getUserId() != null && !t.getUserId().isBlank())
            item.put("userId", AttributeValue.fromS(t.getUserId()));

        if (t.getUserName() != null && !t.getUserName().isBlank())
            item.put("userName", AttributeValue.fromS(t.getUserName()));

        if (t.getUserPhone() != null && !t.getUserPhone().isBlank())
            item.put("userPhone", AttributeValue.fromS(t.getUserPhone()));

        if (t.getPickupLocation() != null && !t.getPickupLocation().isBlank())
            item.put("pickupLocation", AttributeValue.fromS(t.getPickupLocation()));

        if (t.getDropLocation() != null && !t.getDropLocation().isBlank())
            item.put("dropLocation", AttributeValue.fromS(t.getDropLocation()));

        if (t.getVehicleType() != null && !t.getVehicleType().isBlank())
            item.put("vehicleType", AttributeValue.fromS(t.getVehicleType()));

        if (t.getDriverName() != null && !t.getDriverName().isBlank())
            item.put("driverName", AttributeValue.fromS(t.getDriverName()));

        if (t.getDriverPhone() != null && !t.getDriverPhone().isBlank())
            item.put("driverPhone", AttributeValue.fromS(t.getDriverPhone()));

        if (t.getDriverCarType() != null && !t.getDriverCarType().isBlank())
            item.put("driverCarType", AttributeValue.fromS(t.getDriverCarType()));

        if (t.getDriverCarNumber() != null && !t.getDriverCarNumber().isBlank())
            item.put("driverCarNumber", AttributeValue.fromS(t.getDriverCarNumber()));

        if (t.getPassengers() > 0)
            item.put("passengers",
                    AttributeValue.fromN(String.valueOf(t.getPassengers())));

        if (t.getNumberOfDays() > 0)
            item.put("numberOfDays",
                    AttributeValue.fromN(String.valueOf(t.getNumberOfDays())));

        if (t.getStatus() != null)
            item.put("status",
                    AttributeValue.fromS(t.getStatus().name()));

        item.put("createdAt",`
                AttributeValue.fromN(String.valueOf(t.getCreatedAt())));

        System.out.println("Generated DynamoDB Item = " + item);

        return item;
    }

    /* ================= FROM ITEM ================= */

    private Trip fromItem(Map<String, AttributeValue> item) {

        Trip t = new Trip();

        if (item.containsKey("tripId"))
            t.setTripId(item.get("tripId").s());

        if (item.containsKey("userId"))
            t.setUserId(item.get("userId").s());

        if (item.containsKey("userName"))
            t.setUserName(item.get("userName").s());

        if (item.containsKey("userPhone"))
            t.setUserPhone(item.get("userPhone").s());

        if (item.containsKey("pickupLocation"))
            t.setPickupLocation(item.get("pickupLocation").s());

        if (item.containsKey("dropLocation"))
            t.setDropLocation(item.get("dropLocation").s());

        if (item.containsKey("vehicleType"))
            t.setVehicleType(item.get("vehicleType").s());

        if (item.containsKey("passengers"))
            t.setPassengers(Integer.parseInt(item.get("passengers").n()));
        else
            t.setPassengers(1);

        if (item.containsKey("numberOfDays"))
            t.setNumberOfDays(Integer.parseInt(item.get("numberOfDays").n()));
        else
            t.setNumberOfDays(1);

        if (item.containsKey("status"))
            t.setStatus(TripStatus.valueOf(item.get("status").s()));

        if (item.containsKey("createdAt"))
            t.setCreatedAt(Long.parseLong(item.get("createdAt").n()));

        return t;
    }
}
