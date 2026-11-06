package com.ticket.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum DiscountType {
    NONE {
        @Override
        public BigDecimal apply(BigDecimal price) {
            return price;
        }
    },
    PERCENT_10 {
        @Override
        public BigDecimal apply(BigDecimal price) {
            return price.multiply(BigDecimal.valueOf(0.90)).setScale(2, RoundingMode.HALF_UP);
        }
    },
    EARLY_BIRD_20 {
        @Override
        public BigDecimal apply(BigDecimal price) {
            return price.multiply(BigDecimal.valueOf(0.80)).setScale(2, RoundingMode.HALF_UP);
        }
    };

    public abstract BigDecimal apply(BigDecimal price);
}
