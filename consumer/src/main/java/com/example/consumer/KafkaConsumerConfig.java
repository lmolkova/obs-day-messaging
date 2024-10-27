package com.example.consumer;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
public class KafkaConsumerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value(value = "${BOOTSTRAP_SERVERS}")
    private String bootstrapAddress;

    @Value(value = "${CONSUMER_GROUP}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
          ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
          bootstrapAddress);
        props.put(
          ConsumerConfig.GROUP_ID_CONFIG,
          groupId);
        props.put(
          ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
          StringDeserializer.class);
        props.put(
          ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
          StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
      kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
          new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @KafkaListener(topics = "${TOPIC}", groupId = "${CONSUMER_GROUP}")
    public void processMessage(ConsumerRecord<?, ?> consumerRecord,
                               Acknowledgment acknowledgment) throws InterruptedException {

        int random = ThreadLocalRandom.current().nextInt(100);

        LOGGER.atInfo()
                .addKeyValue("key", consumerRecord.key())
                .addKeyValue("partition", consumerRecord.partition())
                .addKeyValue("topic", consumerRecord.topic())
                .addKeyValue("offset", consumerRecord.offset())
                .addKeyValue("message", consumerRecord.value())
                .log("Received message");

        Thread.sleep(random);
        if (random % 511 == 0) {
            LOGGER.atWarn().log("Simulating an error");
            nack(acknowledgment);
            throw new RuntimeException("Simulating an error");
        }

        ack(acknowledgment);
    }

    @WithSpan
    private void ack(Acknowledgment acknowledgment) {
        acknowledgment.acknowledge();
    }

    @WithSpan
    private void nack(Acknowledgment acknowledgment) {
        acknowledgment.nack(Duration.ofSeconds(1));
    }
}