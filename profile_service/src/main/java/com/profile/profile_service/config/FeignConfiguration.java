package com.profile.profile_service.config;


import feign.form.spring.SpringFormEncoder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.codec.Encoder;


@Configuration
public class FeignConfiguration {
    @Bean
    public Encoder multilpartFileEncoder(){
        return new SpringFormEncoder();
    }
}
