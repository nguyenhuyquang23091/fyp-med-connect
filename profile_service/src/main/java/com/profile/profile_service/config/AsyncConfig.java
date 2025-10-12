package com.profile.profile_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class AsyncConfig {

    @Bean(name = "fileManagementExecutor")
    public ThreadPoolTaskExecutor fileManagementExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setThreadNamePrefix("file-mgmt-");
        taskExecutor.setTaskDecorator(new ThreadLocalCleanUpDecorator());

        taskExecutor.initialize();
        return taskExecutor;
    }

    static class ThreadLocalCleanUpDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            return () -> {
                try {
                    runnable.run();
                } finally {
                    AuthenticationInterceptor.clearAuthToken();
                    log.debug(
                            "Cleaned up ThreadLocal for thread: {}",
                            Thread.currentThread().getName());
                }
            };
        }
    }
}
