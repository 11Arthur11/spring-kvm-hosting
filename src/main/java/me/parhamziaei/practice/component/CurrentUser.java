package me.parhamziaei.practice.component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Getter
@Setter
public class CurrentUser {

    private Long id;
    private String email;
    private String fullName;

}
