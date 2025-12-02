package com.example.recycling.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


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

        String url = "https://api.tosspayments.com/v1/payments/ready";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(SECRET_KEY, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", price);
        body.put("orderId", orderId);
        body.put("orderName", orderName);
        body.put("method", "CARD");
        body.put("successUrl", FRONT_URL + "/payment/success");
        body.put("failUrl", FRONT_URL + "/payment/fail");

        RestTemplate rest = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = rest.postForEntity(url, entity, Map.class);

        Map<String, Object> result = new HashMap<>();
        result.put("paymentUrl", response.getBody().get("nextRedirectUrl"));

        return result;
    }
}
