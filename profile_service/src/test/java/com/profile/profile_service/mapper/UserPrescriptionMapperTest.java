package com.profile.profile_service.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.profile.profile_service.constant.PrescriptionStatus;
import com.profile.profile_service.dto.response.PrescriptionGeneralResponse;
import com.profile.profile_service.dto.response.PrescriptionResponse;
import com.profile.profile_service.entity.UserPrescription;

@SpringBootTest
public class UserPrescriptionMapperTest {

    @Autowired
    private UserPrescriptionMapper userPrescriptionMapper;

    @Test
    public void testMappingIncludesId() {
        // Create a UserPrescription entity with an ID (simulating saved entity)
        UserPrescription prescription = UserPrescription.builder()
                .id("test-prescription-id-123")
                .userId("user-123")
                .userProfileId("profile-123")
                .prescriptionName("Test Prescription")
                .doctorId("doctor-123")
                .status(PrescriptionStatus.PENDING_DOCTOR_REVIEW)
                .imageURLS(new ArrayList<>())
                .prescriptionData(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Test PrescriptionGeneralResponse mapping
        PrescriptionGeneralResponse generalResponse =
                userPrescriptionMapper.toUserPrescriptionGeneralResponse(prescription);

        assertNotNull(generalResponse);
        assertEquals("test-prescription-id-123", generalResponse.getId());
        assertEquals("user-123", generalResponse.getUserId());
        assertEquals("Test Prescription", generalResponse.getPrescriptionName());

        // Test PrescriptionResponse mapping
        PrescriptionResponse fullResponse = userPrescriptionMapper.toUserPrescriptionResponse(prescription);

        assertNotNull(fullResponse);
        assertEquals("test-prescription-id-123", fullResponse.getId());
        assertEquals("user-123", fullResponse.getUserId());
        assertEquals("Test Prescription", fullResponse.getPrescriptionName());

        System.out.println("✅ General Response ID: " + generalResponse.getId());
        System.out.println("✅ Full Response ID: " + fullResponse.getId());
    }
}
