package ru.myapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import ru.myapp.model.CommandDto;

@Service
@Slf4j
public class CommandService {

    @Autowired
    private KafkaTemplate<Long, CommandDto> kafkaCommandDtoTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    private String writeValueAsString(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Writing value to JSON failed: " + dto.toString());
        }
    }

    public void setValue(CommandDto dto) {
        log.info("<= sending {}", writeValueAsString(dto));
        ListenableFuture<SendResult<Long, CommandDto>> result = kafkaCommandDtoTemplate.send("command.value_set", dto);
        result.addCallback(new ListenableFutureCallback<SendResult<Long, CommandDto>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.info("Exception occurred while sending event value_set", ex);
            }

            @Override
            public void onSuccess(SendResult<Long, CommandDto> result) {
                log.info("result: " + result.toString());
            }
        });
    }

    public void addValue(CommandDto dto) {
        log.info("<= sending {}", writeValueAsString(dto));
        ListenableFuture<SendResult<Long, CommandDto>> result = kafkaCommandDtoTemplate.send("command.value_add", dto);
        result.addCallback(new ListenableFutureCallback<SendResult<Long, CommandDto>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.info("Exception occurred while sending event value_add", ex);
            }

            @Override
            public void onSuccess(SendResult<Long, CommandDto> result) {
                log.info("result: " + result.toString());
            }
        });
    }
}
