package com.api19_4.api19_4.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UnitDTO {
    private String idUnit;
    private String productId;
    private String productName;
    private String unitName;
    private int quantity;
    private double unitPrice; // Giá của mỗi hộp
    private int stockQuantity;
    private double purchasePrice;
}
