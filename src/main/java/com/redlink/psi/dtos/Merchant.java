package com.redlink.psi.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Merchant {
    private String name;

    @JsonProperty("merchant_id")
    private String merchantId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }
}