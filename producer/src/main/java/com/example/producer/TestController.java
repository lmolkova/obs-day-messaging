package com.example.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(value = "/orders")
public class TestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    @Value(value = "${TOPIC}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping
    public String greeting(@RequestParam("message") String message)  {
        kafkaTemplate.send(topic, message).whenComplete((result, ex) -> {
            if (ex == null) {
                LOGGER.atInfo()
                        .addKeyValue("message", message)
                        .addKeyValue("topic", topic)
                        .addKeyValue("partition", result.getRecordMetadata().partition())
                        .addKeyValue("offset", result.getRecordMetadata().offset())
                        .log("Message sent successfully");
            } else {
                LOGGER.atError()
                        .addKeyValue("message", message)
                        .addKeyValue("topic", topic)
                        .addKeyValue("partition", result.getRecordMetadata().partition())
                        .addKeyValue("offset", result.getRecordMetadata().offset())
                        .log("Unable to send message", ex);
            }
        });
        return message;
    }
}