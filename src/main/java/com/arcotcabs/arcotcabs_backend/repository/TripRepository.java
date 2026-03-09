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

    System.out.println("========= DEBUG TRIP OBJECT =========");

    System.out.println("userId        : " + trip.getUserId());
    System.out.println("userName      : " + trip.getUserName());
    System.out.println("userPhone     : " + trip.getUserPhone());
    System.out.println("pickupLocation: " + trip.getPickupLocation());
    System.out.println("dropLocation  : " + trip.getDropLocation());
    System.out.println("vehicleType   : " + trip.getVehicleType());
    System.out.println("passengers    : " + trip.getPassengers());
    System.out.println("numberOfDays  : " + trip.getNumberOfDays());

    System.out.println("=====================================");

    String tripId = UUID.randomUUID().toString();

    trip.setTripId(tripId);
    trip.setStatus(TripStatus.PENDING);
    trip.setCreatedAt(System.currentTimeMillis());

    Map<String, AttributeValue> item = new HashMap<>();

    putDebugString(item,"tripId",trip.getTripId());
    putDebugString(item,"userId",trip.getUserId());
    putDebugString(item,"userName",trip.getUserName());
    putDebugString(item,"userPhone",trip.getUserPhone());
    putDebugString(item,"pickupLocation",trip.getPickupLocation());
    putDebugString(item,"dropLocation",trip.getDropLocation());
    putDebugString(item,"vehicleType",trip.getVehicleType());

    if(trip.getPassengers()!=null){
        item.put("passengers",
                AttributeValue.builder().n(String.valueOf(trip.getPassengers())).build());
        System.out.println("passengers -> SAVED");
    }else{
        System.out.println("passengers -> EMPTY");
    }

    if(trip.getNumberOfDays()!=null){
        item.put("numberOfDays",
                AttributeValue.builder().n(String.valueOf(trip.getNumberOfDays())).build());
        System.out.println("numberOfDays -> SAVED");
    }else{
        System.out.println("numberOfDays -> EMPTY");
    }

    item.put("status",
            AttributeValue.builder().s(trip.getStatus().name()).build());

    item.put("createdAt",
            AttributeValue.builder().n(String.valueOf(trip.getCreatedAt())).build());

    System.out.println("========= FINAL DYNAMODB ITEM =========");
    System.out.println(item);
    System.out.println("=======================================");

    dynamoDb.putItem(
            PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build()
    );

    return tripId;
}
    private void putDebugString(Map<String, AttributeValue> item, String key, String value){

    if(value == null){
        System.out.println(key + " -> NULL");
        return;
    }

    if(value.trim().isEmpty()){
        System.out.println(key + " -> EMPTY STRING");
        return;
    }

    System.out.println(key + " -> SAVED (" + value + ")");

    item.put(key,
            AttributeValue.builder().s(value.trim()).build());
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

        return findAll()
                .stream()
                .filter(t -> userId.equals(t.getUserId()))
                .sorted((a,b)->Long.compare(b.getCreatedAt(),a.getCreatedAt()))
                .toList();
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

    /* ================= ASSIGN DRIVER ================= */

    public void assignDriver(Trip trip) {

        Map<String, AttributeValue> key =
                Map.of("tripId", AttributeValue.fromS(trip.getTripId()));

        Map<String, AttributeValue> values = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        List<String> updates = new ArrayList<>();

        addStringUpdate(updates,names,values,"driverId",trip.getDriverId());
        addStringUpdate(updates,names,values,"driverName",trip.getDriverName());
        addStringUpdate(updates,names,values,"driverPhone",trip.getDriverPhone());
        addStringUpdate(updates,names,values,"driverCarType",trip.getDriverCarType());
        addStringUpdate(updates,names,values,"driverCarNumber",trip.getDriverCarNumber());

        if(trip.getStatus()!=null){
            names.put("#status","status");
            values.put(":status",AttributeValue.fromS(trip.getStatus().name()));
            updates.add("#status = :status");
        }

        if(updates.isEmpty()) return;

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET " + String.join(", ",updates))
                .expressionAttributeNames(names)
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= START TRIP ================= */

    public void updateStartTrip(Trip trip){

        Map<String, AttributeValue> key =
                Map.of("tripId", AttributeValue.fromS(trip.getTripId()));

        Map<String, AttributeValue> values = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        List<String> updates = new ArrayList<>();

        addStringUpdate(updates,names,values,"startLocation",trip.getStartLocation());
        addStringUpdate(updates,names,values,"odometerImageUrl",trip.getOdometerImageUrl());
        addNumberUpdate(updates,names,values,"startKm",trip.getStartKm());
        addNumberUpdate(updates,names,values,"startTime",trip.getStartTime());

        if(trip.getStatus()!=null){
            names.put("#status","status");
            values.put(":status",AttributeValue.fromS(trip.getStatus().name()));
            updates.add("#status = :status");
        }

        if(updates.isEmpty()) return;

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET "+String.join(", ",updates))
                .expressionAttributeNames(names)
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= END TRIP ================= */

    public void updateEndTrip(Trip trip){

        Map<String, AttributeValue> key =
                Map.of("tripId", AttributeValue.fromS(trip.getTripId()));

        Map<String, AttributeValue> values = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        List<String> updates = new ArrayList<>();

        addStringUpdate(updates,names,values,"endLocation",trip.getEndLocation());
        addStringUpdate(updates,names,values,"endOdometerImageUrl",trip.getEndOdometerImageUrl());
        addStringUpdate(updates,names,values,"signatureUrl",trip.getSignatureUrl());
        addNumberUpdate(updates,names,values,"endKm",trip.getEndKm());
        addNumberUpdate(updates,names,values,"endTime",trip.getEndTime());

        if(trip.getStatus()!=null){
            names.put("#status","status");
            values.put(":status",AttributeValue.fromS(trip.getStatus().name()));
            updates.add("#status = :status");
        }

        if(updates.isEmpty()) return;

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET "+String.join(", ",updates))
                .expressionAttributeNames(names)
                .expressionAttributeValues(values)
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= UPDATE STATUS ================= */

    public void updateStatus(String tripId, TripStatus status){

        Map<String, AttributeValue> key =
                Map.of("tripId", AttributeValue.fromS(tripId));

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET #status = :status")
                .expressionAttributeNames(Map.of("#status","status"))
                .expressionAttributeValues(
                        Map.of(":status",AttributeValue.fromS(status.name()))
                )
                .build();

        dynamoDb.updateItem(request);
    }

    /* ================= SAFE HELPERS ================= */

    private void safePutS(Map<String, AttributeValue> item,String key,String value){

        if(value==null) return;

        String v = value.trim();

        if(v.isEmpty()) return;

        item.put(key,AttributeValue.fromS(v));
    }

    private void addStringUpdate(List<String> updates,Map<String,String> names,
                                 Map<String,AttributeValue> values,String field,String value){

        if(value==null || value.isBlank()) return;

        String nameKey = "#"+field;
        String valueKey = ":"+field;

        names.put(nameKey,field);
        values.put(valueKey,AttributeValue.fromS(value));
        updates.add(nameKey+" = "+valueKey);
    }

    private void addNumberUpdate(List<String> updates,Map<String,String> names,
                                 Map<String,AttributeValue> values,String field,Number value){

        if(value==null) return;

        String nameKey = "#"+field;
        String valueKey = ":"+field;

        names.put(nameKey,field);
        values.put(valueKey,AttributeValue.fromN(String.valueOf(value)));
        updates.add(nameKey+" = "+valueKey);
    }

    /* ================= ENTITY → DYNAMODB ================= */

    private Map<String, AttributeValue> toItem(Trip t){

        Map<String, AttributeValue> item = new HashMap<>();

        safePutS(item,"tripId",t.getTripId());
        safePutS(item,"userId",t.getUserId());
        safePutS(item,"userName",t.getUserName());
        safePutS(item,"userPhone",t.getUserPhone());

        safePutS(item,"pickupLocation",t.getPickupLocation());
        safePutS(item,"dropLocation",t.getDropLocation());
        safePutS(item,"vehicleType",t.getVehicleType());

        safePutS(item,"driverId",t.getDriverId());
        safePutS(item,"driverName",t.getDriverName());
        safePutS(item,"driverPhone",t.getDriverPhone());
        safePutS(item,"driverCarType",t.getDriverCarType());
        safePutS(item,"driverCarNumber",t.getDriverCarNumber());

        if(t.getPassengers()!=null)
            item.put("passengers",AttributeValue.fromN(String.valueOf(t.getPassengers())));

        if(t.getNumberOfDays()!=null)
            item.put("numberOfDays",AttributeValue.fromN(String.valueOf(t.getNumberOfDays())));

        if(t.getStatus()!=null)
            item.put("status",AttributeValue.fromS(t.getStatus().name()));

        if(t.getCreatedAt()!=null)
            item.put("createdAt",AttributeValue.fromN(String.valueOf(t.getCreatedAt())));

        return item;
    }

    /* ================= DYNAMODB → ENTITY ================= */

    private Trip fromItem(Map<String, AttributeValue> item){

        Trip t = new Trip();

        if(item.containsKey("tripId"))
            t.setTripId(item.get("tripId").s());

        if(item.containsKey("userId"))
            t.setUserId(item.get("userId").s());

        if(item.containsKey("userName"))
            t.setUserName(item.get("userName").s());

        if(item.containsKey("userPhone"))
            t.setUserPhone(item.get("userPhone").s());

        if(item.containsKey("pickupLocation"))
            t.setPickupLocation(item.get("pickupLocation").s());

        if(item.containsKey("dropLocation"))
            t.setDropLocation(item.get("dropLocation").s());

        if(item.containsKey("vehicleType"))
            t.setVehicleType(item.get("vehicleType").s());

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
