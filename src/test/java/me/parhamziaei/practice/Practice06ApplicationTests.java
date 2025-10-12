package me.parhamziaei.practice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Random;

@SpringBootTest
class Practice06ApplicationTests {

    @Test
    void contextLoads() {
        Random rand = new Random();
        System.out.printf(String.valueOf(rand.nextInt(10000)));
    }

}
