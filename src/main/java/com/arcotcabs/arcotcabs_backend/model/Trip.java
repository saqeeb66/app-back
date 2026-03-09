package com.arcotcabs.arcotcabs_backend.model;

import com.arcotcabs.arcotcabs_backend.model.enums.TripStatus;

public class Trip {

    /* ================= IDENTIFIERS ================= */
    private String tripId;

    /* ================= USER INFO ================= */
    private String userId;
    private String userName;
    private String userPhone;

    /* ================= BOOKING (PLANNED) ================= */
    private String pickupLocation;
    private String dropLocation;
    private String vehicleType;
    private String tripNotes;

    private String travelDate;
    private Integer passengers = 1;
    private Integer numberOfDays = 1;


    /* ================= STATUS ================= */
    private TripStatus status;

    /* ================= DRIVER INFO ================= */
    private String driverId;
    private String driverName;
    private String driverPhone;
    private String driverCarType;
    private String driverCarNumber;

    /* ================= TRIP EXECUTION (ACTUAL) ================= */
    private String startLocation;
    private Double startKm;
    private Long startTime;

    private String endLocation;
    private Double endKm;
    private Long endTime;

    /* ================= DRIVER ACTIONS ================= */
    private String signatureUrl;

    /* ================= DRIVER PROOF ================= */
    private String odometerImageUrl;
    private String endOdometerImageUrl;   // end odometer



    /* ================= ADMIN ================= */
    private String adminComment;

    /* ================= AUDIT ================= */
    private Long createdAt;

    /* ================= GETTERS & SETTERS ================= */

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(String dropLocation) {
        this.dropLocation = dropLocation;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getTripNotes() {
        return tripNotes;
    }

    public void setTripNotes(String tripNotes) {
        this.tripNotes = tripNotes;
    }

    public String getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(String travelDate) {
        this.travelDate = travelDate;
    }



    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getDriverCarType() {
        return driverCarType;
    }

    public void setDriverCarType(String driverCarType) {
        this.driverCarType = driverCarType;
    }

    public String getDriverCarNumber() {
        return driverCarNumber;
    }

    public void setDriverCarNumber(String driverCarNumber) {
        this.driverCarNumber = driverCarNumber;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public Double getStartKm() {
        return startKm;
    }

    public void setStartKm(Double startKm) {
        this.startKm = startKm;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public Double getEndKm() {
        return endKm;
    }

    public void setEndKm(Double endKm) {
        this.endKm = endKm;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getSignatureUrl() {
        return signatureUrl;
    }

    public void setSignatureUrl(String signatureUrl) {
        this.signatureUrl = signatureUrl;
    }

    public String getOdometerImageUrl() {
        return odometerImageUrl;
    }

    public void setOdometerImageUrl(String odometerImageUrl) {
        this.odometerImageUrl = odometerImageUrl;
    }


    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getEndOdometerImageUrl() {
        return endOdometerImageUrl;
    }

    public void setEndOdometerImageUrl(String endOdometerImageUrl) {
        this.endOdometerImageUrl = endOdometerImageUrl;
    }

    public Integer getPassengers() {
        return passengers;
    }

    public void setPassengers(Integer passengers) {
        this.passengers = passengers;
    }

    public Integer getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }
}
