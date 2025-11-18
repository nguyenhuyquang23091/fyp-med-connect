package com.fyp.profile_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String DOCTOR_PROFILE_CDC_TOPIC = "cdc.profileservice.doctor_profile_relationships";

    @Bean
    public NewTopic doctorProfileCdcTopic() {
        return TopicBuilder.name(DOCTOR_PROFILE_CDC_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
