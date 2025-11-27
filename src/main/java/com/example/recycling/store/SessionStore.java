package com.example.recycling.store;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStore {

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
}
