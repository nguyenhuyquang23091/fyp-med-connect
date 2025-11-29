package com.fyp.statistic_service.tool;


import com.fyp.statistic_service.dto.response.DashboardStatisticsResponse;
import com.fyp.statistic_service.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BusinessAnalystTool {

    DashboardService dashboardService;


    @Tool(description = """
        Retrieve a comprehensive dashboard summary combining statistics from all system services: 
        user registrations, appointment bookings, and payment transactions. Use this tool when 
        the user requests overall system metrics, cross-domain analytics, or a complete business 
        overview.
        
        This tool provides:
        - User metrics: total users (all-time), weekly active users, monthly active users, 
          new users (today/this week/this month)
        - Appointment metrics: total appointments (today/this week/this month), completed appointments,
          cancelled appointments, pending appointments, average cancellation rate
        - Payment metrics: revenue (today/this week/this month), transaction counts (today/this week),
          average transaction amount
        
        Best for queries like:
        - "How is the business doing?"
        - "Show me the dashboard"
        - "What are our key metrics?"
        - "Give me a system overview"
        - "How many users do we have?"
        - "What's our revenue this month?"
        - "Show me appointment statistics"
        - "How many new users signed up?"
        
        Returns aggregated statistics with breakdowns by time period (today, this week, this month)
        across all business domains (users, appointments, payments).
        """)
    public DashboardStatisticsResponse getComprehensiveDashboard() {
        log.info("BusinessAnalystTool invoked: getComprehensiveDashboard");
        
        DashboardStatisticsResponse response = dashboardService.getDashboardSummaryInternal();
        
        log.debug("Dashboard generated: {} total users, {} appointments this month, ${} revenue this month", 
                  response.getUserMetrics().getTotalUsers(),
                  response.getAppointmentMetrics().getTotalAppointmentsThisMonth(),
                  response.getPaymentMetrics().getRevenueThisMonth());
        
        return response;
    }
}
