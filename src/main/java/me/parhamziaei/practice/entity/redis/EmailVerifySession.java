package me.parhamziaei.practice.entity.redis;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class EmailVerifySession {

    String userEmail;
    String code;
    int attempts;

}
