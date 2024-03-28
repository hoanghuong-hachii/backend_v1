package com.api19_4.api19_4.dto;

import com.api19_4.api19_4.models.Bill;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.PrimitiveIterator;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductBillDTO {
    private Long idProductBill;
    private int quantity;
    private double retailPrice;
    private String unitName;
    private double totalPriceProd;
    private String productId;
    private String productName;
    private int discount;
    private String status;
    // Add a constructor that accepts the parameters (int, double, Long)
//    public ProductBillDTO(int quantity, double totalPriceProd, Long productId) {
//        this.quantity = quantity;
//        this.totalPriceProd = totalPriceProd;
//        this.productId = productId;
//    }
}
