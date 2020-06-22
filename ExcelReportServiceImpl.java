/*
 * Copyright (c) 2008-2020
 * LANIT
 * All rights reserved.
 *
 * This product and related documentation are protected by copyright and
 * distributed under licenses restricting its use, copying, distribution, and
 * decompilation. No part of this product or related documentation may be
 * reproduced in any form by any means without prior written authorization of
 * LANIT and its licensors, if any.
 *
 * $
 */
package ru.lanit.bpm.jedu.hrjedi.service.impl;

import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.lanit.bpm.jedu.hrjedi.model.Attendance;
import ru.lanit.bpm.jedu.hrjedi.model.Office;
import ru.lanit.bpm.jedu.hrjedi.repository.AttendanceRepository;
import ru.lanit.bpm.jedu.hrjedi.rest.StreamedResult;
import ru.lanit.bpm.jedu.hrjedi.rest.WorkbookResult;
import ru.lanit.bpm.jedu.hrjedi.service.ExcelReportService;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Month;
import java.util.*;

@Service
public class ExcelReportServiceImpl implements ExcelReportService {
    @Autowired
    AttendanceRepository attendanceRepository;

    @Override
    public StreamedResult createAttendanceReport(Month month, int year) {
        InputStream attendanceTemplate = getClass().getResourceAsStream("/reports/attendance.xlsx");

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(attendanceTemplate);
            List<Attendance> attendanceInfoByMonth = attendanceRepository.findAllByMonth(year, month.getValue());
            fillInSheet0(workbook, attendanceInfoByMonth, year);
            fillInSheet1(workbook, attendanceInfoByMonth);
            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            return new WorkbookResult(workbook);
        } catch (IOException e) {
            throw new IllegalStateException("Error in attendance report", e);
        }
    }

    private void fillInSheet0(XSSFWorkbook workbook, List<Attendance> attendanceInfoByMonth, int year) {
        XSSFSheet sheet = workbook.getSheetAt(0);
        Set<Map<String, String>> completedData = extractTargetDataFromAttendanceInfo(attendanceInfoByMonth);
        List<String> employeeValuesList;
        int keyRow = 3;
        int keyCell = 0;

        for (Map<String, String> employeeData : completedData) {
            employeeValuesList = new ArrayList<>(employeeData.values());
            employeeValuesList.remove(0);
            employeeValuesList.add(Integer.toString(year));

            printValuesByRow(sheet, keyRow, keyCell, employeeValuesList);
            keyRow++;
            keyCell = 0;
        }
    }

    private Set<Map<String, String>> extractTargetDataFromAttendanceInfo(List<Attendance> attendanceInfoByMonth) {
        Map<String, String> employeeDataMap = new LinkedHashMap<>();
        Set<Map<String, String>> extractedDataSet = new TreeSet<>((o1, o2) -> {
            Integer compareNumberOffice1 = getNumberForComparator(o1.get("office"));
            Integer compareNumberOffice2 = getNumberForComparator(o2.get("office"));
            int compareResult = compareNumberOffice1.compareTo(compareNumberOffice2);

            if (compareResult != 0) {
                return compareResult;
            }

            return o1.get("name").compareTo(o2.get("name"));
        });

        Map<Long, String> employeesSummaryHoursByMonth = getEmployeesSummaryHoursByMonth(attendanceInfoByMonth);

        for (Attendance attendance : attendanceInfoByMonth) {
            employeeDataMap.put("id",       Long.toString(attendance.getEmployee().getId()));
            employeeDataMap.put("name",     attendance.getEmployee().getFullName());
            employeeDataMap.put("time",     employeesSummaryHoursByMonth.get(attendance.getEmployee().getId()));
            employeeDataMap.put("office",   attendance.getOffice().getName() );
            extractedDataSet.add(new LinkedHashMap<>(employeeDataMap));
        }

        return extractedDataSet;
    }

    private HashMap<Long, String> getEmployeesSummaryHoursByMonth(List<Attendance> attendanceInfoByMonth) {
        Map<Long, Double> resultMap = new HashMap<>();
        double sumHours = 0;
        Long employeeId;

        for (Attendance attendance : attendanceInfoByMonth) {
           employeeId = attendance.getEmployee().getId();
           if (resultMap.containsKey(employeeId)) {
               resultMap.put(
                   employeeId,
                   resultMap.get(employeeId) + (Duration.between(attendance.getEntranceTime(), attendance.getExitTime()).getSeconds() / 3600)
               );
           }
           else {
               resultMap.put(
                   employeeId,
                   (Duration.between(attendance.getEntranceTime(), attendance.getExitTime()).getSeconds() / 3600.00)
               );
           }
        }

        HashMap<Long, String> convertedToStringAndRoundedHourValuesMap = new HashMap<>();
        for (Map.Entry<Long, Double> pair : resultMap.entrySet()) {
            convertedToStringAndRoundedHourValuesMap.put(
              pair.getKey(),
              Integer.toString((int)Math.ceil(pair.getValue()))
            );
        }

        return convertedToStringAndRoundedHourValuesMap;
    }

    private void fillInSheet1(XSSFWorkbook workbook, List<Attendance> attendanceInfoByMonth) {
        XSSFSheet sheet = workbook.getSheetAt(1);
        fillInSourceData(sheet, attendanceInfoByMonth);
        fillInSupportInformation(sheet, attendanceInfoByMonth);
    }

    private void fillInSourceData(XSSFSheet sheet, List<Attendance> attendanceInfoByMonth) {
        List<String> attendanceValuesList;
        int keyRow = 1;
        int keyCell = 0;

        for (Attendance attendance : attendanceInfoByMonth) {
            attendanceValuesList = new ArrayList<>(
                Arrays.asList(
                    attendance.getEmployee().getFullName(),
                    attendance.getEntranceTime().toString(),
                    attendance.getExitTime().toString(),
                    attendance.getOffice().getId().toString()
                )
            );

            printValuesByRow(sheet, keyRow, keyCell, attendanceValuesList);
            keyRow++;
            keyCell = 0;
        }
    }

    private void fillInSupportInformation(XSSFSheet sheet, List<Attendance> attendanceInfoByMonth) {
        List<Office> allOfficesList = new ArrayList<>();

        for (Attendance attendance : attendanceInfoByMonth) {
            allOfficesList.add(attendance.getOffice());
        }

        Set<Office> distinctOfficesSet = new HashSet<>(allOfficesList);

        List<String> officeValuesList;
        int keyRow = 1;
        int keyCell = 5;

        for (Office office : distinctOfficesSet) {
            officeValuesList = new ArrayList<>(
                Arrays.asList(
                    office.getId().toString(),
                    office.getName()
                )
            );

            printValuesByRow(sheet, keyRow, keyCell, officeValuesList);
            keyRow++;
            keyCell = 5;
        }
    }

    private void printValuesByRow(XSSFSheet sheet, int keyRow, int keyCell, List<String> valuesList) {
        XSSFRow row = sheet.getRow(keyRow);
        if (row == null) {
            row = sheet.createRow(keyRow);
        }

        for (String value : valuesList) {
            XSSFCell cell = row.getCell(keyCell++);
            if (cell == null) {
                cell = row.createCell(--keyCell);
                keyCell++;
            }

            cell.setCellValue(value);
        }
    }

    private int getNumberForComparator(String target) {
        int compareNumber;

        switch (target) {
            case "Москва":
                compareNumber = 3;
                break;
            case "Уфа":
                compareNumber = 2;
                break;
            case "Нижний Новгород":
                compareNumber = 1;
                break;
            default:
                throw new IllegalStateException("Invalid office value");
        }

        return compareNumber;
    }
}