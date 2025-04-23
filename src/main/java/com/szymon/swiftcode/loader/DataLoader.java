package com.szymon.swiftcode.loader;

import com.szymon.swiftcode.repository.SwiftCodeRepository;
import com.szymon.swiftcode.utils.SwiftCodeParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final SwiftCodeRepository swiftCodeRepository;
    private final SwiftCodeParser swiftCodeParser;

    @Value("${swift.data-file-path}")
    private String filePath;

    @Override
    public void run(String... args) throws Exception {
        try{
            if(swiftCodeRepository.count() > 0){
                System.out.println("Data already loaded");
                return;
            }

            swiftCodeRepository.deleteAll();
            FileInputStream file = new FileInputStream(filePath);

            swiftCodeParser.parseSaveExcel(file);
            file.close();

            System.out.println("Data loaded successfully");


        }catch (Exception e){
            System.out.println("Error loading data: " + e.getMessage());
        }
    }
}
