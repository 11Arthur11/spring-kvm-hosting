package me.parhamziaei.practice.util;

import me.parhamziaei.practice.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {

    private ResponseBuilder() {}

    public static ResponseEntity<Object> buildFailed(String type, String message, HttpStatus status) {
        ApiResponse<Void> response = new ApiResponse<>(false, type, message, null);
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<Object> buildFailed(String type, String message, T data, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>(false, type, message, data);
        return ResponseEntity.status(status).body(response);
    }

    public static ResponseEntity<Object> buildSuccess(String type, String message, HttpStatus status) {
        ApiResponse<Void> response = new ApiResponse<>(true, type, message, null);
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<Object> buildSuccess(String type, String message, T data, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>(true, type, message, data);
        return ResponseEntity.status(status).body(response);
    }

}
