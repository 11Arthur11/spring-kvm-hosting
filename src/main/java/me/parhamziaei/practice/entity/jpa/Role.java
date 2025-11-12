package me.parhamziaei.practice.entity.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, name = "role_name")
    private String name;

    @Column(nullable = false, name = "role_hierarchy")
    private Integer hierarchy;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    public Role(String name, Integer hierarchy) {
        this.name = name;
        this.hierarchy = hierarchy;
    }

}
