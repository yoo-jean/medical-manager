package com.example.medicalmanager.domain.patient;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "patients")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이름
    @Column(nullable = false)
    private String name;

    // 나이
    @Column(nullable = false)
    private Integer age;

    // 성별 (MALE / FEMALE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    // 질병 여부
    @Column(nullable = false)
    private Boolean hasDisease;

    // 이미지 파일 상대 경로
    private String imagePath;

    // 이미지 업로드 완료 여부 (조회 가능 여부)
    @Column(nullable = false)
    private Boolean imageUploaded = false;

    // 생성자
    private Patient(String name, Integer age, Gender gender, Boolean hasDisease) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.hasDisease = hasDisease;
        this.imageUploaded = false; // 초기값
    }

    // 정적 팩토리 메서드 (생성 전용)
    public static Patient create(String name, Integer age, Gender gender, Boolean hasDisease) {
        return new Patient(name, age, gender, hasDisease);
    }

    // 이미지 업로드 완료 처리
    public void completeImageUpload(String imagePath) {
        this.imagePath = imagePath;
        this.imageUploaded = true;
    }
}
