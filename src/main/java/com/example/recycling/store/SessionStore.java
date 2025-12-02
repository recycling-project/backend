package com.example.recycling.store;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStore {

    // ====== GPT 분석 결과 저장 ======
    private final ConcurrentHashMap<String, String> resultMap = new ConcurrentHashMap<>();

    public void saveResult(String sessionId, String result) {
        resultMap.put(sessionId, result);
    }

    public String getResult(String sessionId) {
        return resultMap.get(sessionId);
    }

    public void remove(String sessionId) {
        resultMap.remove(sessionId);
    }


    // ====== 모바일 업로드 이미지 저장 ======
    private final ConcurrentHashMap<String, String> imageMap = new ConcurrentHashMap<>();

    public void saveImage(String id, String base64) {
        imageMap.put(id, base64);
    }

    public String getImage(String id) {
        return imageMap.get(id);
    }


    // ====== 마지막 업로드된 ID 저장 ======
    private String lastUploadedId = null;

    public void setLastUploadedId(String id) {
        this.lastUploadedId = id;
    }

    public String getLastUploadedId() {
        return lastUploadedId;
    }


    // ====== 사용 완료 후 삭제 기능 ======
    public void deleteImage(String id) {
        imageMap.remove(id);
    }

    public void resetAll() {
        imageMap.clear();
        resultMap.clear();
        lastUploadedId = null;
    }
}
