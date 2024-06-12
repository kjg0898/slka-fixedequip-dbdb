package org.neighbor21.slkaFixedEquipDBDB.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.service
 * fileName       : LastQueriedTimeService.java
 * author         : kjg08
 * date           : 2024-05-02
 * description    : 마지막 조회 시간을 관리하는 서비스 클래스.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-05-02        kjg08           최초 생성
 */
@Service
public class LastQueriedTimeService {
    private static final Logger logger = LoggerFactory.getLogger(LastQueriedTimeService.class);
    private static final String FILE_PATH = "lastQueriedTime.txt";
    private LocalDateTime lastQueriedDateTime = LocalDateTime.now().minusMinutes(5);

    /**
     * 생성자. 서비스 초기화 시 마지막 조회 시간을 파일에서 읽어옴.
     */
    public LastQueriedTimeService() {
        readLastQueriedDateTime();
    }

    /**
     * 마지막 조회 시간을 업데이트하고 파일에 저장.
     *
     * @param dateTime 업데이트할 시간
     */
    public void updateLastQueriedDateTime(LocalDateTime dateTime) {
        this.lastQueriedDateTime = dateTime;
        writeLastQueriedDateTime();
    }

    /**
     * 파일에서 마지막 조회 시간을 읽어옴. 파일이 없거나 읽기 실패 시 기본 시간을 설정.
     */
    private void readLastQueriedDateTime() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            createFileWithDefaultTime();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String dateTimeString = reader.readLine();
            if (dateTimeString != null) {
                this.lastQueriedDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
            }
        } catch (IOException e) {
            logger.error("Failed to read last queried date time from file. Using default value.", e);
            this.lastQueriedDateTime = LocalDateTime.now().minusMinutes(5);
            createFileWithDefaultTime();
        } catch (Exception e) {
            logger.error("Unexpected error occurred while reading last queried date time from file.", e);
            this.lastQueriedDateTime = LocalDateTime.now().minusMinutes(5);
            createFileWithDefaultTime();
        }
    }

    /**
     * 마지막 조회 시간을 파일에 저장.
     */
    private void writeLastQueriedDateTime() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(this.lastQueriedDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        } catch (IOException e) {
            logger.error("Failed to write last queried date time to file.", e);
        } catch (Exception e) {
            logger.error("Unexpected error occurred while writing last queried date time to file.", e);
        }
    }

    /**
     * 파일이 없거나 읽기 실패 시 기본 시간을 설정하고 파일을 생성.
     */
    private void createFileWithDefaultTime() {
        this.lastQueriedDateTime = LocalDateTime.now().minusMinutes(5);
        writeLastQueriedDateTime();
    }

    /**
     * 마지막 조회 시간을 반환.
     *
     * @return 마지막 조회 시간
     */
    public LocalDateTime getLastQueriedDateTime() {
        return lastQueriedDateTime;
    }
}
