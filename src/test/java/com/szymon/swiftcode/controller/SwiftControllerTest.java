package com.szymon.swiftcode.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.szymon.swiftcode.dto.BranchDTO;
import com.szymon.swiftcode.dto.CountryISO2CodeDTO;
import com.szymon.swiftcode.dto.HeadquarterDTO;
import com.szymon.swiftcode.exceptions.DuplicateResourceException;
import com.szymon.swiftcode.exceptions.ResourceNotFoundException;
import com.szymon.swiftcode.service.SwiftCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SwiftController.class)
class SwiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SwiftCodeService swiftCodeService;

    @Autowired
    private ObjectMapper objectMapper;

    //addSwift
    @Test
    void addSwift_shouldReturnValidationErrors_whenRequestBodyIsEmpty() throws Exception {
        String emptyBody = "{}";

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.address").value("Address cannot be empty"))
                .andExpect(jsonPath("$.errors.swiftCode").value("Swift code cannot be empty"))
                .andExpect(jsonPath("$.errors.bankName").value("Bank name cannot be empty"))
                .andExpect(jsonPath("$.errors.countryISO2").value("Country ISO2 code cannot be empty"))
                .andExpect(jsonPath("$.errors.countryName").value("Country cannot be empty"))
                .andExpect(jsonPath("$.errors.isHeadquarter").value("isHeadquarter cannot be empty"));
    }

    @Test
    void addSwift_shouldReturnValidationError_whenSwiftCodeIsTooShort() throws Exception {
        BranchDTO dto = BranchDTO.builder()
                .swiftCode("PKO")
                .bankName("PKO Bank Polski")
                .address("ul. Puławska 15, 02-515 Warsaw")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .build();

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.swiftCode").value("Swift code must be between 8 and 11 characters"));
    }

    @Test
    void addSwift_shouldReturnValidationError_whenCountryISO2IsInvalid() throws Exception {
        BranchDTO dto = BranchDTO.builder()
                .swiftCode("PKOPPLPW")
                .bankName("PKO Bank Polski")
                .address("ul. Puławska 15, 02-515 Warsaw")
                .countryISO2("POL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .build();

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.countryISO2").value("Country ISO2 code must be exactly 2 characters"));
    }

    @Test
    void addSwift_shouldReturnValidationErrors_whenSomeFieldsAreMissing() throws Exception {
        BranchDTO dto = BranchDTO.builder()
                .swiftCode("PKOPPLPW")
                .address("ul. Puławska 15, 02-515 Warsaw")
                .isHeadquarter(false)
                .build();

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.bankName").value("Bank name cannot be empty"))
                .andExpect(jsonPath("$.errors.countryISO2").value("Country ISO2 code cannot be empty"))
                .andExpect(jsonPath("$.errors.countryName").value("Country cannot be empty"));
    }

    @Test
    void addSwift_shouldReturnCreatedStatus_whenRequestIsValid() throws Exception {
        BranchDTO dto = BranchDTO.builder()
                .swiftCode("PKOPPLPW")
                .bankName("PKO Bank Polski")
                .address("ul. Puławska 15, 02-515 Warsaw")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .build();

        when(swiftCodeService.addSwiftCode(any())).thenReturn("Swift code added successfully");

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Swift code added successfully"));
    }

    @Test
    void addSwift_shouldReturnConflict_whenSwiftCodeAlreadyExists() throws Exception {
        BranchDTO dto = BranchDTO.builder()
                .swiftCode("PKOPPLPW")
                .bankName("PKO Bank Polski")
                .address("ul. Puławska 15, 02-515 Warsaw")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .build();

        String requestBody = objectMapper.writeValueAsString(dto);

        when(swiftCodeService.addSwiftCode(any()))
                .thenThrow(new DuplicateResourceException("SwiftCode", "swiftCode", "PKOPPLPW"));

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("SwiftCode already exists with swiftCode: 'PKOPPLPW'"));
    }

    @Test
    void getSwiftDetails_shouldReturnBranchDTO_whenValidBranchSwiftCodeProvided() throws Exception {
        BranchDTO dto = BranchDTO.builder()
                .swiftCode("PKOPPLPW001")
                .bankName("PKO Branch")
                .address("Branch Address")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .build();

        when(swiftCodeService.getSwiftDetails("PKOPPLPW001")).thenReturn(dto);

        mockMvc.perform(get("/v1/swift-codes/PKOPPLPW001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode").value("PKOPPLPW001"))
                .andExpect(jsonPath("$.isHeadquarter").value(false));
    }

    @Test
    void getSwiftDetails_shouldReturnHeadquarterDTOWithBranches_whenHeadquarterCodeProvided() throws Exception {
        HeadquarterDTO dto = HeadquarterDTO.builder()
                .swiftCode("PKOPPLPW")
                .bankName("PKO HQ")
                .address("HQ Address")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .branches(List.of(
                        BranchDTO.builder().swiftCode("PKOPPLPW001").address("Branch 1").isHeadquarter(false).build(),
                        BranchDTO.builder().swiftCode("PKOPPLPW002").address("Branch 2").isHeadquarter(false).build()
                ))
                .build();

        when(swiftCodeService.getSwiftDetails("PKOPPLPW")).thenReturn(dto);

        mockMvc.perform(get("/v1/swift-codes/PKOPPLPW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode").value("PKOPPLPW"))
                .andExpect(jsonPath("$.branches.length()").value(2));
    }

    @Test
    void getSwiftDetails_shouldReturnNotFound_whenSwiftCodeDoesNotExist() throws Exception {
        String nonExistentSwiftCode = "INVALID123";

        when(swiftCodeService.getSwiftDetails(nonExistentSwiftCode))
                .thenThrow(new ResourceNotFoundException("SwiftCode", "swiftCode", nonExistentSwiftCode));

        mockMvc.perform(get("/v1/swift-codes/" + nonExistentSwiftCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("SwiftCode not found with swiftCode: 'INVALID123'"));
    }

    ////
    @Test
    void getSwiftByCountry_shouldReturnSwiftCodes_whenCountryExists() throws Exception {
        CountryISO2CodeDTO dto = new CountryISO2CodeDTO();
        dto.setCountryISO2("PL");
        dto.setCountryName("POLAND");
        dto.setSwiftCodes(List.of(
                BranchDTO.builder().swiftCode("PKOPPLPW").address("HQ").isHeadquarter(true).build(),
                BranchDTO.builder().swiftCode("PKOPPLPW001").address("Branch").isHeadquarter(false).build()
        ));

        when(swiftCodeService.getSwiftCodesByCountry("PL")).thenReturn(dto);

        mockMvc.perform(get("/v1/swift-codes/country/PL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryISO2").value("PL"))
                .andExpect(jsonPath("$.countryName").value("POLAND"))
                .andExpect(jsonPath("$.swiftCodes.length()").value(2))
                .andExpect(jsonPath("$.swiftCodes[0].swiftCode").value("PKOPPLPW"));
    }

    @Test
    void getSwiftByCountry_shouldReturnNotFound_whenCountryDoesNotExist() throws Exception {
        when(swiftCodeService.getSwiftCodesByCountry("XX"))
                .thenThrow(new ResourceNotFoundException("Country", "countryISO2", "XX"));

        mockMvc.perform(get("/v1/swift-codes/country/XX"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Country not found with countryISO2: 'XX'"));
    }

    @Test
    void getSwiftByCountry_shouldReturnEmptyList_whenCountryHasNoSwiftCodes() throws Exception {
        CountryISO2CodeDTO dto = new CountryISO2CodeDTO();
        dto.setCountryISO2("EE");
        dto.setCountryName("ESTONIA");
        dto.setSwiftCodes(List.of());

        when(swiftCodeService.getSwiftCodesByCountry("EE")).thenReturn(dto);

        mockMvc.perform(get("/v1/swift-codes/country/EE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryISO2").value("EE"))
                .andExpect(jsonPath("$.swiftCodes").isArray())
                .andExpect(jsonPath("$.swiftCodes.length()").value(0));
    }

    /////

    @Test
    void deleteSwiftCode_shouldReturnSuccessMessage_whenSwiftCodeExists() throws Exception {
        String swiftCode = "PKOPPLPW";
        when(swiftCodeService.deleteSwiftCode(swiftCode))
                .thenReturn("Swift code deleted successfully");

        mockMvc.perform(delete("/v1/swift-codes/" + swiftCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Swift code deleted successfully"));
    }

    @Test
    void deleteSwiftCode_shouldReturnNotFound_whenSwiftCodeDoesNotExist() throws Exception {
        String nonExistentCode = "NONEXIST123";

        when(swiftCodeService.deleteSwiftCode(nonExistentCode))
                .thenThrow(new ResourceNotFoundException("SwiftCode", "swiftCode", nonExistentCode));

        mockMvc.perform(delete("/v1/swift-codes/" + nonExistentCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("SwiftCode not found with swiftCode: 'NONEXIST123'"));
    }


}