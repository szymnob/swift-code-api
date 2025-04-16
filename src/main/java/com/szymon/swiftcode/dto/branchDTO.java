package com.szymon.swiftcode.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class branchDTO {
    private String address;
    private String bankName;
    private String countryISO2;
    private String countryName;
    private boolean isHeadquarter;
    private String swiftCode;
}
