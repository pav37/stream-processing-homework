package ru.myapp.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@RedisHash("Notification")
public class Notification implements Serializable {
  private Long id;
  private String username;
  private String email;
  private String title;
  private String body;
  private String description;
  private LocalDateTime dateTime;
  private String eventType;

}