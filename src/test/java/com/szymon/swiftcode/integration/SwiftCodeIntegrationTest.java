package com.szymon.swiftcode.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.szymon.swiftcode.dto.BranchDTO;
import com.szymon.swiftcode.model.SwiftCode;
import com.szymon.swiftcode.repository.SwiftCodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SwiftCodeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SwiftCodeRepository repository;

    @Test
    void addSwift_shouldReturn201_whenValidDataProvided() throws Exception {
        BranchDTO dto = BranchDTO.builder()
                .swiftCode("TESTPLPW")
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .build();

        when(repository.existsBySwiftCode("TESTPLPW")).thenReturn(false);
        when(repository.save(any(SwiftCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Swift code added successfully"));

        verify(repository).save(any(SwiftCode.class));
    }

    @Test
    void addSwift_shouldReturnConflict_whenSwiftCodeAlreadyExists() throws Exception {
        BranchDTO dto = BranchDTO.builder()
                .swiftCode("DUPLICATE")
                .bankName("Duplicate Bank")
                .address("Some Address")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .build();

        when(repository.existsBySwiftCode("DUPLICATE")).thenReturn(true);

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("SwiftCode already exists with swiftCode: 'DUPLICATE'"));

        verify(repository, never()).save(any());
    }

    @Test
    void addSwift_shouldReturnBadRequest_whenSwiftCodeIsTooShort() throws Exception {
        BranchDTO dto = BranchDTO.builder()
                .swiftCode("TOO") // za krótki kod
                .bankName("Test Bank")
                .address("Test Address")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(true)
                .build();

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.swiftCode")
                        .value("Swift code must be between 8 and 11 characters"));
    }

    ///////
    @Test
    void getSwiftDetails_shouldReturnBranchDTO_whenValidSwiftCodeGiven() throws Exception {
        BranchDTO dto = BranchDTO.builder()
                .swiftCode("PKOPPLPW001")
                .bankName("PKO Branch")
                .address("Warsaw")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .build();

        when(repository.findBySwiftCode("PKOPPLPW001")).thenReturn(SwiftCode.builder()
                .swiftCode(dto.getSwiftCode())
                .bankName(dto.getBankName())
                .address(dto.getAddress())
                .countryISO2(dto.getCountryISO2())
                .country(dto.getCountryName())
                .isHeadquarter(dto.getIsHeadquarter())
                .build());

        mockMvc.perform(get("/v1/swift-codes/PKOPPLPW001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode").value("PKOPPLPW001"))
                .andExpect(jsonPath("$.isHeadquarter").value(false));
    }

    @Test
    void getSwiftDetails_shouldReturnNotFound_whenSwiftCodeDoesNotExist() throws Exception {
        when(repository.findBySwiftCode("NOTFOUND")).thenReturn(null);

        mockMvc.perform(get("/v1/swift-codes/NOTFOUND"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("SwiftCode not found with swiftCode: 'NOTFOUND'"));
    }

    ///
    @Test
    void getSwiftByCountry_shouldReturnList_whenValidCountryCodeGiven() throws Exception {
        List<SwiftCode> swiftCodes = List.of(
                SwiftCode.builder().swiftCode("PKO12345").bankName("PKO").address("Address").countryISO2("PL").country("POLAND").isHeadquarter(true).build(),
                SwiftCode.builder().swiftCode("PKO12346").bankName("PKO Branch").address("Branch Address").countryISO2("PL").country("POLAND").isHeadquarter(false).build()
        );

        when(repository.findByCountryISO2IgnoreCase("PL")).thenReturn(swiftCodes);

        mockMvc.perform(get("/v1/swift-codes/country/PL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryISO2").value("PL"))
                .andExpect(jsonPath("$.swiftCodes.length()").value(2));
    }

    @Test
    void getSwiftByCountry_shouldReturnNotFound_whenNoSwiftCodesExistForCountry() throws Exception {
        when(repository.findByCountryISO2IgnoreCase("XX")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/swift-codes/country/XX"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Country not found with countryISO2: 'XX'"));
    }

    ////
    @Test
    void deleteSwiftCode_shouldReturnOk_whenSwiftCodeExists() throws Exception {
        when(repository.existsBySwiftCode("PKO12345")).thenReturn(true);

        mockMvc.perform(delete("/v1/swift-codes/PKO12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Swift code deleted successfully"));
    }

    @Test
    void deleteSwiftCode_shouldReturnNotFound_whenSwiftCodeDoesNotExist() throws Exception {
        when(repository.existsBySwiftCode("DOESNOTEXIST")).thenReturn(false);

        mockMvc.perform(delete("/v1/swift-codes/DOESNOTEXIST"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("SwiftCode not found with swiftCode: 'DOESNOTEXIST'"));
    }




}
