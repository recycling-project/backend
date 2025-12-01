//대형 업로드 감지 컨트롤러

package com.example.recycling.controller;

import com.example.recycling.store.LargeWasteSessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/large")   // 모든 URL 앞에 /large 붙음
public class LargeWasteSyncController {

    @Autowired
    private LargeWasteSessionStore sessionStore;


    // ============================================================
    // 🔥 1) PC가 QR 페이지 진입할 때 세션 초기화
    //     → 이전 업로드 기록이 남아있으면 안 되니까 reset 필수
    // ============================================================
    @PostMapping("/reset")
    public ResponseEntity<?> resetUpload() {
        sessionStore.reset();
        return ResponseEntity.ok(Map.of("status", "reset"));
    }


    // ============================================================
    // 🔥 2) 모바일에서 base64 이미지 업로드 (핸드폰 → 서버)
    //     → 업로드 성공 시 새로운 ID 생성 후 저장
    // ============================================================
    @PostMapping("/mobile-upload")
    public ResponseEntity<?> uploadMobile(@RequestBody Map<String, String> body) {

        // base64 추출
        String base64 = body.get("image");
        if (base64 == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 없음"));
        }

        // 업로드 고유 ID 생성(UUID)
        String id = UUID.randomUUID().toString();

        // base64 이미지 저장
        sessionStore.saveImage(id, base64);

        // 마지막 업로드 ID 저장 → PC가 이 값을 감지함
        sessionStore.setLastUploadedId(id);

        // 모바일에게 업로드 성공 ID 반환
        return ResponseEntity.ok(Map.of("id", id));
    }


    // ============================================================
    // 🔥 3) PC(키오스크)가 1초마다 호출 (업로드 되었는지 체크)
    //     → 업로드가 되면 lastUploadedId 반환
    // ============================================================
    @GetMapping("/check")
    public ResponseEntity<?> checkUpload() {
        String id = sessionStore.getLastUploadedId();
        return ResponseEntity.ok(Map.of("id", id));
    }


    // ============================================================
    // 🔥 4) wait → analyze로 넘어갈 때 실제 사진 가져오기
    // ============================================================
    @GetMapping("/image")
    public ResponseEntity<?> getImage(@RequestParam String id) {
        String base64 = sessionStore.getImage(id);

        if (base64 == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 없음"));
        }

        return ResponseEntity.ok(Map.of("image", base64));
    }
}
