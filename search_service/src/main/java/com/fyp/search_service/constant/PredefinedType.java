package com.fyp.search_service.constant;

import java.util.List;

public class PredefinedType {

    //CDC EVENT Entity Type
   public final static String SERVICE = "SERVICE";
    public final static String SPECIALTY = "SPECIALTY";
    public final static String EXPERIENCE = "EXPERIENCE";
    public final static String PROFILE = "PROFILE";

    //display name ( for logging purpose, used in DoctorSearchService)
    public static final String DISPLAY_SERVICE = "Service";
    public static final String DISPLAY_SPECIALTY = "Specialty";
    public static final String DISPLAY_EXPERIENCE = "Experience";
    public static final String DISPLAY_PROFILE = "Profile";

    public static final List<String> DEFAULT_SEARCH_FIELDS =
            List.of(
                    "residency", "specialtyName", "hospitalName", "serviceName", "fullName"
 );


    private PredefinedType(){

    }
}
