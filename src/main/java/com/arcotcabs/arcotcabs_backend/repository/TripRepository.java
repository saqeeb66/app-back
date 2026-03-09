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

    Map<String, AttributeValue> item = new HashMap<>();

    putS(item,"tripId",trip.getTripId());
    putS(item,"userId",trip.getUserId());
    putS(item,"userName",trip.getUserName());
    putS(item,"userPhone",trip.getUserPhone());
    putS(item,"pickupLocation",trip.getPickupLocation());
    putS(item,"dropLocation",trip.getDropLocation());
    putS(item,"vehicleType",trip.getVehicleType());

    if(trip.getPassengers()!=null && trip.getPassengers()>0)
        item.put("passengers",AttributeValue.fromN(String.valueOf(trip.getPassengers())));

    if(trip.getNumberOfDays()!=null && trip.getNumberOfDays()>0)
        item.put("numberOfDays",AttributeValue.fromN(String.valueOf(trip.getNumberOfDays())));

    item.put("status",AttributeValue.fromS(trip.getStatus().name()));
    item.put("createdAt",AttributeValue.fromN(String.valueOf(trip.getCreatedAt())));

    dynamoDb.putItem(
            PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
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
                .sorted((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()))
                .toList();
    }

    /* ================= ASSIGN DRIVER ================= */

    public void assignDriver(Trip trip) {

        Map<String, AttributeValue> key = Map.of(
                "tripId", AttributeValue.fromS(trip.getTripId())
        );

        Map<String, AttributeValue> values = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        List<String> updates = new ArrayList<>();

        addStringUpdate(updates, names, values, "driverId", trip.getDriverId());
        addStringUpdate(updates, names, values, "driverName", trip.getDriverName());
        addStringUpdate(updates, names, values, "driverPhone", trip.getDriverPhone());
        addStringUpdate(updates, names, values, "driverCarType", trip.getDriverCarType());
        addStringUpdate(updates, names, values, "driverCarNumber", trip.getDriverCarNumber());

        if (trip.getStatus() != null) {
            names.put("#status", "status");
            values.put(":status", AttributeValue.fromS(trip.getStatus().name()));
            updates.add("#status = :status");
        }

        if (updates.isEmpty()) return;

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET " + String.join(", ", updates))
                .expressionAttributeNames(names)
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

    /* ================= UPDATE START TRIP ================= */

    public void updateStartTrip(Trip trip) {

        Map<String, AttributeValue> key = Map.of(
                "tripId", AttributeValue.fromS(trip.getTripId())
        );

        Map<String, AttributeValue> values = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        List<String> updates = new ArrayList<>();

        addStringUpdate(updates, names, values, "startLocation", trip.getStartLocation());
        addStringUpdate(updates, names, values, "odometerImageUrl", trip.getOdometerImageUrl());
        addNumberUpdate(updates, names, values, "startKm", trip.getStartKm());
        addNumberUpdate(updates, names, values, "startTime", trip.getStartTime());

        if (trip.getStatus() != null) {
            names.put("#status", "status");
            values.put(":status", AttributeValue.fromS(trip.getStatus().name()));
            updates.add("#status = :status");
        }

        if (updates.isEmpty()) return;

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET " + String.join(", ", updates))
                .expressionAttributeNames(names)
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= UPDATE END TRIP ================= */

    public void updateEndTrip(Trip trip) {

        Map<String, AttributeValue> key = Map.of(
                "tripId", AttributeValue.fromS(trip.getTripId())
        );

        Map<String, AttributeValue> values = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        List<String> updates = new ArrayList<>();

        addStringUpdate(updates, names, values, "endLocation", trip.getEndLocation());
        addStringUpdate(updates, names, values, "endOdometerImageUrl", trip.getEndOdometerImageUrl());
        addStringUpdate(updates, names, values, "signatureUrl", trip.getSignatureUrl());
        addNumberUpdate(updates, names, values, "endKm", trip.getEndKm());
        addNumberUpdate(updates, names, values, "endTime", trip.getEndTime());

        if (trip.getStatus() != null) {
            names.put("#status", "status");
            values.put(":status", AttributeValue.fromS(trip.getStatus().name()));
            updates.add("#status = :status");
        }

        if (updates.isEmpty()) return;

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET " + String.join(", ", updates))
                .expressionAttributeNames(names)
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(request);
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

    private void addStringUpdate(List<String> updates, Map<String,String> names,
                                 Map<String,AttributeValue> values, String field, String value) {

        if (value == null || value.isBlank()) return;

        String nameKey = "#" + field;
        String valueKey = ":" + field;

        names.put(nameKey, field);
        values.put(valueKey, AttributeValue.fromS(value));
        updates.add(nameKey + " = " + valueKey);
    }

    private void addNumberUpdate(List<String> updates, Map<String,String> names,
                                 Map<String,AttributeValue> values, String field, Number value) {

        if (value == null) return;

        String nameKey = "#" + field;
        String valueKey = ":" + field;

        names.put(nameKey, field);
        values.put(valueKey, AttributeValue.fromN(String.valueOf(value)));
        updates.add(nameKey + " = " + valueKey);
    }

    private void putS(Map<String, AttributeValue> item, String key, String value) {
        if (value != null && !value.isBlank())
            item.put(key, AttributeValue.fromS(value));
    }

    /* ================= MAPPER ================= */

    private Map<String, AttributeValue> toItem(Trip t) {

        Map<String, AttributeValue> item = new HashMap<>();

        putS(item, "tripId", t.getTripId());
        putS(item, "userId", t.getUserId());
        putS(item, "userName", t.getUserName());
        putS(item, "userPhone", t.getUserPhone());
        putS(item, "pickupLocation", t.getPickupLocation());
        putS(item, "dropLocation", t.getDropLocation());
        putS(item, "vehicleType", t.getVehicleType());

        putS(item, "driverName", t.getDriverName());
        putS(item, "driverPhone", t.getDriverPhone());
        putS(item, "driverCarType", t.getDriverCarType());
        putS(item, "driverCarNumber", t.getDriverCarNumber());

        if (t.getPassengers() != null && t.getPassengers() > 0) {
            item.put("passengers",
                AttributeValue.fromN(String.valueOf(t.getPassengers())));
        }

        if (t.getNumberOfDays() != null && t.getNumberOfDays() > 0) {
            item.put("numberOfDays",
                AttributeValue.fromN(String.valueOf(t.getNumberOfDays())));
        }

        if (t.getStatus() != null)
            item.put("status", AttributeValue.fromS(t.getStatus().name()));

        if (t.getCreatedAt() != null)
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

        return t;
    }
}
