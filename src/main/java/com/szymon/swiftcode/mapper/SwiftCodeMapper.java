package com.szymon.swiftcode.mapper;

import com.szymon.swiftcode.dto.BranchDTO;
import com.szymon.swiftcode.dto.CountryISO2CodeDTO;
import com.szymon.swiftcode.dto.HeadquarterDTO;
import com.szymon.swiftcode.model.SwiftCode;

import java.util.List;

public class SwiftCodeMapper {
    public static BranchDTO toBranchDTO(SwiftCode model){
        BranchDTO dto = new BranchDTO();
        dto.setAddress(model.getAddress());
        dto.setBankName(model.getBankName());
        dto.setCountryISO2(model.getCountryISO2());
        dto.setCountryName(model.getCountry());
        dto.setIsHeadquarter(model.isHeadquarter());
        dto.setSwiftCode(model.getSwiftCode());

        return dto;
    }

    public static HeadquarterDTO toHeadquarterDTO(SwiftCode headquarter, List<SwiftCode> branches){
        HeadquarterDTO dto = new HeadquarterDTO();

        dto.setSwiftCode(headquarter.getSwiftCode());
        dto.setBankName(headquarter.getBankName());
        dto.setAddress(headquarter.getAddress());
        dto.setCountryISO2(headquarter.getCountryISO2());
        dto.setCountryName(headquarter.getCountry());
        dto.setIsHeadquarter(true);

        List<BranchDTO> branchDTOs = branches.stream()
                .map(SwiftCodeMapper::toBranchDTO)
                .toList();

        dto.setBranches(branchDTOs);

        return dto;
    }

    public static CountryISO2CodeDTO countryISO2CodeDTO(String countryISO, String countryName, List<BranchDTO> branches){
        CountryISO2CodeDTO dto = new CountryISO2CodeDTO();
        dto.setCountryISO2(countryISO);
        dto.setCountryName(countryName);
        dto.setSwiftCodes(branches);

        return dto;
    }
}
