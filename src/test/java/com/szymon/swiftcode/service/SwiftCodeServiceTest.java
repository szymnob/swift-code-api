package com.szymon.swiftcode.service;

import com.szymon.swiftcode.dto.BranchDTO;
import com.szymon.swiftcode.dto.CountryISO2CodeDTO;
import com.szymon.swiftcode.dto.HeadquarterDTO;
import com.szymon.swiftcode.exceptions.DuplicateResourceException;
import com.szymon.swiftcode.exceptions.ResourceNotFoundException;
import com.szymon.swiftcode.model.SwiftCode;
import com.szymon.swiftcode.repository.SwiftCodeRepository;
import jakarta.inject.Inject;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwiftCodeServiceTest {
    @Mock
    private SwiftCodeRepository swiftCodeRepository;

    @InjectMocks
    private SwiftCodeService swiftCodeService;

    private SwiftCode headquarterSwiftCode;
    private SwiftCode branchSwiftCode;
    private List<SwiftCode> branches;
    private BranchDTO branchDTO;
    private HeadquarterDTO headquarterDTO;

    @BeforeEach
    void setUp() {
        headquarterSwiftCode = SwiftCode.builder()
                .swiftCode("PKOPPLPW")
                .bankName("PKO Bank Polski")
                .address("ul. Puławska 15, 02-515 Warsaw")
                .countryISO2("PL")
                .country("POLAND")
                .isHeadquarter(true)
                .build();

        branchSwiftCode = SwiftCode.builder()
                .swiftCode("PKOPPLPW001")
                .bankName("PKO Bank Polski Branch")
                .address("ul. Marszałkowska 142, 00-061 Warsaw")
                .countryISO2("PL")
                .country("POLAND")
                .isHeadquarter(false)
                .build();

        SwiftCode branchSwiftCode2 = SwiftCode.builder()
                .swiftCode("PKOPPLPW002")
                .bankName("PKO Bank Polski Branch 2")
                .address("ul. Nowy Świat 6/12, 00-400 Warsaw")
                .countryISO2("PL")
                .country("POLAND")
                .isHeadquarter(false)
                .build();

        branches = Arrays.asList(branchSwiftCode, branchSwiftCode2);

        branchDTO = BranchDTO.builder()
                .swiftCode("BPHKPLPK")
                .bankName("Bank BPH")
                .address("ul. Towarowa 25A, 00-958 Warsaw")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .build();
    }

    @Test
    void getSwiftDetails_ShouldReturnBranchDTO_WhenSwiftCodeIsBranch() {
        when(swiftCodeRepository.findBySwiftCode("PKOPPLPW001")).thenReturn(branchSwiftCode);

        BranchDTO result = swiftCodeService.getSwiftDetails("PKOPPLPW001");

        assertNotNull(result);
        assertEquals("PKOPPLPW001", result.getSwiftCode());
        assertEquals("PKO Bank Polski Branch", result.getBankName());
        assertEquals("PL", result.getCountryISO2());
        assertFalse(result.getIsHeadquarter());
        verify(swiftCodeRepository, times(1)).findBySwiftCode("PKOPPLPW001");
    }
    @Test
    void getSwiftDetails_ShouldReturnHeadquarterDTO_WhenSwiftCodeIsHeadquarter() {
        when(swiftCodeRepository.findBySwiftCode("PKOPPLPW")).thenReturn(headquarterSwiftCode);
        when(swiftCodeRepository.findBySwiftCodeStartingWithAndIsHeadquarterFalse("PKOPPLPW")).thenReturn(branches);

        BranchDTO result = swiftCodeService.getSwiftDetails("PKOPPLPW");

        assertNotNull(result);
        assertInstanceOf(HeadquarterDTO.class, result);
        HeadquarterDTO headquarterResult = (HeadquarterDTO) result;
        assertEquals("PKOPPLPW", headquarterResult.getSwiftCode());
        assertEquals("PKO Bank Polski", headquarterResult.getBankName());
        assertEquals("PL", headquarterResult.getCountryISO2());
        assertTrue(headquarterResult.getIsHeadquarter());
        assertEquals(2, headquarterResult.getBranches().size());
        verify(swiftCodeRepository, times(1)).findBySwiftCode("PKOPPLPW");
        verify(swiftCodeRepository, times(1)).findBySwiftCodeStartingWithAndIsHeadquarterFalse("PKOPPLPW");
    }

    @Test
    void getSwiftDetails_ShouldThrowResourceNotFoundException_WhenSwiftCodeNotFound() {
        when(swiftCodeRepository.findBySwiftCode("NONEXISTENT")).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> swiftCodeService.getSwiftDetails("NONEXISTENT")
        );

        assertEquals("SwiftCode not found with swiftCode: 'NONEXISTENT'", exception.getMessage());
        verify(swiftCodeRepository, times(1)).findBySwiftCode("NONEXISTENT");
    }

    @Test
    void getSwiftCodesByCountry_ShouldReturnCountryISO2CodeDTO_WhenCountryExists() {
        List<SwiftCode> polandSwiftCodes = Arrays.asList(headquarterSwiftCode, branchSwiftCode);
        when(swiftCodeRepository.findByCountryISO2IgnoreCase("PL")).thenReturn(polandSwiftCodes);

        CountryISO2CodeDTO result = swiftCodeService.getSwiftCodesByCountry("PL");

        assertNotNull(result);
        assertEquals("PL", result.getCountryISO2());
        assertEquals("POLAND", result.getCountryName());
        assertEquals(2, result.getSwiftCodes().size());
        verify(swiftCodeRepository, times(1)).findByCountryISO2IgnoreCase("PL");
    }

    @Test
    void getSwiftCodesByCountry_ShouldThrowResourceNotFoundException_WhenCountryNotFound() {
        when(swiftCodeRepository.findByCountryISO2IgnoreCase("XX")).thenReturn(Collections.emptyList());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> swiftCodeService.getSwiftCodesByCountry("XX")
        );

        assertEquals("Country not found with countryISO2: 'XX'", exception.getMessage());
        verify(swiftCodeRepository, times(1)).findByCountryISO2IgnoreCase("XX");
    }

    @Test
    void deleteSwiftCode_ShouldThrowResourceNotFoundException_WhenSwiftCodeNotFound() {
        when(swiftCodeRepository.existsBySwiftCode("NONEXISTENT")).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> swiftCodeService.deleteSwiftCode("NONEXISTENT")
        );

        assertEquals("SwiftCode not found with swiftCode: 'NONEXISTENT'", exception.getMessage());
        verify(swiftCodeRepository, times(1)).existsBySwiftCode("NONEXISTENT");
        verify(swiftCodeRepository, never()).deleteBySwiftCode(anyString());
    }

    @Test
    void addSwiftCode_ShouldReturnSuccessMessage_WhenSwiftCodeNotExists() {
        when(swiftCodeRepository.existsBySwiftCode("BPHKPLPK")).thenReturn(false);

        String result = swiftCodeService.addSwiftCode(branchDTO);

        assertEquals("Swift code added successfully", result);
        verify(swiftCodeRepository, times(1)).existsBySwiftCode("BPHKPLPK");
        verify(swiftCodeRepository, times(1)).save(any(SwiftCode.class));
    }

    @Test
    void addSwiftCode_ShouldThrowDuplicateResourceException_WhenSwiftCodeExists() {
        when(swiftCodeRepository.existsBySwiftCode("BPHKPLPK")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> swiftCodeService.addSwiftCode(branchDTO)
        );

        assertEquals("SwiftCode already exists with swiftCode: 'BPHKPLPK'", exception.getMessage());
        verify(swiftCodeRepository, times(1)).existsBySwiftCode("BPHKPLPK");
        verify(swiftCodeRepository, never()).save(any(SwiftCode.class));
    }


}