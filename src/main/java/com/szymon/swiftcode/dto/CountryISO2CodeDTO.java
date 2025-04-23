package com.szymon.swiftcode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryISO2CodeDTO {
    private String countryISO2;
    private String countryName;

    private List<BranchDTO> swiftCodes;
}
