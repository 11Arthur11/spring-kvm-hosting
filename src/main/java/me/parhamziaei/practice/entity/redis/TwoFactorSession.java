package me.parhamziaei.practice.entity.redis;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class TwoFactorSession implements Serializable {

    private String userEmail;
    private String code;
    private int attempts;
    private boolean rememberMe;
    private boolean verified;

    public TwoFactorSession(String userEmail, String hashedCode) {
        this.userEmail = userEmail;
        this.code = hashedCode;
        this.attempts = 0;
    }

}
