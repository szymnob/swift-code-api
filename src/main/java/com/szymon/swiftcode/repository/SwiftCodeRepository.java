package com.szymon.swiftcode.repository;

import com.szymon.swiftcode.model.SwiftCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SwiftCodeRepository extends JpaRepository<SwiftCode, Long> {

    SwiftCode findBySwiftCodeAndIsHeadquarterFalse(String swiftCode);

    List<SwiftCode> findBySwiftCodeStartingWithAndIsHeadquarterFalse(String prefix);

    SwiftCode findBySwiftCode(String swiftCode);

    List<SwiftCode> findByCountryISO2IgnoreCase(String iso2);

    boolean existsBySwiftCode(String swiftCode);

    void deleteBySwiftCode(String swiftCode);
}
