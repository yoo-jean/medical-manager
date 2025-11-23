package com.example.medicalmanager.domain.patient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // 이미지 업로드까지 완료된 환자만 단건 조회
    Optional<Patient> findByIdAndImageUploadedTrue(Long id);

    // 이미지 업로드까지 완료된 환자 목록 조회 (선택)
    List<Patient> findAllByImageUploadedTrue();
}
