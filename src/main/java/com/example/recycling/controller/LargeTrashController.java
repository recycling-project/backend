package com.example.recycling.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/trash/large")
public class LargeTrashController {

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeLargeTrash(@RequestBody Map<String, String> request) {
        String base64Image = request.get("image");

        // 딥러닝 모델 연결 전 테스트용
        return ResponseEntity.ok(
                Map.of("status", "success", "type", "large-trash", "imageLength", base64Image.length())
        );
    }
}
