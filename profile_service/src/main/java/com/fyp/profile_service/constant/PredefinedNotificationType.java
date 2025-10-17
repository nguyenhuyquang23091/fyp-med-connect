package com.fyp.profile_service.constant;

public class PredefinedNotificationType {
    public static final String ACCESS_REQUEST = "ACCESS_REQUEST";
    public static final String ACCESS_APPROVED = "ACCESS_APPROVED";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String ACCESS_EXPIRED = "ACCESS_EXPIRED";
    public static final String PRESCRIPTION_UPDATED = "PRESCRIPTION_UPDATED";
    public static final String PRESCRIPTION_DELETED = "PRESCRIPTION_DELETED";
    public static final String APPOINTMENT_REMINDER = "APPOINTMENT_REMINDER";
    public static final String REQUEST_STATUS_CHANGED = "REQUEST_STATUS_CHANGED";

    private PredefinedNotificationType() {
        // Prevent instantiation
    }
}
