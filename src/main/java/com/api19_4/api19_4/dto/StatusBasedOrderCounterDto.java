package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StatusBasedOrderCounterDto {
    private int quantity;
    private String status;
}
