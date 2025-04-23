package com.szymon.swiftcode.utils;

import com.szymon.swiftcode.model.SwiftCode;
import com.szymon.swiftcode.repository.SwiftCodeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SwiftCodeParser {

    private final SwiftCodeRepository repository;

    public void parseSaveExcel(FileInputStream inputStream) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

        List<SwiftCode> readData = new ArrayList<>();

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        //skip header
        if(rowIterator.hasNext()) {
            rowIterator.next();
        }

        while(rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row == null || isRowEmpty(row)) continue;

            try {
                String countryISO2 = Objects.requireNonNull(getCellValue(row, 0)).toUpperCase();
                String swiftCode = getCellValue(row, 1);
                String codeType = getCellValue(row, 2);
                String bankName = getCellValue(row, 3);
                String address = getCellValue(row, 4);
                String city = getCellValue(row, 5);
                String country = Objects.requireNonNull(getCellValue(row, 6)).toUpperCase();
                String timeZone = getCellValue(row, 7);

                if (swiftCode == null || swiftCode.isEmpty() || Objects.requireNonNull(swiftCode).length() < 8 ) continue;

                boolean isHeadquarter = swiftCode.endsWith("XXX");


                SwiftCode swiftEntity = SwiftCode.builder()
                        .swiftCode(swiftCode)
                        .isHeadquarter(isHeadquarter)
                        .countryISO2(countryISO2)
                        .bankName(bankName)
                        .address(address)
                        .city(city)
                        .country(country)
                        .timeZone(timeZone)
                        .build();

                readData.add(swiftEntity);
            } catch (Exception e) {
                System.out.println("Error parsing row: " + e.getMessage());
            }
        }
        repository.saveAll(readData);
        workbook.close();
    }

    private String getCellValue(Row row, int col) {
        Cell cell = row.getCell(col);
        String cellValue;
        try {
            cellValue = cell.getStringCellValue().trim();
        } catch (Exception e) {
            return null;
        }

        if (cellValue.isEmpty()) {
            return null;
        }
        return cellValue;

    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;

        for (int c = 0; c < 8; c++) {
            String value = getCellValue(row, c);
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }


}
