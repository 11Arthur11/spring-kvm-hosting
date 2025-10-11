package me.parhamziaei.practice.dto.response;

import lombok.*;
import me.parhamziaei.practice.entity.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
