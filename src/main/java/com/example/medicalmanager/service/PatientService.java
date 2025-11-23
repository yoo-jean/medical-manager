package com.example.medicalmanager.service;


import com.example.medicalmanager.dto.patient.PatientBasicResponse;
import com.example.medicalmanager.dto.patient.PatientCreateRequest;
import com.example.medicalmanager.dto.patient.PatientImageUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface PatientService {

    Long createPatient(PatientCreateRequest request);

    PatientImageUploadResponse uploadPatientImage(Long patientId, MultipartFile file);

    PatientBasicResponse getPatient(Long patientId);

    Resource getPatientImage(Long patientId);

    void deletePatient(Long patientId);
}
