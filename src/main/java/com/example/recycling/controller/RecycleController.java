package com.example.recycling.controller;

import com.example.recycling.store.SessionStore;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
@RequestMapping("/recycle")
public class RecycleController {

    private final String OPENAI_API_KEY;
    private final SessionStore sessionStore;

    public RecycleController(SessionStore sessionStore) {

        // 🔥 Railway 환경 변수에서 GPT_API_KEY 가져오기
        this.OPENAI_API_KEY = System.getenv("GPT_API_KEY");

        this.sessionStore = sessionStore;
    }

    private final String GPT_URL = "https://api.openai.com/v1/chat/completions";

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeRecycle(@RequestBody Map<String, String> request) {

        String base64Image = request.get("image");
        String text = request.get("text");

        System.out.println("🔥 입력 텍스트 = " + text);
        System.out.println("🔥 입력 이미지 prefix = " +
                (base64Image != null ? base64Image.substring(0, 30) : "null"));

        List<Object> contentList = new ArrayList<>();

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text",
                "너는 한국 기준 분리배출/재활용 전문가다.\n" +
                        "아래 내용을 분석해 한국 기준으로만 답해라.\n\n" +
                        "문의한 내용: '" + (text != null ? text : "") + "'"
        );
        contentList.add(textPart);

        if (base64Image != null && !base64Image.isEmpty()) {

            String prefix = "data:image/jpeg;base64";
            String pureBase64 = base64Image;

            if (base64Image.contains(",")) {
                prefix = base64Image.substring(0, base64Image.indexOf(","));
                pureBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
            }

            Map<String, Object> imagePart = new HashMap<>();
            imagePart.put("type", "image_url");

            Map<String, Object> imageUrl = new HashMap<>();
            imageUrl.put("url", prefix + "," + pureBase64);

            imagePart.put("image_url", imageUrl);
            contentList.add(imagePart);
        }

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", contentList);

        List<Object> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4o");
        payload.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + OPENAI_API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            RestTemplate rt = new RestTemplate();
            String gptResponse = rt.postForObject(GPT_URL, entity, String.class);

            System.out.println("🔥 GPT 응답 = " + gptResponse);
            return ResponseEntity.ok(gptResponse);

        } catch (HttpClientErrorException | HttpServerErrorException e) {

            System.out.println("🔥 GPT Error Status = " + e.getStatusCode());
            System.out.println("🔥 GPT Error Body = " + e.getResponseBodyAsString());

            return ResponseEntity
                    .status(e.getStatusCode().value())
                    .body(e.getResponseBodyAsString());
        }
    }
}
