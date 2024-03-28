package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
@Setter
@Getter
public class DailyTotalPaymentDTO {
    private LocalDate date;
    private BigDecimal totalPayment;
}
