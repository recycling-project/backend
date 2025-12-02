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
ResponseEntity<Map> response = rest.postForEntity(url, entity, Map.class);

// 핵심! 이걸 꼭 넣어야 프론트에서 paymentUrl을 받는다.
Object paymentUrl = response.getBody().get("paymentUrl");
Object redirectUrl = response.getBody().get("nextRedirectUrl");
Object checkoutUrl = response.getBody().get("checkoutUrl");

Map<String, Object> result = new HashMap<>();
result.put("paymentUrl",
           paymentUrl != null ? paymentUrl :
                   redirectUrl != null ? redirectUrl :
                   checkoutUrl);

return result;
