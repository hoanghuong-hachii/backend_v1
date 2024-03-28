package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
public class DateStatusChangeDto {
    private Long idDateStatusChange;
    private String dateTimeOrder;
    private String status;
    private Long idBill;
    private String idInventory;
}
