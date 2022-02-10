package ru.myapp.model;

import lombok.Data;

@Data
public class CommandDto {
    private String deviceId;
    private String sensorId;
    private String actionType;
    private String value;
    private String userId;
    private String username;
    private String email;
}
