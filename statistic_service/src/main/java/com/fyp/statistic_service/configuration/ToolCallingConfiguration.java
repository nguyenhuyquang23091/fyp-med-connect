package com.fyp.statistic_service.configuration;


import com.fyp.statistic_service.service.DashboardService;

import com.fyp.statistic_service.tool.BusinessAnalystTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolCallingConfiguration {
    @Bean
    public BusinessAnalystTool testTool(DashboardService dashboardService){
        return new BusinessAnalystTool(dashboardService );
    }
}
