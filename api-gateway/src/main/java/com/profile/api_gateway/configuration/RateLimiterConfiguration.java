package com.profile.api_gateway.configuration;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterConfiguration.class);

    @Bean
    public KeyResolver keyResolver() {

        //use to get Client Information( IP address)
        return exchange -> {
            String ip = "unknown";
            ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            log.info("current ip is {}", ip);
            return Mono.just(ip);
        };
    }
}
