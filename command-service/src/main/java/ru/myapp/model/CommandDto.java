package ru.myapp.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class CommandDto {
    private String deviceId;
    private String sensorId;
    private String actionType;
    private String value;
    private String userId;
    private String username;
    private String email;
}
