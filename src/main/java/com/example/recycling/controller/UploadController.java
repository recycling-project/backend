package com.example.recycling.controller;

import com.example.recycling.store.SessionStore;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/recycle")
public class UploadController {

    private final SessionStore sessionStore;

    public UploadController(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    // =====================================================================
    // 📌 1. QR 찍으면 처음 들어오는 페이지 (모바일 업로드 화면 제공)
    //
    // - QR 코드를 스캔하면 이동하는 URL:
    //   https://backend-production-fc4f.up.railway.app/recycle/upload-mobile
    //
    // - 이 HTML은 Next.js가 아니라 Spring이 직접 렌더링하는 업로드용 화면.
    //   모바일에서 파일 선택 창이 떠야 하기 때문에 이렇게 단순 HTML로 제공함.
    // =====================================================================
    @GetMapping("/upload-mobile")
    public ResponseEntity<String> uploadPage() {

        String html = """
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>모바일 업로드</title>
        </head>
        <body>

            <h2>모바일 사진 업로드</h2>

            <form method="post" action="/recycle/upload-mobile" enctype="multipart/form-data">
                <input type="file" name="file" accept="image/*" />
                <button type="submit">업로드</button>
            </form>

        </body>
        </html>
    """;

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }

    // =====================================================================
    // 📌 2. 모바일에서 사진 업로드 처리
    //
    // - 사용자가 업로드한 이미지를 MultipartFile로 받음
    // - 이를 Base64 문자열로 변환함
    // - 변환된 base64를 SessionStore에 "mobile_image" 키로 저장
    //
    // - 그 후, 분석 페이지(analyze)로 리다이렉트:
    //   https://frontend-self-delta-10.vercel.app/general_waste/analyze?type=photo
    //
    // - 프론트의 analyze 페이지는:
    //    1) localStorage에 이미지 없으면
    //    2) /mobile-image API로 다시 요청해서 base64 가져감
    // =====================================================================
    @PostMapping("/upload-mobile")
    public ResponseEntity<?> uploadMobile(@RequestParam("file") MultipartFile file) {

        try {
            // 1) 이미지 → Base64로 변환
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            // 2) SessionStore에 저장
            sessionStore.saveResult("mobile_image", base64);

            // 3) 분석 페이지로 리다이렉트
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location",
                    "https://frontend-self-delta-10.vercel.app/general_waste/analyze?type=photo"
            );

            return ResponseEntity.status(302).headers(headers).build();

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Upload Error: " + e.getMessage());
        }
    }

    // =====================================================================
    // 📌 3. 프론트 analyze 페이지에서 호출하는 API
    //
    // - QR로 업로드한 경우 localStorage에 이미지가 없음
    // - 그래서 analyze 페이지는 백엔드에 다시 요청해서 base64를 가져감
    //
    // - URL:
    //   GET https://backend-production-fc4f.up.railway.app/recycle/mobile-image
    //
    // - 반환 데이터 형식:
    //   { "image": "<base64 문자열>" }
    //
    // - 프론트 analyze 페이지는 이 base64를 받아서 GPT 분석을 진행함
    // =====================================================================
    @GetMapping("/mobile-image")
    public ResponseEntity<?> getMobileImage() {

        String img = sessionStore.getResult("mobile_image");

        Map<String, String> map = new HashMap<>();
        map.put("image", img);

        return ResponseEntity.ok(map);
    }

}
