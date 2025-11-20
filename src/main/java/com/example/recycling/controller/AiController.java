package com.example.recycling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")  // 🔹 5500포트 HTML에서도 접근 허용

@RestController
public class AiController {

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/call-flask")
    public Map<String, Object> callFlask(@RequestBody Map<String, String> request) {
        String text = request.get("text");

        // Flask로 보낼 JSON 데이터 구성
        Map<String, String> body = new HashMap<>();
        body.put("text", text);

        // Flask 서버로 POST 요청
        String flaskUrl = "http://127.0.0.1:5000/predict";
        Map<String, Object> response = restTemplate.postForObject(flaskUrl, body, Map.class);

        System.out.println("Flask 응답: " + response);
        return response;
    }
}
