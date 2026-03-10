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

        if (trip.getPickupLocation() == null || trip.getPickupLocation().isBlank())
            throw new IllegalStateException("pickupLocation required");

        if (trip.getDropLocation() == null || trip.getDropLocation().isBlank())
            throw new IllegalStateException("dropLocation required");

        if (trip.getVehicleType() == null || trip.getVehicleType().isBlank())
            throw new IllegalStateException("vehicleType required");

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

    /* ================= SAFE PUT HELPERS ================= */

    private void putS(Map<String, AttributeValue> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, AttributeValue.fromS(value));
        }
    }

    private void putN(Map<String, AttributeValue> map, String key, Number value) {
        if (value != null) {
            map.put(key, AttributeValue.fromN(String.valueOf(value)));
        }
    }

    /* ================= ASSIGN DRIVER ================= */

    public void assignDriver(Trip trip) {

        Map<String, AttributeValue> key =
                Map.of("tripId", AttributeValue.fromS(trip.getTripId()));

        Map<String, AttributeValue> values = new HashMap<>();

        putS(values, ":driverId", trip.getDriverId());
        putS(values, ":driverName", trip.getDriverName());
        putS(values, ":driverPhone", trip.getDriverPhone());
        putS(values, ":driverCarType", trip.getDriverCarType());
        putS(values, ":driverCarNumber", trip.getDriverCarNumber());
        putS(values, ":status", trip.getStatus().name());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression(
                        "SET driverId=:driverId, driverName=:driverName, driverPhone=:driverPhone, " +
                        "driverCarType=:driverCarType, driverCarNumber=:driverCarNumber, #status=:status"
                )
                .expressionAttributeNames(Map.of("#status","status"))
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= UPDATE START TRIP ================= */

    public void updateStartTrip(Trip trip) {

        Map<String, AttributeValue> key =
                Map.of("tripId", AttributeValue.fromS(trip.getTripId()));

        Map<String, AttributeValue> values = new HashMap<>();

        putS(values, ":startLocation", trip.getStartLocation());
        putN(values, ":startKm", trip.getStartKm());
        putN(values, ":startTime", trip.getStartTime());
        putS(values, ":odometerImageUrl", trip.getOdometerImageUrl());
        putS(values, ":status", trip.getStatus().name());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression(
                        "SET startLocation=:startLocation, startKm=:startKm, startTime=:startTime, " +
                        "odometerImageUrl=:odometerImageUrl, #status=:status"
                )
                .expressionAttributeNames(Map.of("#status","status"))
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= UPDATE END TRIP ================= */

    public void updateEndTrip(Trip trip) {

        Map<String, AttributeValue> key =
                Map.of("tripId", AttributeValue.fromS(trip.getTripId()));

        Map<String, AttributeValue> values = new HashMap<>();

        putS(values, ":endLocation", trip.getEndLocation());
        putN(values, ":endKm", trip.getEndKm());
        putN(values, ":endTime", trip.getEndTime());
        putS(values, ":endOdometerImageUrl", trip.getEndOdometerImageUrl());
        putS(values, ":signatureUrl", trip.getSignatureUrl());
        putS(values, ":status", trip.getStatus().name());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression(
                        "SET endLocation=:endLocation, endKm=:endKm, endTime=:endTime, " +
                        "endOdometerImageUrl=:endOdometerImageUrl, signatureUrl=:signatureUrl, #status=:status"
                )
                .expressionAttributeNames(Map.of("#status","status"))
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= UPDATE STATUS ================= */

    public void updateStatus(String tripId, TripStatus status) {

        Map<String, AttributeValue> key =
                Map.of("tripId", AttributeValue.fromS(tripId));

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET #status=:status")
                .expressionAttributeNames(Map.of("#status","status"))
                .expressionAttributeValues(
                        Map.of(":status", AttributeValue.fromS(status.name()))
                )
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= FETCH BY ID ================= */

    public Trip findById(String tripId) {

        Map<String, AttributeValue> key =
                Map.of("tripId", AttributeValue.fromS(tripId));

        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();

        var response = dynamoDb.getItem(request);

        if (!response.hasItem())
            return null;

        return fromItem(response.item());
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
                .sorted((a,b)->Long.compare(b.getCreatedAt(),a.getCreatedAt()))
                .toList();
    }

    /* ================= MAPPER ================= */

    private Map<String, AttributeValue> toItem(Trip t) {

        Map<String, AttributeValue> item = new HashMap<>();

        putS(item,"tripId",t.getTripId());
        putS(item,"userId",t.getUserId());
        putS(item,"userName",t.getUserName());
        putS(item,"userPhone",t.getUserPhone());
        putS(item,"pickupLocation",t.getPickupLocation());
        putS(item,"dropLocation",t.getDropLocation());
        putS(item,"vehicleType",t.getVehicleType());

        putN(item,"passengers",t.getPassengers());
        putN(item,"numberOfDays",t.getNumberOfDays());

        if (t.getStatus()!=null)
            item.put("status",AttributeValue.fromS(t.getStatus().name()));

        putN(item,"createdAt",t.getCreatedAt());

        return item;
    }

    private Trip fromItem(Map<String, AttributeValue> item) {

        Trip t = new Trip();

        if(item.containsKey("tripId")) t.setTripId(item.get("tripId").s());
        if(item.containsKey("userId")) t.setUserId(item.get("userId").s());
        if(item.containsKey("userName")) t.setUserName(item.get("userName").s());
        if(item.containsKey("userPhone")) t.setUserPhone(item.get("userPhone").s());
        if(item.containsKey("pickupLocation")) t.setPickupLocation(item.get("pickupLocation").s());
        if(item.containsKey("dropLocation")) t.setDropLocation(item.get("dropLocation").s());
        if(item.containsKey("vehicleType")) t.setVehicleType(item.get("vehicleType").s());

        if(item.containsKey("passengers"))
            t.setPassengers(Integer.parseInt(item.get("passengers").n()));

        if(item.containsKey("numberOfDays"))
            t.setNumberOfDays(Integer.parseInt(item.get("numberOfDays").n()));

        if(item.containsKey("status"))
            t.setStatus(TripStatus.valueOf(item.get("status").s()));

        if(item.containsKey("createdAt"))
            t.setCreatedAt(Long.parseLong(item.get("createdAt").n()));

        return t;
    }
}
