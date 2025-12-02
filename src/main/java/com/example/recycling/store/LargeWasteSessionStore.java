package com.example.recycling.store;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LargeWasteSessionStore {

    // 1) 업로드된 이미지 저장소
    private final Map<String, String> imageMap = new ConcurrentHashMap<>();

    // 2) 최근 업로드된 이미지 ID
    private String lastUploadedId = null;

    // 3) 이미지 저장
    public void saveImage(String id, String base64) {
        imageMap.put(id, base64);
    }

    // 4) 이미지 조회
    public String getImage(String id) {
        return imageMap.get(id);
    }

    // 5) 최근 업로드 ID 저장
    public void setLastUploadedId(String id) {
        lastUploadedId = id;
    }

    // 6) 최근 업로드 ID 조회
    public String getLastUploadedId() {
        return lastUploadedId;
    }

    // 7) QR 페이지 시작 시 초기화
    public void reset() {
        lastUploadedId = null;
    }

    //  8) 사용 완료 후 단일 이미지 삭제
    public void deleteImage(String id) {
        imageMap.remove(id);
    }

    //  9) 전체 리셋 (이미지 + lastUploadedId 모두 삭제)
    public void resetAll() {
        imageMap.clear();
        lastUploadedId = null;
    }
}
