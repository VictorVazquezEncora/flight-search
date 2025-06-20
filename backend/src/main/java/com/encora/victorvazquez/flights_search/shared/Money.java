package com.encora.victorvazquez.flights_search.shared;

import lombok.Value;
import java.math.BigDecimal;
import java.util.Currency;

@Value
public class Money {
    BigDecimal amount;
    Currency currency;

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }
} 