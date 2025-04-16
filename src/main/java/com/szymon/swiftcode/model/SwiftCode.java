package com.szymon.swiftcode.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SwiftCode {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="swift_code", length=11)
    private String swiftCode;

    @Column(name="ISO2")
    private String countryISO2;

    @Column(name="is_headquarter")
    private boolean isHeadquarter;

    @Column(name="bank_name", length=255)
    private String bankName;

    @Column(name="adress")
    private String address;

    @Column(name="city", length=255)
    private String city;

    @Column(name="country", length=255)
    private String country;

    @Column(name="time_zone")
    private String timeZone;

}
