package com.szymon.swiftcode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class BranchDTO {
    private String address;
    @NotBlank(message = "Bank name cannot be empty")
    private String bankName;
    @NotBlank(message = "Country ISO2 code cannot be empty")
    private String countryISO2;
    @NotBlank(message = "Country cannot be empty")
    private String countryName;
    @NotNull(message = "isHeadquarter cannot be empty")
    private boolean isHeadquarter;
    @NotBlank(message = "Swift code cannot be empty")
    @Size(min = 8, max = 11, message = "Swift code must be between 8 and 11 characters")
    private String swiftCode;
}
