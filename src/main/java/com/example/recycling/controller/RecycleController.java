package com.example.recycling.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Map;



@RestController                               // 이 클래스가 REST API(= JSON 응답)용 컨트롤러라는 의미
@RequestMapping("/recycle")                   // 이 클래스 안의 모든 API 경로 앞에 "/recycle"이 붙음
public class RecycleController {

    private final String OPENAI_API_KEY;

    public RecycleController() {
        // .env 파일 자동 로드
        Dotenv dotenv = Dotenv.load();
        this.OPENAI_API_KEY = dotenv.get("GPT_API_KEY");
    }

    // OpenAI GPT Vision API 엔드포인트 주소
    private final String GPT_URL = "https://api.openai.com/v1/chat/completions";

    // POST /recycle/analyze 엔드포인트
    // 프론트(또는 ThunderClient)에서 base64 이미지가 들어오면 이 함수가 실행됨
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeRecycle(@RequestBody Map<String, String> request) {

        // JSON에서 이미지(base64 문자열)를 꺼냄
        // 예: { "image": "iVBORw0KGgoAAA..." }
        String base64Image = request.get("image");
        String text = request.get("text");  // 텍스트질문 추후 없앨수도 있음

        // ------------------------------
        // 1) GPT Vision API 호출 준비
        // ------------------------------

        // RestTemplate: Java에서 외부 API(HTTP)를 호출하는 도구
        RestTemplate rt = new RestTemplate();

        // HTTP 요청 header(=메타데이터) 만들기
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + OPENAI_API_KEY);  // 인증키
        headers.setContentType(MediaType.APPLICATION_JSON);         // 요청 타입: JSON

        // 지피티 연결 확인용 .. 사진넣고 실제 확인하는건 돈들어감 !!
        String prompt =
                "너는 한국 기준 분리배출/재활용 전문가다.\n" +
                        "아래 품목을 분석한 뒤 반드시 지정된 형식으로만 답해라.\n\n" +

                        "[출력 형식]\n" +
                        "문의한 내용: (품목 이름)\n" +
                        "재활용 가능 여부: (가능 / 불가능 / 일반쓰레기 / 음식물쓰레기)\n" +
                        "분리배출 방법:\n" +
                        " - 단계별로 간단하고 명확하게 작성\n" +
                        "추가 주의사항:\n" +
                        " - 필요한 경우에만 작성하며, 불필요한 항목은 절대 넣지 말 것.\n\n" +

                        "[중요 지침]\n" +
                        "- 해당 품목과 관련 없는 정보는 절대 쓰지 말 것.\n" +
                        "- 재질(PET, PP 등)은 출력하지 말 것.\n" +
                        "- 뚜껑/라벨/압축/세척 등은 '필요할 때만' 적고 다른 경우는 생략.\n" +
                        "- 답변은 반드시 한국 분리배출 기준만 따른다.\n\n" +
                        "- 모든 단어는 100% 순수 한국어로만 작성하라. 한자, 영어 혼용, 기호 표기 금지."+

                        "문의한 내용: '" + text + "'";


        Map<String, Object> payload;


        if (base64Image != null && !base64Image.isEmpty()) {
            //   사진 분석 모드
            payload = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", new Object[]{
                            Map.of(
                                    "role", "user",
                                    "content", new Object[]{
                                            Map.of("type", "input_text", "text", "이미지를 분석해줘."),
                                            Map.of("type", "input_image", "image_url", "data:image/png;base64," + base64Image)
                                    }
                            )
                    }
            );
        } else {
            //   텍스트 질문 모드
            payload = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", new Object[]{
                            Map.of(
                                    "role", "user",
                                    "content", prompt  //  텍스트로만 질문하는 모드
                            )
                    }
            );
        }


        // 헤더 + 바디를 묶어서 하나의 HTTP 요청으로 구성
        HttpEntity<Map<String, Object>> gptRequest = new HttpEntity<>(payload, headers);

        // ------------------------------
        // 2) GPT Vision API 호출
        // ------------------------------

        // GPT 응답을 String으로 받도록 변경
        String gptResponse = rt.postForObject(GPT_URL, gptRequest, String.class);


        // ------------------------------
        // 3) GPT 결과를 프론트에 그대로 응답
        // ------------------------------

        return ResponseEntity.ok(gptResponse);
    }
}
