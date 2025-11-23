package com.example.medicalmanager.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storePatientImage(Long patientId, MultipartFile file);

    Resource loadAsResource(String relativePath);

    void delete(String relativePath);

    boolean isSupportedImage(String contentType, String originalFilename);
}
