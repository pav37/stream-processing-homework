package ru.myapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.modelmapper.internal.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.myapp.entity.Notification;
import ru.myapp.mapper.NotificationMapper;
import ru.myapp.model.AbstractDto;
import ru.myapp.model.CommandDto;
import ru.myapp.model.UserDto;
import ru.myapp.repository.NotificationRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@Service
@Slf4j
public class NotificationService {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private KafkaTemplate<Long, AbstractDto> kafkaUserDtoTemplate;

    @KafkaListener(id = "notification", topics = {"auth.user_created"}, containerFactory = "singleFactory")
    public void consume(Object dto) {
        ConsumerRecord record = (ConsumerRecord)dto;
        try {
            UserDto user = objectMapper.readValue(record.value().toString(), UserDto.class);
            log.info("=> consumed {}", writeValueAsString(user));
            Notification n = createNotification(record, user.getUsername(), user.getEmail());
            notificationRepository.save(n);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private Notification createNotification(ConsumerRecord record, String username, String email) {
        Notification n = new Notification();
        n.setBody(record.value().toString());
        String title = getTitle(record.topic());
        n.setTitle(title);
        n.setDescription(title + ": " + record.value().toString());
        n.setEventType(record.topic());
        n.setDateTime( LocalDateTime.ofInstant(Instant.ofEpochMilli(record.timestamp()),
                TimeZone.getDefault().toZoneId()));
        n.setEmail(email);
        n.setUsername(username);
        return n;
    }

    @KafkaListener(id = "notification11", containerFactory = "singleFactory",
            topics = {"command.value_set", "command.value_add",
                    "dm.value_set_approved", "dm.value_add_approved",
                    "dm.value_set_error", "dm.value_add_error"})
    public void consume1(Object dto) {
        ConsumerRecord record = (ConsumerRecord)dto;
        try {
            CommandDto command = objectMapper.readValue(record.value().toString(), CommandDto.class);
            log.info("=> consumed {}", writeValueAsString(command));
            Notification n = createNotification(record, command.getUsername(), command.getEmail());
            notificationRepository.save(n);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private String getTitle(String topic) {
        if (Arrays.asList("dm.value_set_approved", "dm.value_add_approved").contains(topic)) {
            return "Команда отправлена";
        } else if (Arrays.asList("command.value_set", "command.value_add").contains(topic)) {
            return "Команда обработана";
        } else if (Arrays.asList("dm.value_set_error", "dm.value_add_error").contains(topic)) {
            return "Ошибка при обработке команды";
        } else if (Arrays.asList("auth.user_created").contains(topic)) {
            return "Создан аккаунт";
        }
        return "";
    }

    public Iterable<Notification> getNotifications() {
        return notificationRepository.findAll();
    }

    private String writeValueAsString(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Writing value to JSON failed: " + dto.toString());
        }
    }
}
