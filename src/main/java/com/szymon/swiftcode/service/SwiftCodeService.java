package com.szymon.swiftcode.service;

import com.szymon.swiftcode.dto.BranchDTO;
import com.szymon.swiftcode.dto.CountryISO2CodeDTO;
import com.szymon.swiftcode.exceptions.DuplicateResourceException;
import com.szymon.swiftcode.exceptions.ResourceNotFoundException;
import com.szymon.swiftcode.mapper.SwiftCodeMapper;
import com.szymon.swiftcode.model.SwiftCode;
import com.szymon.swiftcode.repository.SwiftCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SwiftCodeService {
    private final SwiftCodeRepository repository;

    //endpoint: /api/v1/swiftcode/{swiftCode}
    public BranchDTO getSwiftDetails(String swiftCode) {
        SwiftCode swiftCodeEntity = Optional.ofNullable(repository.findBySwiftCode(swiftCode))
                .orElseThrow(() -> new ResourceNotFoundException("SwiftCode", "swiftCode", swiftCode));


        if(swiftCodeEntity.isHeadquarter()){
            String branchPrefix = swiftCode.substring(0, 8);

            List<SwiftCode> branches = repository.findBySwiftCodeStartingWithAndIsHeadquarterFalse(branchPrefix);

            return SwiftCodeMapper.toHeadquarterDTO(swiftCodeEntity, branches);
        }else{
            return SwiftCodeMapper.toBranchDTO(swiftCodeEntity);
        }
    }

    //endpoint: /api/v1/swiftcode/country/{countryISO2}
    public CountryISO2CodeDTO getSwiftCodesByCountry(String countryISO2) {
        List<SwiftCode> swiftCodes = repository.findByCountryISO2IgnoreCase(countryISO2);

        String countryName;
        if(swiftCodes.isEmpty()){
            throw new ResourceNotFoundException("Country", "countryISO2", countryISO2);
        }else{
            countryName = swiftCodes.getFirst().getCountry();
        }

        List<BranchDTO> branchDTOs = swiftCodes.stream()
                .map(SwiftCodeMapper::toBranchDTO)
                .toList();

        return SwiftCodeMapper.countryISO2CodeDTO(countryISO2, countryName, branchDTOs);
    }

    //endpoint: delete /api/v1/swiftcode/{swiftCode}
    @Transactional
    public String deleteSwiftCode(String swiftCode) {
        if (!repository.existsBySwiftCode(swiftCode)) {
            throw new ResourceNotFoundException("SwiftCode", "swiftCode", swiftCode);
        }

        repository.deleteBySwiftCode(swiftCode);
        return "Swift code deleted successfully";
    }

    //endpoint: post /api/v1/swiftcode
    @Transactional
    public String addSwiftCode(BranchDTO branchDTO) {
        String swiftCode = branchDTO.getSwiftCode();
        if (repository.existsBySwiftCode(swiftCode)) {
            throw new DuplicateResourceException("SwiftCode", "swiftCode", branchDTO.getSwiftCode());
        }

        SwiftCode swiftCodeEntity = SwiftCode.builder()
                .swiftCode(branchDTO.getSwiftCode())
                .bankName(branchDTO.getBankName())
                .address(branchDTO.getAddress())
                .countryISO2(branchDTO.getCountryISO2().toUpperCase())
                .country(branchDTO.getCountryName().toUpperCase())
                .isHeadquarter(branchDTO.getIsHeadquarter())
                .build();

        repository.save(swiftCodeEntity);
        return "Swift code added successfully";
    }
}
