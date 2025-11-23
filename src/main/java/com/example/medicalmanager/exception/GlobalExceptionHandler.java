package com.example.medicalmanager.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        // 알 수 없는 서버 에러 처리
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode));
    }

    // 에러 응답 DTO 내부 클래스로 생성
    static class ErrorResponse {
        private final int status;
        private final String message;

        public ErrorResponse(ErrorCode errorCode) {
            this.status = errorCode.getStatus().value();
            this.message = errorCode.getMessage();
        }

        public int getStatus() { return status; }
        public String getMessage() { return message; }
    }
}
