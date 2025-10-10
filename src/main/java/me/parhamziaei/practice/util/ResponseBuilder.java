package me.parhamziaei.practice.util;

import me.parhamziaei.practice.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {

    private ResponseBuilder() {}

    public static ResponseEntity<Object> build(String code, String message, HttpStatus status) {
        ApiResponse<Void> response = new ApiResponse<>(false, code, message, null);
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<Object> build(String code, String message, HttpStatus status, T data) {
        ApiResponse<T> response = new ApiResponse<>(false, code, message, data);
        return ResponseEntity.status(status).body(response);
    }

}
