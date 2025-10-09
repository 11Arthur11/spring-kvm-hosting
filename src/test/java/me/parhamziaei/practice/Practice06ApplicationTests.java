package me.parhamziaei.practice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class Practice06ApplicationTests {

    @Test
    void contextLoads() {
        Date date = new Date(System.currentTimeMillis());
        Date date2 = new Date(System.currentTimeMillis() + 1000 * 60 * 24);
        System.out.println(System.currentTimeMillis() + " - " + date + " - " + date2);
    }

}
