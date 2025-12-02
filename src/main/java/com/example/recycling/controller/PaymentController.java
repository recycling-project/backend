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

        // price 안전하게 파싱
        Object priceObj = req.get("price");
        int price = Integer.parseInt(priceObj.toString());

        String orderId = UUID.randomUUID().toString();
        String orderName = "대형폐기물 배출 수수료";

        RestTemplate rest = new RestTemplate();
        String url = "https://api.tosspayments.com/v1/payments";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(SECRET_KEY, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", price);
        body.put("orderId", orderId);
        body.put("orderName", orderName);
        body.put("successUrl", FRONT_URL + "/payment/success");
        body.put("failUrl", FRONT_URL + "/payment/fail");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // Toss 요청
        ResponseEntity<Map> response = rest.postForEntity(url, entity, Map.class);

        Map<String, Object> result = new HashMap<>();
        result.put("paymentUrl", response.getBody().get("checkoutUrl"));

        return result;
    }
}
