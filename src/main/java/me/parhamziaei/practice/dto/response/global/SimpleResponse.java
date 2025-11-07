package me.parhamziaei.practice.dto.response.global;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleResponse {

    private boolean success;
    private String type;
    private String message;

}
