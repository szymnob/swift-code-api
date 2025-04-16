package com.szymon.swiftcode.repository;

import com.szymon.swiftcode.model.SwiftCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SwiftCodeRepository extends JpaRepository<SwiftCode, Long> {

    SwiftCode findBySwiftCodeAndIsHeadquarterFalse(String swiftCode);

}
