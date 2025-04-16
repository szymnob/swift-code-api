package com.szymon.swiftcode;

import com.szymon.swiftcode.model.SwiftCode;
import com.szymon.swiftcode.repository.SwiftCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.szymon.swiftcode.utils.SwiftCodeParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SwiftCodeParserTest {

    @Mock
    private SwiftCodeRepository repository;

    @Captor
    private ArgumentCaptor<List<SwiftCode>> swiftCaptor;

    private SwiftCodeParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new SwiftCodeParser(repository);
    }

    @Test
    void testParseSwiftCode() throws IOException {
        FileInputStream file = new FileInputStream("src/test/resources/example.xlsx");

        parser.parseSaveExcel(file);

        verify(repository).saveAll(swiftCaptor.capture());
        List<SwiftCode> savedEntities = swiftCaptor.getValue();

        assertEquals(7, savedEntities.size());

        SwiftCode model = savedEntities.getFirst();
        assertEquals("AAISALTRXXX", model.getSwiftCode());
        assertEquals("UNITED BANK OF ALBANIA SH.A", model.getBankName());
        assertEquals("HYRJA 3 RR. DRITAN HOXHA ND. 11 TIRANA, TIRANA, 1023", model.getAddress());
        assertEquals("TIRANA", model.getCity());
        assertEquals("ALBANIA", model.getCountry());
        assertEquals("Europe/Tirane", model.getTimeZone());
        assertFalse(model.isHeadquarter());
    }

    //one
    @Test
    void testParseSwiftCodeWithHeadquarter() throws IOException {
        File testFile = createTestExcelWithHeadquarter();
        FileInputStream fis = new FileInputStream(testFile);

        parser.parseSaveExcel(fis);

        verify(repository).saveAll(swiftCaptor.capture());
        List<SwiftCode> savedEntities = swiftCaptor.getValue();

        SwiftCode headquarter = savedEntities.stream()
                .filter(SwiftCode::isHeadquarter)
                .findFirst()
                .orElse(null);

        assertNotNull(headquarter, "Powinien być przynajmniej jeden bank oznaczony jako centrala");
        assertEquals("TESTPL00XXX", headquarter.getSwiftCode());
        assertTrue(headquarter.isHeadquarter());
    }

    @Test
    void testParseEmptySwiftCode() throws IOException {
        File testFile = createTestExcelWithEmptySwiftCode();
        FileInputStream fis = new FileInputStream(testFile);

        parser.parseSaveExcel(fis);

        verify(repository).saveAll(swiftCaptor.capture());
        List<SwiftCode> savedEntities = swiftCaptor.getValue();

        assertEquals(1, savedEntities.size());
        assertEquals("VALIDCODE", savedEntities.getFirst().getSwiftCode());
    }

    @Test
    void testParseEmptyExcel() throws IOException {
        File testFile = createEmptyExcel();
        FileInputStream fis = new FileInputStream(testFile);

        parser.parseSaveExcel(fis);

        verify(repository).saveAll(swiftCaptor.capture());
        List<SwiftCode> savedEntities = swiftCaptor.getValue();

        assertTrue(savedEntities.isEmpty());
    }

    @Test
    void testParseMissingFields() throws IOException {
        File testFile = createTestExcelWithMissingFields();
        FileInputStream fis = new FileInputStream(testFile);

        parser.parseSaveExcel(fis);

        verify(repository).saveAll(swiftCaptor.capture());
        List<SwiftCode> savedEntities = swiftCaptor.getValue();

        assertEquals(1, savedEntities.size());
        SwiftCode entity = savedEntities.get(0);
        assertEquals("SWIFTCODE", entity.getSwiftCode());
        assertNull(entity.getBankName());
        assertEquals("Warsaw", entity.getCity());
    }

    @Test
    void testParseTrimmedValues() throws IOException {
        File testFile = createTestExcelWithWhitespaces();
        FileInputStream fis = new FileInputStream(testFile);

        parser.parseSaveExcel(fis);

        verify(repository).saveAll(swiftCaptor.capture());
        List<SwiftCode> savedEntities = swiftCaptor.getValue();

        assertEquals(1, savedEntities.size());
        SwiftCode entity = savedEntities.getFirst();
        assertEquals("TRIMCODE", entity.getSwiftCode());
        assertEquals("Test Bank Name", entity.getBankName());
    }

    @Test
    void testExceptionHandling() {
        assertThrows(IOException.class, () -> {
            FileInputStream fis = new FileInputStream("non_existing_file.xlsx");
            parser.parseSaveExcel(fis);
        });

        verify(repository, never()).saveAll(any());
    }


    private File createTestExcelWithHeadquarter() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        // header row
        Row headerRow = sheet.createRow(0);
        createHeader(headerRow);

        Row dataRow1 = sheet.createRow(1);
        createCell(dataRow1, 0, "PL");
        createCell(dataRow1, 1, "TESTPL00XXX");
        createCell(dataRow1, 2, "XXX");
        createCell(dataRow1, 3, "Test Bank HQ");
        createCell(dataRow1, 4, "Test Address");
        createCell(dataRow1, 5, "Warsaw");
        createCell(dataRow1, 6, "Poland");
        createCell(dataRow1, 7, "Europe/Warsaw");

        // no central bank
        Row dataRow2 = sheet.createRow(2);
        createCell(dataRow2, 0, "PL");
        createCell(dataRow2, 1, "TESTPL01");
        createCell(dataRow2, 2, "Branch");
        createCell(dataRow2, 3, "Test Bank Branch");
        createCell(dataRow2, 4, "Branch Address");
        createCell(dataRow2, 5, "Krakow");
        createCell(dataRow2, 6, "Poland");
        createCell(dataRow2, 7, "Europe/Warsaw");

        return saveWorkbookToTempFile(workbook, "headquarters.xlsx");
    }

    private File createTestExcelWithEmptySwiftCode() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row headerRow = sheet.createRow(0);
        createHeader(headerRow);

        Row dataRow1 = sheet.createRow(1);
        createCell(dataRow1, 0, "DE");
        createCell(dataRow1, 1, "VALIDCODE");
        createCell(dataRow1, 2, "Regular");
        createCell(dataRow1, 3, "Valid Bank");
        createCell(dataRow1, 4, "Valid Address");
        createCell(dataRow1, 5, "Berlin");
        createCell(dataRow1, 6, "Germany");
        createCell(dataRow1, 7, "Europe/Berlin");

        //empty swift code
        Row dataRow2 = sheet.createRow(2);
        createCell(dataRow2, 0, "FR");
        createCell(dataRow2, 1, "");
        createCell(dataRow2, 2, "Regular");
        createCell(dataRow2, 3, "Empty SWIFT Bank");
        createCell(dataRow2, 4, "Address");
        createCell(dataRow2, 5, "Paris");
        createCell(dataRow2, 6, "France");
        createCell(dataRow2, 7, "Europe/Paris");

        return saveWorkbookToTempFile(workbook, "empty_swift.xlsx");
    }

    private File createEmptyExcel() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row headerRow = sheet.createRow(0);
        createHeader(headerRow);

        return saveWorkbookToTempFile(workbook, "empty.xlsx");
    }

    private File createTestExcelWithMissingFields() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row headerRow = sheet.createRow(0);
        createHeader(headerRow);

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "PL");
        createCell(dataRow, 1, "SWIFTCODE");
        createCell(dataRow, 2, "Regular");

        createCell(dataRow, 4, "Address Street 123");
        createCell(dataRow, 5, "Warsaw");
        createCell(dataRow, 6, "Poland");
        createCell(dataRow, 7, "Europe/Warsaw");

        return saveWorkbookToTempFile(workbook, "missing_fields.xlsx");
    }

    private File createTestExcelWithWhitespaces() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");


        Row headerRow = sheet.createRow(0);
        createHeader(headerRow);


        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "PL");
        createCell(dataRow, 1, " TRIMCODE  ");
        createCell(dataRow, 2, "Regular");
        createCell(dataRow, 3, "  Test Bank Name  ");
        createCell(dataRow, 4, "Test Address");
        createCell(dataRow, 5, "Warsaw");
        createCell(dataRow, 6, "Poland");
        createCell(dataRow, 7, "Europe/Warsaw");

        return saveWorkbookToTempFile(workbook, "whitespaces.xlsx");
    }

    private void createHeader(Row headerRow){
        createCell(headerRow, 0, "Country ISO2");
        createCell(headerRow, 1, "Swift Code");
        createCell(headerRow, 2, "Code Type");
        createCell(headerRow, 3, "Bank Name");
        createCell(headerRow, 4, "Address");
        createCell(headerRow, 5, "City");
        createCell(headerRow, 6, "Country");
        createCell(headerRow, 7, "Time Zone");

    }

    private void createCell(Row row, int col, String value) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
    }

    private File saveWorkbookToTempFile(XSSFWorkbook workbook, String fileName) throws IOException {
        File file = tempDir.resolve(fileName).toFile();
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }
        workbook.close();
        return file;
    }
}