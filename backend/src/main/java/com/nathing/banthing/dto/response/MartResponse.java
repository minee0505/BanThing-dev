package com.nathing.banthing.dto.response;

import com.nathing.banthing.entity.Mart;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MartResponse {

    private final Long martId;
    private final String martName;
    private final String martBrand;
    private final String address;
    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public MartResponse(Mart mart) {
        this.martId = mart.getMartId();
        this.martName = mart.getMartName();
        this.martBrand = mart.getMartBrand().name();
        this.address = mart.getAddress();
        this.latitude = mart.getLatitude();
        this.longitude = mart.getLongitude();
    }
}