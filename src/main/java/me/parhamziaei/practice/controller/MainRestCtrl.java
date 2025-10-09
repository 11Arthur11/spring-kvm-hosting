package me.parhamziaei.practice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainRestCtrl {

    @GetMapping("/404")
    public ResponseEntity<String> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT_FOUND");
    }

    @GetMapping("/favicon.ico")
    private void noFavicon() {
        System.out.println("favicon.ico is not configured!");
    }

    @GetMapping("/greeting")
    public ResponseEntity<String> greeting() {
        return ResponseEntity.ok("Hello From Backend");
    }

    @GetMapping("/goodbye")
    public ResponseEntity<String> goodbye() {
        return ResponseEntity.ok("Goodbye From Backend");
    }

}
