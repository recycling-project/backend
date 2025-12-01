//YOLO 분석 컨트롤러 대형

package com.example.recycling.controller;

import org.springframework.http.HttpEntity;          // HTTP 요청(Body + Header) 한 번에 담는 객체
import org.springframework.http.HttpHeaders;        // HTTP Header 설정용 (Content-Type 등)
import org.springframework.http.MediaType;          // JSON, MULTIPART 형식 지정하는 enum
import org.springframework.http.ResponseEntity;     // 컨트롤러에서 응답 반환할 때 사용하는 객체

import org.springframework.web.bind.annotation.*;   // @RestController, @PostMapping 등

import org.springframework.web.client.RestTemplate; // 다른 서버(FastAPI)로 요청 보내는 Spring HTTP 클라이언트

import org.springframework.core.io.ByteArrayResource; // byte[]를 Multipart 파일처럼 전송하게 해주는 Spring 객체
import org.springframework.util.LinkedMultiValueMap; // Multipart 요청의 Form-data 구조 구현체
import org.springframework.util.MultiValueMap;       // 키-값 여러개 담는 Form-data 구조

import java.util.Base64;                            // Base64 인코딩/디코딩 (이미지를 문자열 ↔ 바이트 변환)
import java.util.Map;                               // RequestBody로 받은 JSON을 Map 형태로 읽기



@RestController                          // 스프링 REST API 컨트롤러 선언
@RequestMapping("/trash/large")          // 모든 요청 URL 앞에 /trash/large 붙음
public class LargeTrashController {

    @PostMapping("/analyze")             // POST /trash/large/analyze
    public ResponseEntity<?> analyzeLargeTrash(@RequestBody Map<String, String> request) {

        // 프론트에서 전달한 이미지 base64
        String base64Image = request.get("image");

        // 이미지가 없으면 에러 반환
        if (base64Image == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 없음"));
        }

        try {
            // ============================================================
            // 1) Base64 문자열에서 실제 이미지 바이트(byte[]) 추출
            // (data:image/png;base64,....) 이런 형식이라 split(",")[1] 필요
            // ============================================================
            byte[] imageBytes = Base64.getDecoder().decode(base64Image.split(",")[1]);

            // ============================================================
            // 2) FastAPI(YOLO 서버)에 multipart/form-data 형식으로 요청할 준비
            // ============================================================
            RestTemplate rest = new RestTemplate();   // HTTP 요청 보내는 클라이언트

            HttpHeaders headers = new HttpHeaders();  // 헤더 생성
            headers.setContentType(MediaType.MULTIPART_FORM_DATA); // 파일 업로드 형태 지정

            // multipart 요청인지라 MultiValueMap 구조 사용
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 파일 형태로 이미지 바이트를 포장
            body.add("file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {          // FastAPI가 파일명 필요함
                    return "image.jpg";
                }
            });

            // (body + headers)를 하나로 묶어서 요청 엔티티 생성
            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // ============================================================
            // 3) FastAPI YOLO 서버 URL ( 배포되면 여기 넣음)
            // ============================================================
            String fastApiUrl = "https://raseoyun-large-waste.hf.space/predict/recycle_item";

            // ============================================================
            // 4) FastAPI YOLO 서버로 실제 HTTP 요청 보내기
            // - 파일 업로드
            // - YOLO 분석 실행
            // - JSON 결과 받아오기
            // ============================================================
            ResponseEntity<Map> fastApiResponse =
                    rest.postForEntity(fastApiUrl, requestEntity, Map.class);

            // ============================================================
            // 5) YOLO가 반환한 JSON을 그대로 프론트로 보내기
            // ============================================================
            return ResponseEntity.ok(fastApiResponse.getBody());

        } catch (Exception e) {
            // 예외가 나면 에러 메시지 전달
            return ResponseEntity.status(500).body(
                    Map.of("error", "FastAPI 연결 실패", "detail", e.getMessage())
            );
        }
    }

}

