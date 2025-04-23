package com.szymon.swiftcode.loader;

import com.szymon.swiftcode.repository.SwiftCodeRepository;
import com.szymon.swiftcode.utils.SwiftCodeParser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final SwiftCodeRepository swiftCodeRepository;
    private final SwiftCodeParser swiftCodeParser;


    @Override
    public void run(String... args) throws Exception {
        try{
            if(swiftCodeRepository.count() == 0){
                FileInputStream file = new FileInputStream("src/main/resources/swiftCodes.xlsx");

                swiftCodeParser.parseSaveExcel(file);
                file.close();

                System.out.println("Data loaded successfully");
            }
            else{
                System.out.println("Data already loaded");
            }
        }catch (Exception e){
            System.out.println("Error loading data: " + e.getMessage());
        }
    }
}
