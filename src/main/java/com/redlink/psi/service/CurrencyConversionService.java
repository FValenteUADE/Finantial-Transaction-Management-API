package com.redlink.psi.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CurrencyConversionService {

    public BigDecimal convertToARS(BigDecimal amount, String currency) {
        // Para simplificar, asumimos una tasa de cambio ficticia con valores del 27/01/25.
        BigDecimal conversionRate = getConversionRate(currency, "ARS");
        return amount.multiply(conversionRate);
    }

    private BigDecimal getConversionRate(String fromCurrency, String toCurrency) {
        // Tomamos los valores de la moneda BLUE en cada caso.
        if (fromCurrency.equals("USD") && toCurrency.equals("ARS")) {
            return BigDecimal.valueOf(1230);
        }
        if (fromCurrency.equals("EUR") && toCurrency.equals("ARS")) {
            return BigDecimal.valueOf(1328);
        }
        return BigDecimal.ONE;
    }
}