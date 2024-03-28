package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BillSummaryDto {
    private String date;
    private  double totalPrice;
    private int totalBillCount;
}
