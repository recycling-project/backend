package com.example.recycling.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Toss Payments 결제 생성을 담당하는 컨트롤러
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    /**
     * 환경변수에서 불러오는 Toss Secret Key
     */
    @Value("${TOSS_SECRET_KEY}")
    private String SECRET_KEY;

    /**
     * 프론트엔드 URL (성공/실패 리다이렉트용)
     */
    @Value("${FRONT_URL}")
    private String FRONT_URL;


    @PostMapping("/start")
    public Map<String, Object> startPayment(@RequestBody Map<String, Object> req) {

        int price = (int) req.get("price");
        String orderId = UUID.randomUUID().toString();
        String orderName = "대형폐기물 배출 수수료";

        RestTemplate rest = new RestTemplate();
        String url = "https://api.tosspayments.com/v1/payments";

        // 🔥 헤더 (Basic Auth + JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(SECRET_KEY, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 🔥 Body (토스에 전달할 데이터)
        Map<String, Object> body = new HashMap<>();
        body.put("amount", price);
        body.put("orderId", orderId);
        body.put("orderName", orderName);
        body.put("successUrl", FRONT_URL + "/payment/success");
        body.put("failUrl", FRONT_URL + "/payment/fail");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 🔥 토스 결제 생성 요청
        ResponseEntity<Map> response = rest.postForEntity(url, entity, Map.class);

        // 🔥 토스가 전달하는 checkoutUrl 반환
        Map<String, Object> result = new HashMap<>();
        result.put("paymentUrl", response.getBody().get("checkoutUrl"));

        return result;
    }
}
