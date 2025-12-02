package com.example.recycling.controller;

import com.example.recycling.logic.TrashType;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/large")
public class LargeWastePriceController {

    @PostMapping("/price")
    public Map<String, Object> getPrice(@RequestBody Map<String, Object> req) {

        String type = (String) req.get("type");
        Map<String, Object> res = new HashMap<>();

        int price = 0;

        switch (type) {

            case "bab-sang":  // 밥상
                price = TrashType.getDiningTablePrice();
                break;

            case "seo-rap-jang": // 서랍장
                int drawers = (int) req.getOrDefault("drawers", 4);
                price = TrashType.getDrawerPrice(drawers);
                break;

            case "sofa":  //소파
                int person = (int) req.getOrDefault("person", 2);
                price = TrashType.getSofaPrice(person);
                break;

            case "chair":  //의자
                price = TrashType.getChairPrice();
                break;

            case "jang-long": // 장롱
                int width = (int) req.getOrDefault("width", 80);
                price = TrashType.getClosetPrice(width);
                break;

            case "desk":  //책상
                String deskSize = (String) req.getOrDefault("size", "소형");
                price = TrashType.getDeskPrice(
                        deskSize.equals("대형") ? TrashType.DeskSize.대형 : TrashType.DeskSize.소형
                );
                break;

            case "hwa-jang-dae":  //화장대
                String dtType = (String) req.getOrDefault("type2", "일반용");
                price = TrashType.getDressingTablePrice(
                        dtType.equals("미용실용") ? TrashType.DressingTableType.미용실용
                                : TrashType.DressingTableType.일반용
                );
                break;

            case "bed":  //침대
                String part = (String) req.getOrDefault("part", "매트리스");
                String size = (String) req.getOrDefault("size", "일인용");
                price = TrashType.getBedPrice(
                        TrashType.BedPart.valueOf(part),
                        TrashType.BedSize.valueOf(size)
                );
                break;

            case "bicycle":  //두발자전거
                price = TrashType.getBicyclePrice();
                break;

            case "hang-a-ri":  //항아리
                int height = (int) req.getOrDefault("height", 50);
                price = TrashType.getJarPrice(height);
                break;

            default:  //화분
                res.put("error", "가격 정보 없음: " + type);
                return res;
        }

        res.put("type", type);
        res.put("price", price);
        return res;
    }
}
