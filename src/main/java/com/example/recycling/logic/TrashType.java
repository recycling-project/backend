package com.example.recycling.logic;

import java.util.List;
import java.util.Map;

public class TrashType {
    public enum DeskSize { 소형, 대형 }
    public enum DressingTableType { 일반용, 미용실용 }
    public enum BedPart { 매트리스, 틀 }
    public enum BedSize { 일인용, 이인용 }

    // 밥상 (고정 가격)
    public static int getDiningTablePrice() {
        return 3000;
    }

    // 서랍장 (5단 미만 or 이상) / numberOfDrawers가 단수 관련 변수
    public static int getDrawerPrice(int numberOfDrawers) {
        return (numberOfDrawers < 5) ? 5000 : 7000;
    }

    // 소파 (1, 2, 3, 4인용) / personCapacity가 소파 크기 관련 변수
    public static int getSofaPrice(int personCapacity) {
        return switch (personCapacity) {
            case 1 -> 5000;
            case 2 -> 6000;
            case 3 -> 9000;
            case 4 -> 12000;
            default -> throw new IllegalArgumentException(personCapacity + "인용 소파는 가격이 책정되지 않았습니다.");
        };
    }

    // 의자 (고정 가격)
    public static int getChairPrice() {
        return 4000;
    }

    // 장롱 (짝 너비) / widthCm가 장롱 너비 관련 변수
    public static int getClosetPrice(int widthCm) {
        if (widthCm < 0) throw new IllegalArgumentException("너비는 0보다 작을 수 없습니다.");
        if (widthCm >= 120) return 12000;
        if (widthCm >= 90) return 9000;
        return 7000; // 90cm 미만
    }

    // 책상 (대형 or 소형)
    public static int getDeskPrice(DeskSize size) {
        return switch (size) {
            case 소형 -> 5000;
            case 대형 -> 6000;
        };
    }

    // 화장대 (일반 or 영업용)
    public static int getDressingTablePrice(DressingTableType type) {
        return switch (type) {
            case 일반용 -> 5000;
            case 미용실용 -> 10000;
        };
    }

    // 침대 (매트리스 or 틀)
    private static final Map<BedPart, Map<BedSize, Integer>> BED_PRICE_MAP = Map.of(
            BedPart.매트리스, Map.of(BedSize.일인용, 7000, BedSize.이인용, 8000),
            BedPart.틀, Map.of(BedSize.일인용, 3000, BedSize.이인용, 4000)
    );

    public static int getBedPrice(BedPart part, BedSize size) {
        Integer price = BED_PRICE_MAP.getOrDefault(part, Map.of()).get(size);
        if (price == null) {
            throw new IllegalArgumentException("해당 침대 조합(" + part + "/" + size + ")의 가격이 없습니다.");
        }
        return price;
    }

    // 자전거 (고정 가격)
    public static int getBicyclePrice() {
        return 5000;
    }

    // 항아리 (특정 cm 이상 미만)
    private record PriceTier(String description, int maxSize, int price) {}
    private static final List<PriceTier> JAR_PRICE_TABLE = List.of(
            new PriceTier("소형 (40cm 미만)", 40, 2000),
            new PriceTier("중형 (40cm 이상 70cm 미만)", 70, 3000),
            new PriceTier("대형 (70cm 이상)", Integer.MAX_VALUE, 5000)
    );

    public static int getJarPrice(int heightCm) {
        if (heightCm < 0) throw new IllegalArgumentException("높이는 0보다 작을 수 없습니다.");
        return JAR_PRICE_TABLE.stream()
                .filter(tier -> heightCm < tier.maxSize())
                .findFirst()
                .map(PriceTier::price)
                .orElseThrow(() -> new IllegalStateException("항아리 가격표가 잘못 설정되었습니다."));
    }

    public static void main(String[] args) {
        System.out.println("로직 생성");
    }
}