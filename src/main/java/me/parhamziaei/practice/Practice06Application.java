package me.parhamziaei.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class Practice06Application {

    public static void main(String[] args) {
        SpringApplication.run(Practice06Application.class, args);
    }

}
