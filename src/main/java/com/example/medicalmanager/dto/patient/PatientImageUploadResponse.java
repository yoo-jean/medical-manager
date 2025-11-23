package com.example.medicalmanager.dto.patient;

public record PatientImageUploadResponse(
        Long patientId,
        String imageUrl
) { }
