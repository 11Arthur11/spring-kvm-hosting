package me.parhamziaei.practice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {

    private String email;
    private String fullName;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private List<String> roles;
    private boolean locked;

}
