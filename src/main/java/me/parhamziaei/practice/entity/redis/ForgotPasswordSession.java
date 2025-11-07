package me.parhamziaei.practice.entity.redis;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ForgotPasswordSession implements Serializable {

    private String userEmail;
    private String code;
    private int attempts;

}
