package org.neighbor21.slkaFixedEquipDBDB.Util;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb.Util
 * fileName       : ParshingUtil.java
 * author         : kjg08
 * date           : 24. 4. 11.
 * description    : 데이터 파싱을 위한 유틸리티 클래스 모음.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 4. 11.        kjg08           최초 생성
 */
public class ParshingUtil {

    /**
     * 차량 분류 코드에 맞춰서 차량 종류로 분류.
     *
     * @param labelId 차량 분류 코드
     * @return 차량 종류
     */
    public static String getVehicleClassification(int labelId) {
        return switch (labelId) {
            case 0 -> "승용차/SUV";
            case 1 -> "소형버스";
            case 2 -> "대형버스";
            case 3 -> "트럭";
            case 4 -> "대형 트레일러";
            case 5 -> "오토바이/자전거";
            case 6 -> "보행자";
            case 8 -> "VAN/승합차/스타렉스";
            case 9 -> "삼륜차";
            default -> "알 수 없음";
        };
    }
}
