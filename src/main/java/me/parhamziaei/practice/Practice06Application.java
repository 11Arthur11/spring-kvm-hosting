package me.parhamziaei.practice;

import me.parhamziaei.practice.configuration.properties.ImageStorageProperties;
import me.parhamziaei.practice.configuration.properties.RuntimeInitProperties;
import me.parhamziaei.practice.configuration.properties.SecurityProperties;
import me.parhamziaei.practice.configuration.properties.TicketServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({
        ImageStorageProperties.class,
        TicketServiceProperties.class,
        RuntimeInitProperties.class,
        SecurityProperties.class
})
public class Practice06Application {

    public static void main(String[] args) {
        SpringApplication.run(Practice06Application.class, args);
    }

}
