package me.parhamziaei.practice.dto.request;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class TwoFactorRequest {

    private String code;

}
