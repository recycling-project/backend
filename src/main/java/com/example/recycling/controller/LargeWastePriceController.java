package com.example.recycling.controller;

import com.example.recycling.logic.trash_type;
import org.springframework.web.bind.annotation.*;

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
                price = trash_type.getDiningTablePrice();
                break;

            case "seo-rap-jang": // 서랍장
                int drawers = (int) req.getOrDefault("drawers", 4); // 기본값 4단
                price = trash_type.getDrawerPrice(drawers);
                break;

            case "sofa":
                int person = (int) req.getOrDefault("person", 2); // 기본 2인용
                price = trash_type.getSofaPrice(person);
                break;

            case "chair":
                price = trash_type.getChairPrice();
                break;

            case "jang-long": // 장롱
                int width = (int) req.getOrDefault("width", 80); // 기본 80cm
                price = trash_type.getClosetPrice(width);
                break;

            case "desk":
                String deskSize = (String) req.getOrDefault("size", "소형");
                price = trash_type.getDeskPrice(
                        deskSize.equals("대형") ? trash_type.DeskSize.대형 : trash_type.DeskSize.소형
                );
                break;

            case "hwa-jang-dae": // 화장대
                String dtType = (String) req.getOrDefault("type2", "일반용");
                price = trash_type.getDressingTablePrice(
                        dtType.equals("미용실용") ? trash_type.DressingTableType.미용실용 :
                                trash_type.DressingTableType.일반용
                );
                break;

            case "bed":
                String part = (String) req.getOrDefault("part", "매트리스");
                String size = (String) req.getOrDefault("size", "일인용");
                price = trash_type.getBedPrice(
                        trash_type.BedPart.valueOf(part),
                        trash_type.BedSize.valueOf(size)
                );
                break;

            case "bicycle":
                price = trash_type.getBicyclePrice();
                break;

            case "hang-a-ri":
                int height = (int) req.getOrDefault("height", 50);
                price = trash_type.getJarPrice(height);
                break;

            default:
                res.put("error", "가격 정보 없음: " + type);
                return res;
        }

        res.put("type", type);
        res.put("price", price);
        return res;
    }
}
