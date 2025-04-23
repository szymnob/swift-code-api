package com.szymon.swiftcode.controller;

import com.szymon.swiftcode.dto.MessageResponse;
import com.szymon.swiftcode.dto.BranchDTO;
import com.szymon.swiftcode.service.SwiftCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1/swift-codes")
@RequiredArgsConstructor
public class SwiftController {
    private final SwiftCodeService swiftCodeService;

    @GetMapping("/{swiftCode}")
    public ResponseEntity<?> getSwiftDetails(@PathVariable String swiftCode) {
        return ResponseEntity.ok(swiftCodeService.getSwiftDetails(swiftCode));
    }

    @GetMapping("/country/{countryISO2}")
    public ResponseEntity<?> getSwiftByCountry(@PathVariable String countryISO2) {
        return ResponseEntity.ok(swiftCodeService.getSwiftCodesByCountry(countryISO2));
    }

    @DeleteMapping("/{swiftCode}")
    public ResponseEntity<?> deleteSwift(@PathVariable String swiftCode) {
        String message = swiftCodeService.deleteSwiftCode(swiftCode);
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @PostMapping
    public ResponseEntity<?> addSwift(@Valid @RequestBody BranchDTO branchDTO) {
        String message = swiftCodeService.addSwiftCode(branchDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse(message));
    }
}
