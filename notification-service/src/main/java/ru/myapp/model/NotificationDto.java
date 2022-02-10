package ru.myapp.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDto extends AbstractDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object val;
}