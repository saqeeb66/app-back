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
                .sorted((a, b) ->
                        Long.compare(b.getCreatedAt(), a.getCreatedAt()))
                .toList();
    }

    /* ================= FETCH BY ID ================= */
    public void assignDriver(Trip trip) {

        Map<String, AttributeValue> key = Map.of(
                "tripId", AttributeValue.fromS(trip.getTripId())
        );

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression(
                        "SET #driverId = :driverId, " +
                                "#driverName = :driverName, " +
                                "#driverPhone = :driverPhone, " +
                                "#driverCarType = :driverCarType, " +
                                "#driverCarNumber = :driverCarNumber, " +
                                "#status = :status"
                )
                .expressionAttributeNames(Map.of(
                        "#driverId", "driverId",
                        "#driverName", "driverName",
                        "#driverPhone", "driverPhone",
                        "#driverCarType", "driverCarType",
                        "#driverCarNumber", "driverCarNumber",
                        "#status", "status"
                ))
                .expressionAttributeValues(Map.of(
                        ":driverId", AttributeValue.fromS(trip.getDriverId()),
                        ":driverName", AttributeValue.fromS(trip.getDriverName()),
                        ":driverPhone", AttributeValue.fromS(trip.getDriverPhone()),
                        ":driverCarType", AttributeValue.fromS(trip.getDriverCarType()),
                        ":driverCarNumber", AttributeValue.fromS(trip.getDriverCarNumber()),
                        ":status", AttributeValue.fromS(trip.getStatus().name())
                ))
                .build();

        dynamoDb.updateItem(request);
    }
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

    /* ================= UPDATE START TRIP ================= */

    public void updateStartTrip(Trip trip) {

        Map<String, AttributeValue> key = Map.of(
                "tripId", AttributeValue.fromS(trip.getTripId())
        );

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression(
                        "SET #startLocation = :startLocation, " +
                                "#startKm = :startKm, " +
                                "#startTime = :startTime, " +
                                "#odometerImageUrl = :odometerImageUrl, " +
                                "#status = :status"
                )
                .expressionAttributeNames(Map.of(
                        "#startLocation", "startLocation",
                        "#startKm", "startKm",
                        "#startTime", "startTime",
                        "#odometerImageUrl", "odometerImageUrl",
                        "#status", "status"
                ))
                .expressionAttributeValues(Map.of(
                        ":startLocation", AttributeValue.fromS(trip.getStartLocation()),
                        ":startKm", AttributeValue.fromN(String.valueOf(trip.getStartKm())),
                        ":startTime", AttributeValue.fromN(String.valueOf(trip.getStartTime())),
                        ":odometerImageUrl", AttributeValue.fromS(trip.getOdometerImageUrl()),
                        ":status", AttributeValue.fromS(trip.getStatus().name())
                ))
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= UPDATE END TRIP ================= */

    public void updateEndTrip(Trip trip) {

        Map<String, AttributeValue> key = Map.of(
                "tripId", AttributeValue.fromS(trip.getTripId())
        );

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression(
                        "SET #endLocation = :endLocation, " +
                                "#endKm = :endKm, " +
                                "#endTime = :endTime, " +
                                "#endOdometerImageUrl = :endOdometerImageUrl, " +
                                "#signatureUrl = :signatureUrl, " +
                                "#status = :status"
                )
                .expressionAttributeNames(Map.of(
                        "#endLocation", "endLocation",
                        "#endKm", "endKm",
                        "#endTime", "endTime",
                        "#endOdometerImageUrl", "endOdometerImageUrl",
                        "#signatureUrl", "signatureUrl",
                        "#status", "status"
                ))
                .expressionAttributeValues(Map.of(
                        ":endLocation", AttributeValue.fromS(trip.getEndLocation()),
                        ":endKm", AttributeValue.fromN(String.valueOf(trip.getEndKm())),
                        ":endTime", AttributeValue.fromN(String.valueOf(trip.getEndTime())),
                        ":endOdometerImageUrl", AttributeValue.fromS(trip.getEndOdometerImageUrl()),
                        ":signatureUrl", AttributeValue.fromS(trip.getSignatureUrl()),
                        ":status", AttributeValue.fromS(trip.getStatus().name())
                ))
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= UPDATE STATUS ONLY ================= */

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

    /* ================= MAPPER ================= */

    private Map<String, AttributeValue> toItem(Trip t) {

        Map<String, AttributeValue> item = new HashMap<>();

        item.put("tripId", AttributeValue.fromS(t.getTripId()));
        item.put("userId", AttributeValue.fromS(t.getUserId()));
        item.put("userName", AttributeValue.fromS(t.getUserName()));
        item.put("userPhone", AttributeValue.fromS(t.getUserPhone()));
        item.put("pickupLocation", AttributeValue.fromS(t.getPickupLocation()));
        item.put("dropLocation", AttributeValue.fromS(t.getDropLocation()));
        item.put("vehicleType", AttributeValue.fromS(t.getVehicleType()));
        item.put("DriverName", AttributeValue.fromS(t.getDriverName()));
        item.put("DriverPhone", AttributeValue.fromS(t.getDriverPhone()));
        item.put("driverCarType",AttributeValue.fromS(t.getDriverCarType()));
        item.put("driverCarNumber",AttributeValue.fromS(t.getDriverCarNumber()));
        item.put("passengers", AttributeValue.fromN(String.valueOf(t.getPassengers())));
        item.put("numberOfDays", AttributeValue.fromN(String.valueOf(t.getNumberOfDays())));
        item.put("status", AttributeValue.fromS(t.getStatus().name()));
        item.put("createdAt", AttributeValue.fromN(String.valueOf(t.getCreatedAt())));

        return item;
    }

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

        if (item.containsKey("passengers") && item.get("passengers").n() != null)
            t.setPassengers(Integer.parseInt(item.get("passengers").n()));
        else
            t.setPassengers(1); // default

        if (item.containsKey("numberOfDays") && item.get("numberOfDays").n() != null)
            t.setNumberOfDays(Integer.parseInt(item.get("numberOfDays").n()));
        else
            t.setNumberOfDays(1);

        if (item.containsKey("status"))
            t.setStatus(TripStatus.valueOf(item.get("status").s()));

        if (item.containsKey("createdAt"))
            t.setCreatedAt(Long.parseLong(item.get("createdAt").n()));

        /* ===== DRIVER INFO ===== */

        if (item.containsKey("driverId"))
            t.setDriverId(item.get("driverId").s());

        if (item.containsKey("driverName"))
            t.setDriverName(item.get("driverName").s());

        if (item.containsKey("driverPhone"))
            t.setDriverPhone(item.get("driverPhone").s());

        if (item.containsKey("driverCarType"))
            t.setDriverCarType(item.get("driverCarType").s());

        if (item.containsKey("driverCarNumber"))
            t.setDriverCarNumber(item.get("driverCarNumber").s());

        /* ===== START INFO ===== */

        if (item.containsKey("startLocation"))
            t.setStartLocation(item.get("startLocation").s());

        if (item.containsKey("startKm") && item.get("startKm").n() != null)
            t.setStartKm(Double.parseDouble(item.get("startKm").n()));

        if (item.containsKey("startTime") && item.get("startTime").n() != null)
            t.setStartTime(Long.parseLong(item.get("startTime").n()));

        if (item.containsKey("odometerImageUrl"))
            t.setOdometerImageUrl(item.get("odometerImageUrl").s());

        /* ===== END INFO ===== */

        if (item.containsKey("endLocation"))
            t.setEndLocation(item.get("endLocation").s());

        if (item.containsKey("endKm") && item.get("endKm").n() != null)
            t.setEndKm(Double.parseDouble(item.get("endKm").n()));

        if (item.containsKey("endTime") && item.get("endTime").n() != null)
            t.setEndTime(Long.parseLong(item.get("endTime").n()));

        if (item.containsKey("endOdometerImageUrl"))
            t.setEndOdometerImageUrl(item.get("endOdometerImageUrl").s());

        if (item.containsKey("signatureUrl"))
            t.setSignatureUrl(item.get("signatureUrl").s());

        return t;
    }
}