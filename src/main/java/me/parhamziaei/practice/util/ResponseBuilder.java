package me.parhamziaei.practice.util;

import me.parhamziaei.practice.dto.response.DataResponse;
import me.parhamziaei.practice.dto.response.SimpleResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseBuilder {

    private ResponseBuilder() {}

    public static ResponseEntity<Object> buildFailed(String type, String message, HttpStatus status) {
        SimpleResponse response = new SimpleResponse(false, type, message);
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<Object> buildFailed(String type, String message, T data, HttpStatus status) {
        DataResponse<T> response = new DataResponse<>(false, type, message, data);
        return ResponseEntity.status(status).body(response);
    }

    public static ResponseEntity<Object> buildSuccess(String type, String message, HttpStatus status) {
        SimpleResponse response = new SimpleResponse(true, type, message);
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<Object> buildSuccess(String type, String message, T data, HttpStatus status) {
        DataResponse<T> response = new DataResponse<>(true, type, message, data);
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<Object> buildSuccess(String type, T data, HttpStatus status) {
        DataResponse<T> response = new DataResponse<>(true, type, null, data);
        return ResponseEntity.status(status).body(response);
    }

}
