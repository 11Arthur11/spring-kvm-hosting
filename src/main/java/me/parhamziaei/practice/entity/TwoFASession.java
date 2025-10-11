package me.parhamziaei.practice.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TwoFASession implements Serializable {

    private Long userId;
    private String hashedCode;
    private int attempts;

}
