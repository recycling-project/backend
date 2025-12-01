//세션 저장소

package com.example.recycling.store;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component  // 스프링이 자동으로 메모리에 띄우고 Bean 으로 관리
public class LargeWasteSessionStore {

    // ============================================
    // 1) 업로드된 이미지를 저장하는 공간 (메모리)
    //    key: 업로드 ID (UUID)
    //    value: base64 이미지 데이터
    // ============================================
    private final Map<String, String> imageMap = new ConcurrentHashMap<>();


    // ============================================
    // 2) 가장 최근에 업로드된 이미지의 ID
    //    - PC(키오스크) wait 화면이 이 ID를 감지함
    // ============================================
    private String lastUploadedId = null;


    // ============================================
    // 3) 이미지 저장
    //    - 업로드 시 UUID를 key로 base64 이미지 저장
    // ============================================
    public void saveImage(String id, String base64) {
        imageMap.put(id, base64);
    }


    // ============================================
    // 4) ID로 이미지(base64) 조회
    //    - 분석 페이지에서 해당 이미지 불러올 때 사용
    // ============================================
    public String getImage(String id) {
        return imageMap.get(id);
    }


    // ============================================
    // 5) 최근 업로드된 이미지의 ID 설정
    //    - 모바일 업로드 시 마지막 업로드 ID를 저장
    // ============================================
    public void setLastUploadedId(String id) {
        lastUploadedId = id;
    }


    // ============================================
    // 6) 최근 업로드된 이미지의 ID 가져오기
    //    - PC가 1초마다 체크할 때 사용
    // ============================================
    public String getLastUploadedId() {
        return lastUploadedId;
    }


    // ============================================
    // 7) 초기화 (QR 페이지 들어올 때 실행)
    //    - 이전 업로드 기록을 모두 제거
    // ============================================
    public void reset() {
        lastUploadedId = null;
    }
}
