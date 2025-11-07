package me.parhamziaei.practice.dto.request.authenticate;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class TwoFactorRequest {

    private String code;

}
