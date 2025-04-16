package com.szymon.swiftcode.service;

import com.szymon.swiftcode.dto.branchDTO;
import com.szymon.swiftcode.repository.SwiftCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SwiftCodeService {
    private final SwiftCodeRepository repository;


}
