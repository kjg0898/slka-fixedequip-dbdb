/*
package org.neighbor21.slkaFixedEquipDBDB;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class DataGenerator {
    public static void main(String[] args) {
        Random random = new Random();
        int totalEntries = 100000;  // 생성할 레코드 수
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String filePath = "data.csv";  // 저장할 파일 경로

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // CSV 헤더 작성
            writer.write("CamID,SiteName,TrackingID,LabelID,LabelName,LabelGroup,RegionID,RegionName,Velocity,EventID,EventName,TimeStamp\n");

            for (int i = 0; i < totalEntries; i++) {
                int camID = random.nextInt(100) + 1;  // 1에서 100 사이의 카메라 ID
                String trackingID = "ID" + (1000 + i);  // 트래킹 ID
                LocalDateTime timeStamp = LocalDateTime.now().minusMinutes(random.nextInt(60 * 24 * 365));  // 최대 1년 전까지의 랜덤 타임스탬프
                String formattedDate = timeStamp.format(formatter);

                String[] sites = {"Main Street", "Second Avenue", "Third Boulevard"};
                String siteName = sites[random.nextInt(sites.length)];

                int labelID = random.nextInt(8) + 1;  // 1에서 8 사이의 차량 분류 코드
                String[] labels = {"SUV", "Sedan", "Truck", "Compact", "Convertible", "Van", "Sports Car", "Motorcycle"};
                String labelName = labels[labelID - 1];

                int labelGroup = random.nextInt(3) + 1;  // 1에서 3 사이의 차량 분류 그룹

                int regionID = random.nextInt(10) + 101;  // 101에서 110 사이의 지역 ID
                String[] regions = {"Downtown", "Uptown", "Midtown", "Eastside", "Westside", "North Hills", "South Valley", "Central Park", "River District", "Old Town"};
                String regionName = regions[regionID - 101];

                int velocity = random.nextInt(120) + 10;  // 10에서 130 km/h 사이의 속도

                int eventID = random.nextInt(3);  // 이벤트 ID: 0 (없음), 1 (정지), 2 (후진)
                String eventName = eventID == 1 ? "Stop" : eventID == 2 ? "Reverse" : "";

                // CSV 데이터 작성
                String csvLine = String.format(
                        "%d,%s,%s,%d,%s,%d,%d,%s,%d,%d,%s,%s\n",
                        camID, siteName, trackingID, labelID, labelName, labelGroup, regionID, regionName, velocity, eventID, eventName, formattedDate
                );

                // 파일에 쓰기
                writer.write(csvLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("데이터 생성 완료. 파일 경로: " + filePath);
    }
}
*/
