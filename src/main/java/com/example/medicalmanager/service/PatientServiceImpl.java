package com.example.medicalmanager.service;

import com.example.medicalmanager.domain.patient.Patient;
import com.example.medicalmanager.domain.patient.PatientRepository;
import com.example.medicalmanager.dto.patient.PatientBasicResponse;
import com.example.medicalmanager.dto.patient.PatientCreateRequest;
import com.example.medicalmanager.dto.patient.PatientImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.example.medicalmanager.exception.ApiException;
import com.example.medicalmanager.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final FileStorageService fileStorageService;

    @Override
    public Long createPatient(PatientCreateRequest request) {
        if (request.name() == null || request.name().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Patient patient = Patient.create(
                request.name(),
                request.age(),
                request.gender(),
                request.hasDisease()
        );

        return patientRepository.save(patient).getId();
    }

    @Override
    public PatientImageUploadResponse uploadPatientImage(Long patientId, MultipartFile file) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(ErrorCode.PATIENT_NOT_FOUND));

        if (patient.getImageUploaded()) {
            throw new ApiException(ErrorCode.IMAGE_ALREADY_UPLOADED);
        }

        // 확장자 검사
        if (!fileStorageService.isSupportedImage(file.getContentType(), file.getOriginalFilename())) {
            throw new ApiException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }

        // 저장
        String relativePath = fileStorageService.storePatientImage(patientId, file);
        patient.completeImageUpload(relativePath);
        patientRepository.save(patient);

        String imageUrl = buildImageUrl(patientId);

        return new PatientImageUploadResponse(patientId, imageUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientBasicResponse getPatient(Long patientId) {
        Patient patient = patientRepository.findByIdAndImageUploadedTrue(patientId)
                .orElseThrow(() -> new ApiException(ErrorCode.IMAGE_NOT_FOUND));

        String imageUrl = buildImageUrl(patient.getId());

        return new PatientBasicResponse(
                patient.getId(),
                patient.getName(),
                patient.getAge(),
                patient.getGender(),
                patient.getHasDisease(),
                imageUrl
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getPatientImage(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(ErrorCode.PATIENT_NOT_FOUND));

        if (patient.getImagePath() == null) {
            throw new ApiException(ErrorCode.IMAGE_NOT_FOUND);
        }

        return fileStorageService.loadAsResource(patient.getImagePath());
    }

    @Override
    public void deletePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(ErrorCode.PATIENT_NOT_FOUND));

        if (patient.getImagePath() != null) {
            fileStorageService.delete(patient.getImagePath());
        }

        patientRepository.delete(patient);
    }

    private String buildImageUrl(Long patientId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/patients/")
                .path(String.valueOf(patientId))
                .path("/image")
                .toUriString();
    }
}
