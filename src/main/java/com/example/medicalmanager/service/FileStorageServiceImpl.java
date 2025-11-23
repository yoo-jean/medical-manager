package com.example.medicalmanager.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 루트 디렉토리 생성 실패: " + rootLocation, e);
        }
    }

    @Override
    public String storePatientImage(Long patientId, MultipartFile file) {
        try {
            Path patientDir = rootLocation.resolve("patients").resolve(String.valueOf(patientId));
            Files.createDirectories(patientDir);

            String originalFilename = file.getOriginalFilename();
            String ext = getExtension(originalFilename);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = timestamp + ext;

            Path target = patientDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "patients/" + patientId + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public Resource loadAsResource(String relativePath) {
        try {
            Path filePath = rootLocation.resolve(relativePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("파일을 찾을 수 없습니다: " + relativePath);
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로가 유효하지 않습니다: " + relativePath, e);
        }
    }

    @Override
    public void delete(String relativePath) {
        try {
            Path filePath = rootLocation.resolve(relativePath).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", relativePath, e);
        }
    }

    @Override
    public boolean isSupportedImage(String contentType, String originalFilename) {
        String ext = getExtension(originalFilename).toLowerCase();

        boolean extOk = ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg");
        boolean typeOk = contentType != null &&
                (contentType.equalsIgnoreCase("image/png")
                        || contentType.equalsIgnoreCase("image/jpg")
                        || contentType.equalsIgnoreCase("image/jpeg"));

        return extOk || typeOk;
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx == -1) return "";
        return filename.substring(dotIdx);
    }
}
