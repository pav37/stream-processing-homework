package ru.myapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import ru.myapp.entity.Device;
import ru.myapp.entity.Sensor;
import ru.myapp.model.CommandDto;
import ru.myapp.repository.DeviceGroupRepository;
import ru.myapp.repository.DeviceRepository;
import ru.myapp.repository.ParameterRepository;
import ru.myapp.repository.SensorRepository;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class DeviceService {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private SensorRepository sensorRepository;
    @Autowired
    private DeviceGroupRepository deviceGroupRepository;
    @Autowired
    private ParameterRepository parameterRepository;
    @Autowired
    private KafkaTemplate<Long, CommandDto> kafkaCommandDtoTemplate;

    public List<Device> getDevices() {
        return deviceRepository.findAll();
    }

    public List<Sensor> getSensors() {
        return sensorRepository.findAll();
    }

    public Device createDeviceWithSensor(UUID deviceId, UUID sensorId, String parameterName) {
        Device device = new Device();
        device.setId(deviceId);
        device.setName("Test Device " + new Random().nextInt(100));
        device.setIsTest(true);
        device.setDeviceGroup(deviceGroupRepository.findAll().stream()
                .filter(a -> a.getName().equalsIgnoreCase("test"))
                .findFirst().orElse(null));
        log.info(device.toString());
        deviceRepository.save(device);
        Sensor sensor = new Sensor();
        sensor.setDevice(device);
        sensor.setId(sensorId);
        sensor.setParameter(parameterRepository.findAll().stream()
                .filter(p -> p.getParameterName().equalsIgnoreCase(parameterName)).findFirst()
                .orElse(null));
        log.info(sensor.toString());
        sensorRepository.save(sensor);
        return deviceRepository.getById(deviceId);
    }

    @KafkaListener(id = "device-management-service1", topics = {"command.value_set"}, containerFactory = "singleFactory")
    public void consumeValueAdd(CommandDto dto) {
        log.info("=> consumed {}", writeValueAsString(dto));
        deviceRepository.findById(UUID.fromString(dto.getDeviceId()))
                .flatMap(a -> a.getSensors().stream()
                        .filter(s -> s.getId().equals(UUID.fromString(dto.getSensorId())))
                        .findFirst()).ifPresent(e -> {
                    Double value = Double.valueOf(dto.getValue());
                    if (value > e.getParameter().getValueMin() && value < e.getParameter().getValueMax()) {
                        e.setValue(value);
                        log.info("Saving: " + e);
                        sensorRepository.save(e);
                        sendCommandDtoEvent(dto, "dm.value_set_approved");
                    } else {
                        log.info("Incorrect parameter value: " + e);
                        sendCommandDtoEvent(dto, "dm.value_set_error");
                    }
                });
    }

    @KafkaListener(id = "device-management-service2", topics = {"command.value_add"}, containerFactory = "singleFactory")
    public void consumeValueSet(CommandDto dto) {
        log.info("=> consumed {}", writeValueAsString(dto));
        deviceRepository.findById(UUID.fromString(dto.getDeviceId()))
                .flatMap(a -> a.getSensors().stream()
                        .filter(s -> s.getId().equals(UUID.fromString(dto.getSensorId())))
                        .findFirst()).ifPresent(e -> {
                    Double value = e.getValue() + Double.parseDouble(dto.getValue());
                    if (value > e.getParameter().getValueMin() && value < e.getParameter().getValueMax()) {
                        e.setValue(value);
                        log.info("Saving: " + e);
                        sensorRepository.save(e);
                        sendCommandDtoEvent(dto, "dm.value_add_approved");
                    } else {
                        log.info("Incorrect parameter value: " + e);
                        sendCommandDtoEvent(dto, "dm.value_add_error");
                    }
                });
    }

    public void sendCommandDtoEvent(CommandDto dto, String topic) {
        log.info("<= sending {}", writeValueAsString(dto));
        ListenableFuture<SendResult<Long, CommandDto>> result = kafkaCommandDtoTemplate.send(topic, dto);
        result.addCallback(new ListenableFutureCallback<SendResult<Long, CommandDto>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.info("Send CommandDto event error", ex);
            }

            @Override
            public void onSuccess(SendResult<Long, CommandDto> result) {
                log.info("result: " + result.toString());
            }
        });
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
