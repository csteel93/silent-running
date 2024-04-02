package com.steel.silent.entity;

import java.math.BigDecimal;

public interface IdentifiableBody {

    String name();

    String classification();

    BigDecimal x();

    BigDecimal y();

    BigDecimal aspect();

    BigDecimal radius();

    String getColor();

}
