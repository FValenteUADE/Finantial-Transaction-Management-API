package com.redlink.psi.service;

import java.math.BigDecimal;

public interface CurrencyConversionService {
    BigDecimal convertToARS(BigDecimal amount, String currency);
}
