package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
public class HourlyTotalPaymentDTO {
    private String  hour;
    private BigDecimal totalPayment;
}
