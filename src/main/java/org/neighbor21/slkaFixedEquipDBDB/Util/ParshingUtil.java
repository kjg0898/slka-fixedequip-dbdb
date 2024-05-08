package org.neighbor21.slkaFixedEquipDBDB.Util;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb.service
 * fileName       : VehicleClassificationUtil.java
 * author         : kjg08
 * date           : 24. 4. 11.
 * description    : 데이터 파싱을 위한 유틸 모음
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 4. 11.        kjg08           최초 생성
 */
public class ParshingUtil {

    //차량 분류 코드에 맞춰서 차량 종류로 분류
    public static String getVehicleClassification(int labelId) {
        switch (labelId) {
            case 0: return "승용차/SUV";
            case 1: return "소형버스";
            case 2: return "대형버스";
            case 3: return "트럭";
            case 4: return "대형 트레일러";
            case 5: return "오토바이/자전거";
            case 6: return "보행자";
            case 8: return "VAN/승합차/스타렉스";
            case 9: return "삼륜차";
            default: return "알 수 없음";
        }
    }
}
