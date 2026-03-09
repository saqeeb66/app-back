package com.arcotcabs.arcotcabs_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverResponse {
    private String driverId;
    private String name;
    private String email;
    private String phone;
    private String carType;
    private String carNumber;
}
