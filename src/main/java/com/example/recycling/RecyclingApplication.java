package com.example.recycling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RecyclingApplication {

    public static void main(String[] args) {

        // 🔥 환경 변수 확인 로그
        System.out.println("---- GPT KEY ----");
        System.out.println(System.getenv("GPT_API_KEY"));

        SpringApplication.run(RecyclingApplication.class, args);
    }
}
