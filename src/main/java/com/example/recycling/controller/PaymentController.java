package com.example.recycling.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Value("${TOSS_SECRET_KEY}")
    private String SECRET_KEY;

    @Value("${FRONT_URL}")
    private String FRONT_URL;

    @PostMapping("/start")
    public Map<String, Object> startPayment(@RequestBody Map<String, Object> req) {

        int price = Integer.parseInt(req.get("price").toString());

        String orderId = UUID.randomUUID().toString();
        String orderName = "대형폐기물 배출 수수료";

        RestTemplate rest = new RestTemplate();
        String url = "https://api.tosspayments.com/v1/payments";

        // 🔥 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(SECRET_KEY, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 🔥 요청 바디
        Map<String, Object> body = new HashMap<>();
        body.put("amount", price);
        body.put("orderId", orderId);
        body.put("orderName", orderName);
        body.put("method", "CARD");

        body.put("successUrl", FRONT_URL + "/payment/success");
        body.put("failUrl", FRONT_URL + "/payment/fail");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 🔥 토스 API 호출
        ResponseEntity<Map> response = rest.postForEntity(url, entity, Map.class);

        // checkoutUrl 또는 redirectUrl 대응
        Object checkoutUrl = response.getBody().get("checkoutUrl");
        Object redirectUrl = response.getBody().get("redirectUrl");

        Map<String, Object> result = new HashMap<>();
        result.put("paymentUrl", checkoutUrl != null ? checkoutUrl : redirectUrl);

        return result;
    }
}
