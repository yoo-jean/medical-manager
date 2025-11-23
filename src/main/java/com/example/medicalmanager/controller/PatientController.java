package com.example.medicalmanager.controller;

import com.example.medicalmanager.dto.patient.PatientBasicResponse;
import com.example.medicalmanager.dto.patient.PatientCreateRequest;
import com.example.medicalmanager.dto.patient.PatientImageUploadResponse;
import com.example.medicalmanager.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * 1단계 - 환자 기본 정보 저장
     * POST /api/patients
     */
    @PostMapping
    @Operation(summary = "환자 기본 정보 저장")
    public ResponseEntity<Long> createPatient(@RequestBody PatientCreateRequest request) {
        Long patientId = patientService.createPatient(request);
        return ResponseEntity.ok(patientId);
    }

    /**
     * 2단계 - 환자 이미지 업로드
     * POST /api/patients/{id}/image
     * multipart/form-data
     */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "환자 이미지 업로드")
    public ResponseEntity<PatientImageUploadResponse> uploadImage(
            @PathVariable("id") Long patientId,
            @RequestPart("file") MultipartFile file
    ) {
        PatientImageUploadResponse response = patientService.uploadPatientImage(patientId, file);
        return ResponseEntity.ok(response);
    }

    /**
     * 환자 기본 정보 조회
     * GET /api/patients/{id}
     * (imageUploaded == true 인 경우에만 조회 가능)
     */
    @GetMapping("/{id}")
    @Operation(summary = "환자 기본 정보 조회")
    public ResponseEntity<PatientBasicResponse> getPatient(@PathVariable("id") Long patientId) {
        PatientBasicResponse response = patientService.getPatient(patientId);
        return ResponseEntity.ok(response);
    }

    /**
     * 환자 이미지 조회 (브라우저에서 URL로 바로 이미지 보여주기 용도)
     * GET /api/patients/{id}/image
     */
    @GetMapping("/{id}/image")
    @Operation(summary = "환자 이미지 조회")
    public ResponseEntity<Resource> getPatientImage(@PathVariable("id") Long patientId) {
        Resource imageResource = patientService.getPatientImage(patientId);

        // 간단하게: contentType은 일단 IMAGE_JPEG로 두고,
        // 나중에 실제 확장자/타입에 맞게 변경해도 됨.
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageResource);
    }

    /**
     * 환자 데이터 삭제 (DB + 이미지 파일)
     * DELETE /api/patients/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "환자 데이터 삭제")
    public ResponseEntity<Void> deletePatient(@PathVariable("id") Long patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
