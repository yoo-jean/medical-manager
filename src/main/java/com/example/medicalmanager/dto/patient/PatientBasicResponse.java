package com.example.medicalmanager.dto.patient;

import com.example.medicalmanager.domain.patient.Gender;

public record PatientBasicResponse(
        Long id,
        String name,
        Integer age,
        Gender gender,
        Boolean hasDisease,
        String imageUrl
) { }
