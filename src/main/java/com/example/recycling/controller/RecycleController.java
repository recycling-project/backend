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
        System.out.println("🔥 입력 이미지 prefix = " + (base64Image != null ? base64Image.substring(0, 30) : "null"));

        List<Object> contentList = new ArrayList<>();

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text",
                "너는 한국 기준 분리배출/재활용 전문가다.\n" +
                        "아래 내용을 분석해 한국 기준으로만 답해라.\n" +
                        "영어 문장, 불확실한 멘트, 사족, 서론은 절대 사용하지 말고 분리배출 내용만 간결하게 답하라.\n\n" +

                        "문의한 내용: '" + (text != null ? text : "") + "'\n\n" +

                        "① 요약\n" +
                        "품목: [이미지 기반 폐기물 설명]\n" +
                        "재활용: [가능 / 불가능 / 조건부 가능]\n" +
                        "위험물: [예 / 아니오]\n\n" +
                        "대형 폐기물일 경우: 일반 생활폐기물이 아니므로 지자체 대형폐기물 스티커 발급 후 배출해야 함.\n\n" +

                        "② 이유\n" +
                        "재질: [PET / PP / PE / 금속 / 유리 / 종이 / 비닐 / 복합재질 등]\n" +
                        "오염: [깨끗함 / 오염 / 세척 필요 등]\n" +
                        "구조·특징: [라미네이트, 은박층, 접합선 등]\n\n" +

                        "③ 분리배출 방법\n" +
                        "[1단계: 내용물 비우기]\n" +
                        "[2단계: 라벨·부속물 제거]\n" +
                        "[3단계: 깨끗하게 헹군 후 배출]\n" +
                        "[필요 시 추가 단계 포함]\n\n" +

                        "④ 주의사항\n" +
                        "지자체 규정에 따라 배출 방식이 달라질 수 있음.\n" +
                        "위험물 의심 시 즉시 밀봉 후 지자체 안내에 따름.\n" +
                        "위 형식 외 문장은 출력하지 말 것."
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

            return ResponseEntity.status(e.getStatusCode().value()).body(e.getResponseBodyAsString());
        }
    }

    // ============================================================
    // 🔥 ❷ 모바일 업로드 API (핸드폰 → 사진 업로드)
    // ============================================================
    @PostMapping("/mobile-upload")
    public ResponseEntity<?> uploadMobile(@RequestBody Map<String, String> body) {

        String base64 = body.get("image");
        if (base64 == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 없음"));
        }

        String id = UUID.randomUUID().toString();  // 업로드 고유 ID 생성

        sessionStore.saveImage(id, base64);        // 이미지 저장
        sessionStore.setLastUploadedId(id);        // 최근 업로드 ID 저장

        return ResponseEntity.ok(Map.of("id", id));
    }


    // ============================================================
    // 🔥 ❸ wait 페이지에서 호출하는 API (가장 최근 업로드된 id)
    // ============================================================
    @GetMapping("/check")
    public ResponseEntity<?> checkLastUpload() {
        String lastId = sessionStore.getLastUploadedId();
        return ResponseEntity.ok(Map.of("id", lastId));
    }


    // ============================================================
    // 🔥 ❹ analyze에서 해당 id의 이미지 내용 조회
    // ============================================================
    @GetMapping("/image")
    public ResponseEntity<?> getImage(@RequestParam String id) {
        String base64 = sessionStore.getImage(id);

        if (base64 == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 없음"));
        }

        return ResponseEntity.ok(Map.of("image", base64));
    }

    // ============================================================
    // 🔥 QR 페이지 시작할 때 이전 업로드 기록 초기화
    // ============================================================
    @PostMapping("/reset")
    public ResponseEntity<?> resetUpload() {
        sessionStore.resetAll();
        return ResponseEntity.ok(Map.of("status", "reset_ok"));
    }


    //============사용완료 후 삭제코드 ======
    @PostMapping("/cleanup")
    public ResponseEntity<?> cleanup(@RequestBody Map<String, String> body) {
        String id = body.get("id");

        sessionStore.remove(id);
        sessionStore.deleteImage(id);

        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}